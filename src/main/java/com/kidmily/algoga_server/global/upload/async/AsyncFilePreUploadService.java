package com.kidmily.algoga_server.global.upload.async;

import com.kidmily.algoga_server.global.exception.BusinessException;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AsyncFilePreUploadService {

    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.file.upload-root:uploads}")
    private String tempDirPath;

    /**
     * 비동기 사전 업로드를 시작하고, DB에 저장할 object key(상대경로)를 반환한다.
     * 절대 URL은 응답 직렬화 시점에 CDN 루트로 매핑된다.
     */
    public String startPreUpload(MultipartFile multipartFile, String directory, String trackingId) {
        String extension = getExtension(multipartFile.getOriginalFilename());
        String targetS3Key = directory + "/" + trackingId + extension;

        File tempFile = saveToLocalTemp(multipartFile, trackingId + extension);

        eventPublisher.publishEvent(new FilePreUploadEvent(
                tempFile.getAbsolutePath(), targetS3Key, trackingId
        ));

        return targetS3Key;
    }

    public void cancelPreUpload(String trackingId, String fileKey) {
        eventPublisher.publishEvent(new FileCancelEvent(trackingId, fileKey));
    }

    private File saveToLocalTemp(MultipartFile multipartFile, String fileName) {
        File dir = new File(tempDirPath);
        if (!dir.exists()) dir.mkdirs();

        File tempFile = new File(dir, fileName);
        try {
            multipartFile.transferTo(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new BusinessException(GlobalErrorCode.SERVER_ERROR);
        }
    }

    private String getExtension(String filename) {
        return (filename != null && filename.contains(".")) ? filename.substring(filename.lastIndexOf(".")) : "";
    }
}
