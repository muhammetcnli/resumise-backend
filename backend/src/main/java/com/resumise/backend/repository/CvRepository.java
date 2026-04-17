package com.resumise.backend.repository;

import com.resumise.backend.model.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CvRepository extends JpaRepository<Cv, Long> {

    List<Cv> findAllByUserIdOrderByIdDesc(Long userId);

    Optional<Cv> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("update Cv c set c.isDefault = false where c.user.id = :userId")
    void clearDefaultByUserId(@Param("userId") Long userId);
}
