package com.bervan.logging;

import com.bervan.history.model.BaseRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;

public interface LogRepository extends BaseRepository<LogEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM logs_owners
            WHERE log_entity_id IN (
                SELECT id FROM my_tools_db.logs WHERE timestamp < :cutoff
            )
            """, nativeQuery = true)
    void deleteOwnersByOldLogs(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Transactional
    @Query("DELETE FROM LogEntity l WHERE l.timestamp < :cutoff")
    void deleteOldLogs(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT DISTINCT applicationName FROM LogEntity")
     Set<String> findAllApplicationNames();
}