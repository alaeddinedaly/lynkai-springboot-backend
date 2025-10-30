package com.lynkai.repository;

import com.lynkai.model.ActivityLog;
import com.lynkai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // Optional: fetch logs by user if needed
    List<ActivityLog> findByUser(User user);

    // Optional: fetch logs by action type
    List<ActivityLog> findByActionType(String actionType);
}
