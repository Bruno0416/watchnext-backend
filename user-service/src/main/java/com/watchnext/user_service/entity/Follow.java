package com.watchnext.user_service.entity;

import com.watchnext.user_service.enums.FollowStatus;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "follows",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_follow_pair",
        columnNames = { "follower_profile_id", "followee_profile_id" }
    ),
    indexes = {
        @Index(
            name = "idx_follows_followee_status",
            columnList = "followee_profile_id, status"
        ),
        @Index(
            name = "idx_follows_follower_status",
            columnList = "follower_profile_id, status"
        ),
    },
    check = @CheckConstraint(
        name = "chk_no_self_follow",
        constraint = "follower_profile_id <> followee_profile_id"
    )
)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = { "follower", "followee" })
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "follower_profile_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_follow_follower")
    )
    private Profile follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "followee_profile_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_follow_followee")
    )
    private Profile followee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FollowStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
