package com.lynkai.service;

import com.lynkai.model.ActivityLog;
import com.lynkai.model.User;
import com.lynkai.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void saveLog(ActivityLog log) {
        activityLogRepository.save(log);
    }

    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAll();
    }

    public List<ActivityLog> getLogsByUser(User user) {
        return activityLogRepository.findByUser(user);
    }

    public void deleteLog(Long id) {
        activityLogRepository.deleteById(id);
    }
}
