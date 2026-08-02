package bg.whiteswallow.manager.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class LessonEnrollmentEvent extends ApplicationEvent {

    private final UUID userId;
    private final UUID slotId;

    public LessonEnrollmentEvent(Object source, UUID userId, UUID slotId) {
        super(source);
        this.userId = userId;
        this.slotId = slotId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSlotId() {
        return slotId;
    }
}
