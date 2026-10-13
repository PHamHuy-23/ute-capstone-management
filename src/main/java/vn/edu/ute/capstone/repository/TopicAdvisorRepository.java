package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ute.capstone.entity.TopicAdvisor;
import vn.edu.ute.capstone.entity.TopicAdvisorId;

import java.util.List;

public interface TopicAdvisorRepository extends JpaRepository<TopicAdvisor, TopicAdvisorId> {

    List<TopicAdvisor> findByTopicId(Long topicId);

    List<TopicAdvisor> findByLecturerId(Long lecturerId);

    boolean existsByTopicIdAndLecturerId(Long topicId, Long lecturerId);

}
