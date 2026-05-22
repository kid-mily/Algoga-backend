package com.kidmily.algoga_server.notice.infrastructure.mapper;

import com.kidmily.algoga_server.notice.domain.model.Notice;
import com.kidmily.algoga_server.notice.infrastructure.persistence.entity.NoticeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoticeMapper {

    // Domain Model -> Entity 변환
    NoticeEntity toEntity(Notice notice);

    // Entity -> Domain Model 변환
    Notice toDomain(NoticeEntity noticeEntity);
}