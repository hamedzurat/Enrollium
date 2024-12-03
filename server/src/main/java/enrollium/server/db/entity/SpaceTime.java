package enrollium.server.db.entity;

import enrollium.server.db.entity.types.SubjectType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;


@Entity
@Table(name = "space_time", uniqueConstraints = @UniqueConstraint(columnNames = {"room_name", "day_of_week", "start_time"}))
@Getter
@Setter
public class SpaceTime extends BaseEntity {
    @Column(name = "room_name", nullable = false)
    @NotBlank(message = "Room name cannot be blank")
    @Size(max = 100, message = "Room name must not exceed 100 characters")
    private String      name;
    //
    @Column(name = "room_number", nullable = false)
    @NotBlank(message = "Room number cannot be blank")
    @Size(max = 8, message = "Room number must not exceed 8 characters")
    private String      roomNumber;
    //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Room type cannot be null")
    private SubjectType roomType;
    //
    @Column(nullable = false)
    @NotNull(message = "Day of the week cannot be null")
    private DayOfWeek   dayOfWeek;
    //
    @Column(name = "timeslot")
    @Min(value = 1, message = "Time slot must be at least 1")
    @Max(value = 6, message = "Time slot must not exceed 6")
    private Integer     timeSlot;

    @PrePersist
    @PreUpdate
    private void validateTimeSlot() {
        if (roomType == SubjectType.LAB && (timeSlot < 1 || timeSlot > 3)) {
            throw new IllegalArgumentException("Lab room time slot must be between 1 and 3");
        }
        if (roomType == SubjectType.THEORY && (timeSlot < 1 || timeSlot > 6)) {
            throw new IllegalArgumentException("Theory room time slot must be between 1 and 6");
        }
    }
}
