package bg.whiteswallow.manager.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstructorDashboardDTO {
    private long myCoursesCount;
    private long upcomingSlotsCount;
    private long myStudentsCount;
    private BigDecimal pendingAmount;
}
