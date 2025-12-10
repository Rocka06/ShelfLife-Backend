package com.shelflife.project.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shelflife.project.model.InvalidJwt;

import jakarta.transaction.Transactional;


@Repository
public interface InvalidJwtRepository extends JpaRepository<InvalidJwt, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM InvalidJwt ij WHERE ij.created_at < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);

    Optional<InvalidJwt> findByToken(String token);
}
