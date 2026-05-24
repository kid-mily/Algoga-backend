package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateChapterCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateChapterCommand;
import com.kidmily.algoga_server.lms.domain.model.Chapter;

import java.util.List;

public interface AdminChapterUseCase {

    List<Chapter> getChapters(Long courseId);

    Chapter createChapter(CreateChapterCommand command);

    Chapter updateChapter(Long courseId, Long chapterId, UpdateChapterCommand command);

    void deleteChapter(Long courseId, Long chapterId);
}