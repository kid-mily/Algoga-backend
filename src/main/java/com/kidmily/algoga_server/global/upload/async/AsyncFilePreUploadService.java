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
    
    @Value("${cloud.aws.s3.endpoint}")
    private String s3Endpoint;

    public String startPreUpload(MultipartFile multipartFile, String bucketName, String directory, String trackingId) {
        String extension = getExtension(multipartFile.getOriginalFilename());
        String targetS3Key = directory + "/" + trackingId + extension;
        String expectedUrl = s3Endpoint + "/" + bucketName + "/" + targetS3Key;

        File tempFile = saveToLocalTemp(multipartFile, trackingId + extension);

        eventPublisher.publishEvent(new FilePreUploadEvent(
                tempFile.getAbsolutePath(), bucketName, targetS3Key, trackingId
        ));

        return expectedUrl;
    }

    public void cancelPreUpload(String bucketName, String trackingId, String fileUrl) {
        eventPublisher.publishEvent(new FileCancelEvent(bucketName, trackingId, fileUrl));
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