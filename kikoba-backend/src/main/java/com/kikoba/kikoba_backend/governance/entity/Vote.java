package com.kikoba.kikoba_backend.governance.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "votes", uniqueConstraints = {
        @UniqueConstraint(name = "unique_poll_member_option", columnNames = {"poll_id", "group_member_id", "choice"})
}, indexes = {
        @Index(name = "idx_votes_poll", columnList = "poll_id"),
        @Index(name = "idx_votes_member", columnList = "group_member_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_member_id", nullable = false)
    private GroupMember groupMember;

    @Column(name = "choice", nullable = false, length = 255)
    private String choice;
}