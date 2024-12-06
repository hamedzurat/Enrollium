package enrollium.server.db.entity;

import enrollium.server.db.DB;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "prerequisite", uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "prerequisite_id"}))
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

    @PrePersist
    @PreUpdate
    private void validatePrerequisite() {
        // Check for self-prerequisite
        if (subject.getId().equals(prerequisite.getId()))
            throw new IllegalArgumentException("A subject cannot be its own prerequisite");

        // Get current session and check for cycles
        if (hasCircularDependency(DB.getSessionFactory().openSession(), subject.getId(), prerequisite.getId()))
            throw new IllegalArgumentException("Circular prerequisite dependency detected");
    }

    private boolean hasCircularDependency(Session session, UUID startId, UUID currentId) {
        // If we reach back to the start, we found a cycle
        if (startId.equals(currentId)) return true;

        // Find all prerequisites of the current subject
        @SuppressWarnings("unchecked")
        List<UUID> nextPrereqs = session.createQuery("SELECT p.prerequisite.id FROM Prerequisite p WHERE p.subject.id = :subjectId")
                                        .setParameter("subjectId", currentId)
                                        .getResultList();

        // Recursively check each prerequisite
        for (UUID prereqId : nextPrereqs) if (hasCircularDependency(session, startId, prereqId)) return true;

        return false;
    }
}
