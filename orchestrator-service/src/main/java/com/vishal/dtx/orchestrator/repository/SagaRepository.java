package com.vishal.dtx.orchestrator.repository;

import com.vishal.dtx.orchestrator.domain.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaRepository extends JpaRepository<SagaInstance, String> {
}
