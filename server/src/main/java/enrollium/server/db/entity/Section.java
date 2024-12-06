package enrollium.server.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "sections", uniqueConstraints = @UniqueConstraint(columnNames = {"trimester_id", "space_time_id"}))
@Getter
@Setter
public class Section extends BaseEntity {
    @Column(nullable = false)
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String         name;
    //
    @Column(nullable = false)
    @NotBlank(message = "Section identifier cannot be blank")
    @Size(max = 4, message = "Section identifier must not exceed 4 characters")
    private String         section;
    //
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    @NotNull(message = "Subject cannot be null")
    private Subject        subject;
    //
    @ManyToOne
    @JoinColumn(name = "trimester_id", nullable = false)
    @NotNull(message = "Trimester cannot be null")
    private Trimester      trimester;
    //
    @OneToMany
    @JoinTable(name = "section_space_times",//
            joinColumns = @JoinColumn(name = "section_id"),//
            inverseJoinColumns = @JoinColumn(name = "space_time_id"),//
            uniqueConstraints = @UniqueConstraint(//
                    columnNames = {"section_id", "space_time_id"}//
            )//
    )
    @NotEmpty(message = "Space time slots must not be empty")
    private Set<SpaceTime> spaceTimeSlots  = new HashSet<>();
    //
    @Column(name = "max_capacity", nullable = false)
    @NotNull(message = "Max capacity cannot be null")
    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer        maxCapacity;
    //
    @Column(name = "current_capacity", nullable = false)
    @NotNull(message = "Current capacity cannot be null")
    @Min(value = 0, message = "Current capacity must be at least 0")
    private Integer        currentCapacity = 0;
    //
    @ManyToMany
    @JoinTable(name = "section_faculty",//
            joinColumns = @JoinColumn(name = "section_id"),//
            inverseJoinColumns = @JoinColumn(name = "faculty_id")//
    )
    private Set<Faculty>   teachers        = new HashSet<>();
    //
    @OneToMany(mappedBy = "section")
    private Set<Course>    registrations   = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void validateSection() {
        validateCapacity();
        validateSpaceTimeSlots();
    }

    private void validateCapacity() {
        if (maxCapacity == null) {
            throw new IllegalArgumentException("Max capacity cannot be null");
        }
        if (currentCapacity == null) {
            throw new IllegalArgumentException("Current capacity cannot be null");
        }
        if (currentCapacity > maxCapacity) {
            throw new IllegalArgumentException("Current capacity cannot exceed max capacity");
        }
        if (maxCapacity < 1) {
            throw new IllegalArgumentException("Max capacity must be at least 1");
        }
        if (currentCapacity < 0) {
            throw new IllegalArgumentException("Current capacity cannot be negative");
        }
    }

    private void validateSpaceTimeSlots() {
        // First check if slots exist
        if (spaceTimeSlots == null || spaceTimeSlots.isEmpty()) {
            throw new IllegalArgumentException("Section must have at least one space-time slot");
        }

        // Only check for duplicates if we have a trimester (let JPA handle @NotNull validation)
        if (trimester != null) {
            Set<String> timeSlotKeys = new HashSet<>();
            for (SpaceTime slot : spaceTimeSlots) {
                String key = trimester.getId() + "-" + slot.getId();
                if (!timeSlotKeys.add(key)) {
                    throw new IllegalArgumentException("Duplicate space-time slot in trimester");
                }
            }
        }
    }
}
