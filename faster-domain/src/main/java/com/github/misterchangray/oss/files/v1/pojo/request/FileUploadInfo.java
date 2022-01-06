package com.github.misterchangray.oss.files.v1.pojo.request;

/**
 * @description:
 * @author: Ray.chang
 * @create: 2022-01-05 17:19
 **/
public class FileUploadInfo {
    private String filename;
    private String fileSize;
    private String md5;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
