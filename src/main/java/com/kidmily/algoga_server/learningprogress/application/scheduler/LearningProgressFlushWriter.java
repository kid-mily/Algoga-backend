package com.kidmily.algoga_server.learningprogress.application.scheduler;

import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LearningProgressFlushWriter {

    private final LearningProgressRepository learningProgressRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LearningProgress save(LearningProgress learningProgress) {
        return learningProgressRepository.save(learningProgress);
    }
}
