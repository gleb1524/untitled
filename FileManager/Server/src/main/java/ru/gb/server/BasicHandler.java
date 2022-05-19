package ru.gb.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.gb.dto.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BasicHandler extends ChannelInboundHandlerAdapter {

    DataBaseService dataBaseService = new DataBaseService();
    private String login;
    private String password;
    private String auth;
    private final String SER_DIR = ".";
    File file;
    private long sizefile;
    private int byteRead;
    private int start = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        if (msg instanceof FileRequest) {
            FileRequest request = (FileRequest) msg;
            if (dataBaseService.hasAuthRegister(request.getAuth(), request.getLogin())) {
                System.out.println(request.getPath());
                Path path = Paths.get(request.getPath());
                if (!Files.exists(path)) {
                    ctx.writeAndFlush(new BasicResponse("creat_ok"));
                    Files.createDirectory(path);
                }
            }
        } else if (msg instanceof RegRequest) {
            RegRequest request = (RegRequest) msg;
            if (!dataBaseService.hasRegistration(request.getLogin())) {
                dataBaseService.creatRegistration(request.getLogin(), request.getPassword(),
                        request.getName(), request.getSurname());
                ctx.writeAndFlush(new BasicResponse("reg_ok"));
            } else {
                ctx.writeAndFlush(new BasicResponse("reg_no"));
            }
        }
        if (msg instanceof AuthRequest) {
            AuthRequest request = (AuthRequest) msg;
            login = request.getLogin();
            password = request.getPassword();
            auth = dataBaseService.creatAuth(request.getLogin(), request.getPassword());
            if (!dataBaseService.isAuthRegister(auth, login)) {
                dataBaseService.authRegister(auth, login);
            }
            ctx.writeAndFlush(new BasicResponse("auth " + auth));
            System.out.println(auth);
            if (dataBaseService.hasAuth(login, password)) {
                String filename = SER_DIR + "\\" + login + "root";
                Path path = Paths.get(filename);
                if (!Files.exists(path)) {
                    Files.createDirectory(path);
                    System.out.println("New Directory created !   " + filename);
                } else {
                    System.out.println("Directory already exists");
                }
                ctx.writeAndFlush(new BasicResponse("server_dir " + filename));
                ctx.writeAndFlush(new BasicResponse("auth_ok"));
            } else {
                ctx.writeAndFlush(new BasicResponse("auth_no"));
            }
        } else if (msg instanceof UploadRequest) {
            UploadRequest uploadRequest = (UploadRequest) msg;
            this.file = new File(uploadRequest.getRemPath() + "\\" + uploadRequest.getFilename());
            System.out.println(uploadRequest.getSize() + " размер файла");
            sizefile = uploadRequest.getSize();
            byteRead = uploadRequest.getByteRead();

            try (RandomAccessFile fos = new RandomAccessFile(file, "rw")) {

                if (sizefile - start > 16_000_000) {
                    fos.seek(start);
                    fos.write(uploadRequest.getData());
                    start = start + byteRead;
                    System.out.println(start);

                } else {
                    System.out.println("хотим взять последнюю пачку");
                    System.out.println("размер последней пачки " + uploadRequest.getLustDataPac().length);
                    fos.seek(sizefile - uploadRequest.getLustPac());
                    fos.write(uploadRequest.getLustDataPac());
                    start = 0;
                    System.out.println("записали последнюю пачку");
                    fos.close();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
