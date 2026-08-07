package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.dashboard.AdminDashboardDTO;
import bg.whiteswallow.manager.model.dto.dashboard.InstructorDashboardDTO;
import bg.whiteswallow.manager.model.dto.dashboard.PublicDashboardDTO;

import java.util.UUID;

public interface DashboardService {
    PublicDashboardDTO getPublicDashboard();
    AdminDashboardDTO getAdminDashboard();
    InstructorDashboardDTO getInstructorDashboard(UUID instructorId);
}
