package enrollium.server.db.entity;

import enrollium.server.db.entity.types.Season;
import enrollium.server.db.entity.types.TrimesterStatus;
import jakarta.persistence.*;
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
    @Min(value = 30, message = "Code must be at least 030 (format: YY[1|2|3])")
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
    private LocalDateTime   courseSelectionStart;
    //
    @Column(name = "course_selection_end")
    private LocalDateTime   courseSelectionEnd;
    //
    @Column(name = "section_registration_start")
    private LocalDateTime   sectionRegistrationStart;
    //
    @Column(name = "section_registration_end")
    private LocalDateTime   sectionRegistrationEnd;

    @PrePersist
    @PreUpdate
    private void validateTrimester() {
        validateDateRanges();
        validateCode();
        validateStatusDates();
    }

    private void validateDateRanges() {
        // Validate course selection dates
        if (courseSelectionStart != null && courseSelectionEnd != null) {
            if (courseSelectionStart.isAfter(courseSelectionEnd)) {
                throw new IllegalArgumentException("Course selection start date must be before or equal to end date");
            }
        }

        // Validate section registration dates
        if (sectionRegistrationStart != null && sectionRegistrationEnd != null) {
            if (sectionRegistrationStart.isAfter(sectionRegistrationEnd)) {
                throw new IllegalArgumentException("Section registration start date must be before or equal to end date");
            }
        }

        // Validate sequence of date ranges
        if (courseSelectionEnd != null && sectionRegistrationStart != null) {
            if (courseSelectionEnd.isAfter(sectionRegistrationStart)) {
                throw new IllegalArgumentException("Course selection must end before section registration begins");
            }
        }
    }

    private void validateCode() {
        if (code == null) return;

        // Extract last digit (must be 1, 2, or 3)
        int lastDigit = code % 10;
        if (lastDigit < 1 || lastDigit > 3) {
            throw new IllegalArgumentException("Trimester code must end with 1, 2, or 3 (format: YY[1|2|3])");
        }

        // Extract year from code (first two digits)
        int codeYear = code / 10;
        if (codeYear != year % 100) {
            throw new IllegalArgumentException("Trimester code must match the last two digits of the year");
        }

        // Validate season matches trimester number
        Season expectedSeason = switch (lastDigit) {
            case 1 -> Season.SPRING;
            case 2 -> Season.SUMMER;
            case 3 -> Season.FALL;
            default -> throw new IllegalStateException("Unexpected trimester number: " + lastDigit);
        };

        if (season != expectedSeason) {
            throw new IllegalArgumentException("Season must match trimester number (1=SPRING, 2=SUMMER, 3=FALL)");
        }
    }

    private void validateStatusDates() {
        switch (status) {
            case UPCOMING -> {
                if (courseSelectionStart == null)
                    throw new IllegalArgumentException("UPCOMING status requires course selection start date");
            }
            case COURSE_SELECTION -> {
                if (courseSelectionStart == null || courseSelectionEnd == null)
                    throw new IllegalArgumentException("COURSE_SELECTION status requires course selection dates");
            }
            case SECTION_CREATION -> {
                if (courseSelectionEnd == null || sectionRegistrationStart == null)
                    throw new IllegalArgumentException("SECTION_CREATION status requires course selection end and section registration start dates");
            }
            case SECTION_SELECTION -> {
                if (sectionRegistrationStart == null || sectionRegistrationEnd == null)
                    throw new IllegalArgumentException("SECTION_SELECTION status requires section registration dates");
            }
            case ONGOING, COMPLETED -> {
                if (sectionRegistrationEnd == null)
                    throw new IllegalArgumentException("ONGOING/COMPLETED status requires section registration end date");
            }
        }
    }
}
