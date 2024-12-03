package enrollium.server.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class Student extends User {
    @Column(name = "university_id", unique = true, nullable = false)
    @NotNull(message = "University ID cannot be null")
    @Min(value = 0, message = "University ID cannot be negative")
    private Integer     universityId;
    //
    @OneToMany(mappedBy = "student")
    private Set<Course> courses = new HashSet<>();
}
