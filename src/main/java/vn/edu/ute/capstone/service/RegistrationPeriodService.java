package vn.edu.ute.capstone.service;

import vn.edu.ute.capstone.dto.RegistrationPeriodDTO;
import vn.edu.ute.capstone.entity.PeriodStatus;
import vn.edu.ute.capstone.entity.RegistrationPeriod;

import java.util.List;

public interface RegistrationPeriodService {

    List<RegistrationPeriod> getAllPeriods();

    RegistrationPeriod getPeriodById(Long id);

    RegistrationPeriod createPeriod(RegistrationPeriodDTO dto);

    RegistrationPeriod updatePeriod(Long id, RegistrationPeriodDTO dto);

    void updateStatus(Long id, PeriodStatus newStatus);

    void deletePeriod(Long id);

    vn.edu.ute.capstone.dto.StudentEligibilityDTO checkStudentEligibility(vn.edu.ute.capstone.entity.User student, RegistrationPeriod period);

}

