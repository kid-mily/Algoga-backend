package com.kidmily.algoga_server.course.application.port;

/**
 * 코스 도메인 파일 스토리지 포트.
 *
 * <p>버킷은 전역 단일 버킷을 사용하므로 파라미터로 받지 않는다.
 * 업로드는 저장된 object key(상대경로)를 반환하고, 삭제는 그 key를 그대로 받는다.
 */
public interface CourseFileStoragePort {

    String uploadFile(UploadFile file, String directory);

    /**
     * 첨부(강의자료)용 업로드. object key는 기존과 동일하게 UUID로 저장하되,
     * 다운로드 시 업로드 당시 원본 파일명으로 저장되도록 S3 객체에
     * Content-Disposition(attachment; filename=원본명)을 설정한다.
     */
    String uploadAttachmentFile(UploadFile file, String directory);

    void deleteFile(String key);
}
