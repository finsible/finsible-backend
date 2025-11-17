package org.finsible.backend.repository;

import org.finsible.backend.entity.CreditCardDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCardDetailRepository extends JpaRepository<CreditCardDetail, Long> {
}
