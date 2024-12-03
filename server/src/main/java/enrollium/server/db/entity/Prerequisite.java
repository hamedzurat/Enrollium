package enrollium.server.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "prerequisite")
@Getter
@Setter
public class Prerequisite extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    @NotNull(message = "Subject cannot be null")
    private Subject subject;
    //
    @ManyToOne
    @JoinColumn(name = "prerequisite_id", nullable = false)
    @NotNull(message = "Prerequisite subject cannot be null")
    private Subject prerequisite;
    //
    @Column(name = "minimum_grade", nullable = false)
    @NotNull(message = "Minimum grade cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum grade must be at least 0.0")
    @DecimalMax(value = "4.0", inclusive = true, message = "Minimum grade must not exceed 4.0")
    private Double  minimumGrade;
}
