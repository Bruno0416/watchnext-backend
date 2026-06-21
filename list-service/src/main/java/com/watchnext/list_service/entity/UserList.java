package com.watchnext.list_service.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user_list")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = "items")
public class UserList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private ListVisibility visibility = ListVisibility.PRIVATE;

    @OneToMany(
        mappedBy = "list",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("position ASC")
    @Builder.Default
    private List<ListItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addItem(ListItem item) {
        item.setList(this);
        item.setPosition(items.size());
        items.add(item);
    }

    public void removeItem(ListItem item) {
        items.remove(item);
        item.setList(null);
    }
}
