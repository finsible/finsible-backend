package org.finsible.backend.repository;

import org.finsible.backend.entity.LoanDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanDetailRepository extends JpaRepository<LoanDetail, Long> {
}
