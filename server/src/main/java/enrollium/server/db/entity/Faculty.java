package enrollium.server.db.entity;

import enrollium.server.db.entity.types.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "faculty")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class Faculty extends User {
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Shortcode cannot be blank")
    @Size(max = 10, message = "Shortcode must not exceed 10 characters")
    private String       shortcode;
    //
    @ManyToMany
    @JoinTable(name = "faculty_subjects", //
            joinColumns = @JoinColumn(name = "faculty_id"), //
            inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private Set<Subject> teachableSubjects = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void validateFacultyType() {
        if (getType() != UserType.TEACHER && getType() != UserType.ADMIN) {
            throw new IllegalArgumentException("Faculty type must be either TEACHER or ADMIN");
        }
    }
}
