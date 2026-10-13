package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.ute.capstone.entity.Council;

import java.util.List;

public interface CouncilRepository extends JpaRepository<Council, Long> {

    List<Council> findByPeriodId(Long periodId);

    @Query("SELECT c FROM Council c JOIN c.members m WHERE m.lecturer.id = :lecturerId ORDER BY c.defenseDate ASC")
    List<Council> findByLecturerId(Long lecturerId);

    @Query("SELECT c FROM Council c JOIN c.assignedTopics t WHERE t.id = :topicId")
    List<Council> findByTopicId(Long topicId);

}
