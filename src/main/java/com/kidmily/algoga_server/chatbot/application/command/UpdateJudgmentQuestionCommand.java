// chatbot/application/command/UpdateJudgmentQuestionCommand.java
package com.kidmily.algoga_server.chatbot.application.command;
public record UpdateJudgmentQuestionCommand(Long judgmentQuestionId, String question, String answer) {}