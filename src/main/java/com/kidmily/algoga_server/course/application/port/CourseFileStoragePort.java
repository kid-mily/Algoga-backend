package com.kidmily.algoga_server.course.application.port;

public interface CourseFileStoragePort {

    String uploadFile(UploadFile file, String bucketName, String directory);

    void deleteFile(String bucketName, String fileUrl);
}