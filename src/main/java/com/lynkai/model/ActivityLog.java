package com.lynkai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Setter
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Setter
    private LocalDateTime timestamp;

    // Optional: constructors for convenience
    public ActivityLog() {} // JPA default

    public ActivityLog(User user, ActionType actionType) {
        this.user = user;
        this.actionType = actionType;
        this.timestamp = LocalDateTime.now();
    }

}
