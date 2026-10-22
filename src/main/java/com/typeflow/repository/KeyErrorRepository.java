package com.typeflow.repository;

import com.typeflow.entity.KeyError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyErrorRepository extends JpaRepository<KeyError, Long> {

    List<KeyError> findByTypingResultId(Long typingResultId);

    @Query("SELECT ke FROM KeyError ke JOIN ke.typingResult tr " +
           "WHERE tr.user.id = :userId")
    List<KeyError> findByUserId(@Param("userId") Long userId);
}
