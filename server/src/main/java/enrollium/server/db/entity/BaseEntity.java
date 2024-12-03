package enrollium.server.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;


@MappedSuperclass // this is not a table
@Getter
@Setter
@ToString
@EqualsAndHashCode
public abstract class BaseEntity {
    @Id // primary key
    @GeneratedValue(strategy = GenerationType.UUID) // auto generate a UUID
    private UUID          id;
    //
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @PastOrPresent(message = "Creation date must be in the past or present")
    private LocalDateTime createdAt;
    //
    @UpdateTimestamp
    @Column(name = "updated_at")
    @PastOrPresent(message = "Update date must be in the past or present")
    private LocalDateTime updatedAt;
    //
    @Version
    private Long          version;
    //
    @Column(name = "created_by", updatable = false)
    @Size(max = 255, message = "Created by must not exceed 255 characters")
    private String        createdBy;
    //
    @Column(name = "updated_by")
    @Size(max = 255, message = "Updated by must not exceed 255 characters")
    private String        updatedBy;
    //
    @Size(max = 1000, message = "Info must not exceed 500 characters")
    private String        info;

    // https://stackoverflow.com/a/38056446
    // https://stackoverflow.com/a/51989629
    @PrePersist
    @PreUpdate
    public void trimStrings() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                try {
                    field.setAccessible(true);
                    String value = (String) field.get(this);
                    if (value != null) field.set(this, value.trim());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to trim field: " + field.getName(), e);
                }
            }
        }
    }
}
