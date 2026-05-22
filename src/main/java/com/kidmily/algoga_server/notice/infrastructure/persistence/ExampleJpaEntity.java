package com.kidmily.algoga_server.notice.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "examples")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExampleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active;

    // 도메인 엔티티의 데이터를 받아 JPA 엔티티를 생성하는 생성자
    public ExampleJpaEntity(Long id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }
}