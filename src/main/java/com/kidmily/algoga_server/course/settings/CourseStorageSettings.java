package com.kidmily.algoga_server.course.settings;

import com.kidmily.algoga_server.global.port.out.StorageSettings;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CourseStorageSettings implements StorageSettings {

    private final String bucketName = "algoga-lms";

    private final String courseThumbnailDirectory = "lms/course-thumbnails";

    private final String courseFileDirectory = "lms/course-files";

    private final String chapterVideoDirectory = "lms/chapter-videos";

    private final String certificateDirectory = "lms/certificates";

    @Override
    public String getDirectory() {
        return "lms";
    }
}
