package com.kidmily.algoga_server.report.infrastructure.mapper;

import com.kidmily.algoga_server.report.domain.model.Report;
import com.kidmily.algoga_server.report.infrastructure.persistence.entity.ReportJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReportMapper {

    default ReportJpaEntity toJpaEntity(Report report) {
        if (report == null) return null;
        return ReportJpaEntity.builder()
                .userId(report.getUserId())
                .reportedUserId(report.getReportedUserId())
                .targetId(report.getTargetId())
                .targetType(report.getTargetType())
                .reasonType(report.getReasonType())
                .detail(report.getDetail())
                .build();
    }

    default Report toDomain(ReportJpaEntity entity) {
        if (entity == null) return null;
        return Report.reconstitute(
                entity.getReportId(),
                entity.getUserId(),
                entity.getReportedUserId(),
                entity.getTargetId(),
                entity.getTargetType(),
                entity.getReasonType(),
                entity.getDetail(),
                entity.getCreatedAt()
        );
    }
}