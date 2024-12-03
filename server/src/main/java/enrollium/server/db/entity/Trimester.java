package enrollium.server.db.entity;

import enrollium.server.db.entity.types.Season;
import enrollium.server.db.entity.types.TrimesterStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "trimesters")
@Getter
@Setter
public class Trimester extends BaseEntity {
    @Column(unique = true, nullable = false)
    @NotNull(message = "Code cannot be null")
    @Min(value = 30, message = "Code must be at least 030 (format: YYY[1|2|3])")
    private Integer         code;
    //
    @Column(nullable = false)
    @NotNull(message = "Year cannot be null")
    @Min(value = 2003, message = "Year must be 1900 or later")
    private Integer         year;
    //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Season cannot be null")
    private Season          season;
    //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status cannot be null")
    private TrimesterStatus status;
    //
    @Column(name = "course_selection_start")
    @FutureOrPresent(message = "Course selection start date must be in the present or future")
    private LocalDateTime   courseSelectionStart;
    //
    @Column(name = "course_selection_end")
    @FutureOrPresent(message = "Course selection end date must be in the present or future")
    private LocalDateTime   courseSelectionEnd;
    //
    @Column(name = "section_registration_start")
    @FutureOrPresent(message = "Section registration start date must be in the present or future")
    private LocalDateTime   sectionRegistrationStart;
    //
    @Column(name = "section_registration_end")
    @FutureOrPresent(message = "Section registration end date must be in the present or future")
    private LocalDateTime   sectionRegistrationEnd;

    @PrePersist
    @PreUpdate
    private void validateDateRanges() {
        if (courseSelectionStart != null && courseSelectionEnd != null && courseSelectionStart.isAfter(courseSelectionEnd)) {
            throw new IllegalArgumentException("Course selection start date must be before or equal to end date");
        }
        if (sectionRegistrationStart != null && sectionRegistrationEnd != null && sectionRegistrationStart.isAfter(sectionRegistrationEnd)) {
            throw new IllegalArgumentException("Section registration start date must be before or equal to end date");
        }
    }
}
