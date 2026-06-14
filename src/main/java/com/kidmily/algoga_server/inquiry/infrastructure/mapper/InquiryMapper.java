package com.kidmily.algoga_server.inquiry.infrastructure.mapper;

import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.infrastructure.persistence.entity.InquiryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InquiryMapper {
    InquiryEntity toJpaEntity(Inquiry inquiry);

    default Inquiry toDomain(InquiryEntity entity) {
        if (entity == null) return null;
        return Inquiry.reconstitute(
                entity.getInquiryId(), entity.getUserId(), entity.getManagerId(),
                entity.getCategory(), entity.getTitle(), entity.getContent(),
                entity.getAnswer(), entity.getStatus(), entity.getCreatedAt(), entity.getAnsweredAt()
        );
    }
}