package com.kidmily.algoga_server.lms.domain.model;

public class QuizSubmissionAnswer {

    private final Long id;
    private final Long submissionId;
    private final Long quizId;
    private final String question;
    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;
    private final int selectedOption;
    private final int correctOption;
    private final boolean correct;
    private final String explanation;

    private QuizSubmissionAnswer(
            Long id,
            Long submissionId,
            Long quizId,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int selectedOption,
            int correctOption,
            boolean correct,
            String explanation
    ) {
        this.id = id;
        this.submissionId = submissionId;
        this.quizId = quizId;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.selectedOption = selectedOption;
        this.correctOption = correctOption;
        this.correct = correct;
        this.explanation = explanation;
    }

    public static QuizSubmissionAnswer create(
            Long submissionId,
            Quiz quiz,
            int selectedOption
    ) {
        int correctOption = quiz.getCorrectOption();

        return new QuizSubmissionAnswer(
                null,
                submissionId,
                quiz.getId(),
                quiz.getQuestion(),
                quiz.getOption1(),
                quiz.getOption2(),
                quiz.getOption3(),
                quiz.getOption4(),
                selectedOption,
                correctOption,
                correctOption == selectedOption,
                quiz.getExplanation()
        );
    }

    public static QuizSubmissionAnswer withId(
            Long id,
            Long submissionId,
            Long quizId,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            int selectedOption,
            int correctOption,
            boolean correct,
            String explanation
    ) {
        return new QuizSubmissionAnswer(
                id,
                submissionId,
                quizId,
                question,
                option1,
                option2,
                option3,
                option4,
                selectedOption,
                correctOption,
                correct,
                explanation
        );
    }

    public Long getId() {
        return id;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public Long getQuizId() {
        return quizId;
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

    public int getSelectedOption() {
        return selectedOption;
    }

    public int getCorrectOption() {
        return correctOption;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getSelectedAnswer() {
        return getOptionText(selectedOption);
    }

    public String getCorrectAnswer() {
        return getOptionText(correctOption);
    }

    private String getOptionText(int optionNumber) {
        return switch (optionNumber) {
            case 1 -> option1;
            case 2 -> option2;
            case 3 -> option3;
            case 4 -> option4;
            default -> null;
        };
    }
}