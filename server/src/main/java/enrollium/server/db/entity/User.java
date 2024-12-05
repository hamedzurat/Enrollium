package enrollium.server.db.entity;

import at.favre.lib.crypto.bcrypt.BCrypt;
import enrollium.server.db.entity.types.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class User extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "UserType cannot be null")
    private UserType type;
    //
    @Column(unique = true, nullable = false)
    @Email(message = "Invalid email format")
    @NotNull(message = "Email cannot be null")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 64, message = "Email must not exceed 64 characters")
    private String   email;
    //
    @Column(nullable = false)
    @NotNull(message = "Name cannot be null")
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String   name;
    //
    @Column(nullable = false, length = 60)  // BCrypt hash size is 60
    @NotNull(message = "Password cannot be null")
    @NotBlank(message = "Password cannot be blank")
    private String   password;

    public void setPassword(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (plainPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        this.password = BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    public boolean verifyPassword(String plainPassword) {
        return plainPassword != null &&
               BCrypt.verifyer().verify(plainPassword.toCharArray(), this.password).verified;
    }
}
