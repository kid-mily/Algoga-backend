package com.kidmily.algoga_server.community.infrastructure.persistence.repository;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {
}
