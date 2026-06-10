// chatbot/application/command/AskChatbotCommand.java
package com.kidmily.algoga_server.chatbot.application.command;

public record AskChatbotCommand(
        Long userId,    // 컨트롤러에서 시큐리티를 통해 주입받은 유저 ID
        String question // 클라이언트가 보낸 질문 내용
) {}