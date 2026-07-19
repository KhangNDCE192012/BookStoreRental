package vn.edu.fpt.bookstore.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.bookstore.entity.ActivityLog;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.repository.ActivityLogRepository;

import java.util.List;

@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public void log(User actor, String action, String entityType, String entityId, String details) {
        ActivityLog log = new ActivityLog();
        log.setActor(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        activityLogRepository.save(log);
    }

    @Transactional
    public void log(User actor, String action, String entityType, String entityId, String details, HttpServletRequest request) {
        ActivityLog log = new ActivityLog();
        log.setActor(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(request == null ? null : request.getRemoteAddr());
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> latest() {
        return activityLogRepository.findTop200ByOrderByCreatedAtDesc();
    }
}
