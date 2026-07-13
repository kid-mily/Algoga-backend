package com.kidmily.algoga_server.course.application.port;

/**
 * 코스 도메인 파일 스토리지 포트.
 *
 * <p>버킷은 전역 단일 버킷을 사용하므로 파라미터로 받지 않는다.
 * 업로드는 저장된 object key(상대경로)를 반환하고, 삭제는 그 key를 그대로 받는다.
 */
public interface CourseFileStoragePort {

    String uploadFile(UploadFile file, String directory);

    void deleteFile(String key);
}
