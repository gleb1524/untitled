package ru.gb.dto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UploadRequest implements BasicRequest {

    private final File file;
    private final String filename;
    byte[] data;
    byte[] lustDataPac;
    private int packageCount;

    public int getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(int packageCount) {
        this.packageCount = packageCount;
    }

    public byte[] getLustDataPac() {
        return lustDataPac;
    }

    public void setLustDataPac(byte[] lustDataPac) {
        this.lustDataPac = lustDataPac;
    }

    private final String remPath;
    private final long size;
    private int byteRead;
    private int lustPac;

    public int getLustPac() {
        return lustPac;
    }

    public void setLustPac(int lustPac) {
        this.lustPac = lustPac;
    }

    public int getByteRead() {
        return byteRead;
    }

    public void setByteRead(int byteRead) {
        this.byteRead = byteRead;
    }

    public long getSize() {
        return size;
    }


    public UploadRequest(String path, String remPath) {
        this.remPath = remPath;
        this.file = new File(path);
        this.filename = file.getName();
        try {
            this.size = Files.size(Paths.get(path));
            data = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public File getFile() {
        return file;
    }


    public String getRemPath() {
        return remPath;
    }

    @Override
    public String getType() {
        return null;
    }


}
