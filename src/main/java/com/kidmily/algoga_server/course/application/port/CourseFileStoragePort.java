package com.kidmily.algoga_server.course.application.port;

public interface CourseFileStoragePort {

    String uploadFile(UploadFile file, String directory);

    String uploadAttachmentFile(UploadFile file, String directory);

    void deleteFile(String key);
}
