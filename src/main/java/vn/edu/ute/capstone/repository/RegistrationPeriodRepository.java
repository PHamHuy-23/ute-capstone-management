package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ute.capstone.entity.PeriodStatus;
import vn.edu.ute.capstone.entity.PeriodType;
import vn.edu.ute.capstone.entity.RegistrationPeriod;

import java.util.List;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {

    List<RegistrationPeriod> findByStatus(PeriodStatus status);

    List<RegistrationPeriod> findByPeriodType(PeriodType periodType);

    List<RegistrationPeriod> findAllByOrderByCreatedAtDesc();

}
