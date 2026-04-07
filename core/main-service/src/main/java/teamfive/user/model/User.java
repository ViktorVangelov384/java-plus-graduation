package teamfive.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@ToString
@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "User: Поле name не может быть пустым")
    @Size(min = 2, max = 255)
    private String name;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "User: Поле email не может быть пустым")
    @Email
    @Size(min = 5, max = 255)
    private String email;
}
