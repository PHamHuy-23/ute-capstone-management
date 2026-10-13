package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ute.capstone.entity.CouncilMember;
import vn.edu.ute.capstone.entity.CouncilMemberId;

import java.util.List;

public interface CouncilMemberRepository extends JpaRepository<CouncilMember, CouncilMemberId> {

    List<CouncilMember> findByCouncilId(Long councilId);

    List<CouncilMember> findByLecturerId(Long lecturerId);

    boolean existsByCouncilIdAndLecturerId(Long councilId, Long lecturerId);

    long countByCouncilId(Long councilId);

}
