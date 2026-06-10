// chatbot/application/command/RegisterJudgmentQuestionCommand.java
package com.kidmily.algoga_server.chatbot.application.command;

public record RegisterJudgmentQuestionCommand(
        Long managerId, // 컨트롤러에서 시큐리티를 통해 주입받은 관리자 ID
        String question,
        String answer
) {}