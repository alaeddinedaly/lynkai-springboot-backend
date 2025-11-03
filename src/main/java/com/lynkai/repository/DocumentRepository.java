package com.lynkai.repository;

import com.lynkai.model.Document;
import com.lynkai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByUser(User user);

    List<Document> findAllByUserId(Long userId);
}
