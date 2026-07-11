package com.kidmily.algoga_server.quiz.domain.model;

public class Quiz {

    private final Long id;
    private final Long courseId;
    private final String question;
    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;
    private final int correctOption;
    private final String explanation;
    private final boolean deleted;

    private Quiz(
            Long id,
            Long courseId,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption,
            String explanation,
            boolean deleted
    ) {
        this.id = id;
        this.courseId = courseId;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
        this.explanation = explanation;
        this.deleted = deleted;
    }

    public static Quiz create(
            Long courseId,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption,
            String explanation
    ) {
        return new Quiz(
                null,
                courseId,
                question,
                option1,
                option2,
                option3,
                option4,
                correctOption,
                explanation,
                false
        );
    }

    public static Quiz withId(
            Long id,
            Long courseId,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int correctOption,
            String explanation,
            boolean deleted
    ) {
        return new Quiz(
                id,
                courseId,
                question,
                option1,
                option2,
                option3,
                option4,
                correctOption,
                explanation,
                deleted
        );
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public int getCorrectOption() {
        return correctOption;
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean isDeleted() {
        return deleted;
    }
}