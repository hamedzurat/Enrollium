package enrollium.server.db.entity;

import enrollium.server.db.entity.types.CourseStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "courses", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id", "trimester_id", "section_id"}))
@Getter
@Setter
public class Course extends BaseEntity {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    private CourseStatus status;
    //
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "Student cannot be null")
    private Student      student;
    //
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    @NotNull(message = "Subject cannot be null")
    private Subject      subject;
    //
    @ManyToOne
    @JoinColumn(name = "trimester_id", nullable = false)
    @NotNull(message = "Trimester cannot be null")
    private Trimester    trimester;
    //
    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section      section;
    //
    @DecimalMin(value = "0.0", inclusive = true, message = "Grade must be at least 0.0")
    @DecimalMax(value = "4.0", inclusive = true, message = "Grade must not exceed 4.0")
    private Double       grade;

    @PrePersist
    @PreUpdate
    private void validateCourse() {
        validateCourseRegistration();
        validateSectionMatch();
    }

    private void validateCourseRegistration() {
        switch (status) {
            case SELECTED -> {
                if (section != null) throw new IllegalArgumentException("Section must be null when status is SELECTED");
                if (grade != null) throw new IllegalArgumentException("Grade must be null when status is SELECTED");
            }
            case REGISTERED -> {
                if (section == null)
                    throw new IllegalArgumentException("Section must not be null when status is REGISTERED");
                if (grade != null) throw new IllegalArgumentException("Grade must be null when status is REGISTERED");
            }
            case COMPLETED -> {
                if (section == null)
                    throw new IllegalArgumentException("Section must not be null when status is COMPLETED");
                if (grade == null)
                    throw new IllegalArgumentException("Grade must not be null when status is COMPLETED");
                if (grade < 0.0 || grade > 4.0)
                    throw new IllegalArgumentException("Grade must be between 0.0 and 4.0 when status is COMPLETED");
            }
            case DROPPED -> {
                if (section == null)
                    throw new IllegalArgumentException("Section must not be null when status is DROPPED");
                if (grade != null) throw new IllegalArgumentException("Grade must be null when status is DROPPED");
            }
        }
    }

    private void validateSectionMatch() {
        if (section != null) {
            if (!section.getSubject().getId().equals(subject.getId()))
                throw new IllegalArgumentException("Section's subject must match course's subject");
            if (!section.getTrimester().getId().equals(trimester.getId()))
                throw new IllegalArgumentException("Section's trimester must match course's trimester");
        }
    }
}
