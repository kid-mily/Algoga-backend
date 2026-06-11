package com.kidmily.algoga_server.lms.application.usecase;

import com.kidmily.algoga_server.lms.application.command.CreateChapterCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateChapterCommand;
import com.kidmily.algoga_server.lms.application.result.ChapterResult;

import java.util.List;

public interface ChapterUseCase {

    List<ChapterResult> getChapters(Long courseId);

    ChapterResult createChapter(CreateChapterCommand command);

    ChapterResult updateChapter(Long courseId, Long chapterId, UpdateChapterCommand command);

    void deleteChapter(Long courseId, Long chapterId);
}
