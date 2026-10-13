package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.ute.capstone.entity.Evaluation;
import vn.edu.ute.capstone.entity.EvaluationType;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByTopicId(Long topicId);

    List<Evaluation> findByEvaluatorIdOrderByCreatedAtDesc(Long evaluatorId);

    List<Evaluation> findByTopicIdAndEvaluationType(Long topicId, EvaluationType evaluationType);

    Optional<Evaluation> findByTopicIdAndEvaluatorIdAndEvaluationType(Long topicId, Long evaluatorId, EvaluationType evaluationType);

    boolean existsByTopicIdAndEvaluatorIdAndEvaluationType(Long topicId, Long evaluatorId, EvaluationType evaluationType);

    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.topic.id = :topicId")
    Double calculateAverageScoreByTopicId(Long topicId);

}
