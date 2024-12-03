package enrollium.server.db.entity;

import enrollium.server.db.entity.types.NotificationCategory;
import enrollium.server.db.entity.types.NotificationScope;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull(message = "Sender cannot be null")
    private User                 sender;
    //
    @Column(nullable = false)
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 128, message = "Title must not exceed 128 characters")
    private String               title;
    //
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Content cannot be blank")
    @Size(max = 4000, message = "Content must not exceed 4000 characters")
    private String               content;
    //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Category cannot be null")
    private NotificationCategory category;
    //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Scope cannot be null")
    private NotificationScope    scope;
    //
    @ManyToOne
    @JoinColumn(name = "trimester_id")
    private Trimester            trimester;
    //
    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section              section;
    //
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User                 targetUser;

    @PrePersist
    @PreUpdate
    private void validateCourseRegistration() {
        switch (scope) {
            case GLOBAL -> {
                if (trimester != null) throw new IllegalArgumentException("Trimester must be null for GLOBAL scope.");
                if (section != null) throw new IllegalArgumentException("Section must be null for GLOBAL scope.");
                if (targetUser != null)
                    throw new IllegalArgumentException("Target user must be null for GLOBAL scope.");
            }
            case TRIMESTER -> {
                if (trimester == null)
                    throw new IllegalArgumentException("Trimester must not be null for TRIMESTER scope.");
                if (section != null) throw new IllegalArgumentException("Section must be null for TRIMESTER scope.");
                if (targetUser != null)
                    throw new IllegalArgumentException("Target user must be null for TRIMESTER scope.");
            }
            case SECTION -> {
                if (trimester != null) throw new IllegalArgumentException("Trimester must be null for SECTION scope.");
                if (section == null) throw new IllegalArgumentException("Section must not be null for SECTION scope.");
                if (targetUser != null)
                    throw new IllegalArgumentException("Target user must be null for SECTION scope.");
            }
            case USER -> {
                if (trimester != null) throw new IllegalArgumentException("Trimester must be null for USER scope.");
                if (section != null) throw new IllegalArgumentException("Section must be null for USER scope.");
                if (targetUser == null)
                    throw new IllegalArgumentException("Target user must not be null for USER scope.");
            }
        }
    }
}
