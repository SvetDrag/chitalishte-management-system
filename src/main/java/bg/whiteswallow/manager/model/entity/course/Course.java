package bg.whiteswallow.manager.model.entity.course;

import bg.whiteswallow.manager.model.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private BigDecimal groupPricePerLesson;

    private BigDecimal individualPricePerLesson;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

    public boolean offers(CourseType type) {
        return getPriceFor(type) != null;
    }

    public BigDecimal getPriceFor(CourseType type) {
        return type == CourseType.INDIVIDUAL ? individualPricePerLesson : groupPricePerLesson;
    }
}
