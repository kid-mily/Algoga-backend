package com.kidmily.algoga_server.community.application.usecase;

import com.kidmily.algoga_server.community.application.command.AdminDeleteCommentCommand;
import com.kidmily.algoga_server.community.application.command.CreateCommentCommand;
import com.kidmily.algoga_server.community.application.command.DeleteCommentCommand;
import com.kidmily.algoga_server.community.application.command.UpdateCommentCommand;
import com.kidmily.algoga_server.community.domain.model.Comment;

public interface CommentCommandUseCase {
    Comment handle(CreateCommentCommand command);
    Comment handle(UpdateCommentCommand command);
    void handle(DeleteCommentCommand command);
    void handle(AdminDeleteCommentCommand command);
}