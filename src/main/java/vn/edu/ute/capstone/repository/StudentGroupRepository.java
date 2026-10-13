package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ute.capstone.entity.StudentGroup;

import java.util.List;
import java.util.Optional;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {

    boolean existsByGroupCode(String groupCode);

    boolean existsByTopicId(Long topicId);

    Optional<StudentGroup> findByTopicId(Long topicId);

    List<StudentGroup> findByPeriodId(Long periodId);

}
