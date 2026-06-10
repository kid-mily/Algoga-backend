package com.kidmily.algoga_server.inquiry.infrastructure.mapper;

import com.kidmily.algoga_server.inquiry.domain.model.Inquiry;
import com.kidmily.algoga_server.inquiry.infrastructure.persistence.entity.InquiryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InquiryMapper {
    InquiryEntity toJpaEntity(Inquiry inquiry);

    default Inquiry toDomain(InquiryEntity jpaEntity) {
        if (jpaEntity == null) return null;
        return Inquiry.reconstitute(
                jpaEntity.getInquiryId(), jpaEntity.getUserId(), jpaEntity.getManagerId(),
                jpaEntity.getQuestion(), jpaEntity.getAnswer(), jpaEntity.getStatus(),
                jpaEntity.getCreatedAt(), jpaEntity.getAnsweredAt()
        );
    }
}