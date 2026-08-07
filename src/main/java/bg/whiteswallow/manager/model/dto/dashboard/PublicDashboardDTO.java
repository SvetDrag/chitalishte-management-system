package bg.whiteswallow.manager.model.dto.dashboard;

import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.event.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicDashboardDTO {
    private List<Event> upcomingEvents;
    private List<Course> activeCourses;
}
