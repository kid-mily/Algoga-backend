ALTER TABLE quiz_submission_answers
    MODIFY COLUMN question TEXT NOT NULL,
    MODIFY COLUMN option1 TEXT NOT NULL,
    MODIFY COLUMN option2 TEXT NOT NULL,
    MODIFY COLUMN option3 TEXT NOT NULL,
    MODIFY COLUMN option4 TEXT NOT NULL,
    MODIFY COLUMN explanation TEXT;

ALTER TABLE course_qnas
    MODIFY COLUMN question TEXT NOT NULL,
    MODIFY COLUMN answer TEXT;

ALTER TABLE course_qna_comments
    MODIFY COLUMN content TEXT NOT NULL;

ALTER TABLE course_reviews
    MODIFY COLUMN content TEXT NOT NULL;

ALTER TABLE course_reward_failures
    MODIFY COLUMN failure_reason TEXT;
