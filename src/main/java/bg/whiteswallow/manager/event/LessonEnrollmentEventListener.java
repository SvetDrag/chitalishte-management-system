package bg.whiteswallow.manager.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LessonEnrollmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(LessonEnrollmentEventListener.class);

    @EventListener
    public void onLessonEnrollment(LessonEnrollmentEvent event) {
        log.info("Notification: user {} confirmed for lesson slot {}", event.getUserId(), event.getSlotId());
    }
}
