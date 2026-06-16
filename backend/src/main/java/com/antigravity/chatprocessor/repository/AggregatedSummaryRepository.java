package com.antigravity.chatprocessor.repository;

import com.antigravity.chatprocessor.model.AggregatedSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AggregatedSummaryRepository extends JpaRepository<AggregatedSummary, String> {
    
    /**
     * Find all summaries ordered by timestamp descending.
     */
    List<AggregatedSummary> findAllByOrderByTimestampDesc();
}
