package com.example.applib.repository;

import com.example.applib.entity.TurboS3Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurboS3ConfigRepository extends JpaRepository<TurboS3Config, Long> {
}
