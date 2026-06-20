package bg.whiteswallow.manager.model.entity.course;

import bg.whiteswallow.manager.model.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lesson_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private LocalDateTime startTime;


    @Column(nullable = false)
    private int maxCapacity;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "slot_enrolled_users",
            joinColumns = @JoinColumn(name = "slot_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> enrolledUsers = new ArrayList<>();

    public boolean hasUser(UUID userId) {
        if (userId == null) return false;
        return enrolledUsers.stream().anyMatch(u -> u.getId().equals(userId));
    }
}