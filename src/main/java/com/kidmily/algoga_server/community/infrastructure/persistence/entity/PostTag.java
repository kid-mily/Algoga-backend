package com.kidmily.algoga_server.community.infrastructure.persistence.entity;

import com.kidmily.algoga_server.community.domain.model.PostTagType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_tag_id")
    private Long postTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostJpaEntity post;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", length = 50)
    private PostTagType tagType;

    @Column(name = "tag_name", length = 100)
    private String tagName;

}
