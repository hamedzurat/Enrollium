package enrollium.server.db.entity;

import enrollium.server.db.entity.types.SubjectType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "subjects")
@Getter
@Setter
public class Subject extends BaseEntity {
    @Column(nullable = false)
    @NotBlank(message = "Subject name cannot be blank")
    @Size(max = 100, message = "Subject name must not exceed 100 characters")
    private String            name;
    //
    @Column(name = "code_name", unique = true, nullable = false)
    @NotBlank(message = "Code name cannot be blank")
    @Size(max = 8, message = "Code name must not exceed 8 characters")
    private String            codeName;
    //
    @Column(nullable = false)
    @NotNull(message = "Credits cannot be null")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 5, message = "Credits must not exceed 5")
    private Integer           credits;
    //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Subject type cannot be null")
    private SubjectType       type;
    //
    @OneToMany(mappedBy = "subject")
    private Set<Prerequisite> prerequisites = new HashSet<>();
}
