package org.finsible.backend.repository;

import org.finsible.backend.entity.DebitCardDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebitCardDetailsRepository extends JpaRepository<DebitCardDetail, Long> {
}
