package com.github.misterchangray.oss.files.v1.pojo.request;

/**
 * @description:
 * @author: Ray.chang
 * @create: 2022-01-06 15:45
 **/
public class OSSFileMove {
    private String sourceBucketName;
    private String sourcePath;
    private String distBucketName;
    private String distPath;

    public String getSourceBucketName() {
        return sourceBucketName;
    }

    public void setSourceBucketName(String sourceBucketName) {
        this.sourceBucketName = sourceBucketName;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDistBucketName() {
        return distBucketName;
    }

    public void setDistBucketName(String distBucketName) {
        this.distBucketName = distBucketName;
    }

    public String getDistPath() {
        return distPath;
    }

    public void setDistPath(String distPath) {
        this.distPath = distPath;
    }
}
