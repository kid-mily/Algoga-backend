package com.kidmily.algoga_server.course.application.usecase;

import com.kidmily.algoga_server.course.application.command.CreateChapterCommand;
import com.kidmily.algoga_server.course.application.command.UpdateChapterCommand;
import com.kidmily.algoga_server.course.application.result.ChapterResult;

import java.util.List;

public interface ChapterUseCase {

    List<ChapterResult> getChapters(Long courseId);

    ChapterResult createChapter(CreateChapterCommand command);

    ChapterResult updateChapter(Long courseId, Long chapterId, UpdateChapterCommand command);

    void deleteChapter(Long courseId, Long chapterId);
}
