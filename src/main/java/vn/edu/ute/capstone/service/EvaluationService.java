package vn.edu.ute.capstone.service;

import vn.edu.ute.capstone.dto.CouncilCreateDTO;
import vn.edu.ute.capstone.dto.EvaluationDTO;
import vn.edu.ute.capstone.entity.Council;
import vn.edu.ute.capstone.entity.Evaluation;
import vn.edu.ute.capstone.entity.User;

import java.util.List;

public interface EvaluationService {

    Council createCouncil(CouncilCreateDTO dto, User admin);

    Council updateCouncil(Long councilId, CouncilCreateDTO dto, User admin);

    void deleteCouncil(Long councilId, User admin);

    Evaluation submitEvaluation(EvaluationDTO dto, User evaluator);

    List<Council> getCouncilsByPeriod(Long periodId);

    List<Council> getCouncilsByLecturer(Long lecturerId);

    List<Evaluation> getEvaluationsByTopic(Long topicId, User currentUser);

    List<Evaluation> getEvaluationsByEvaluator(Long evaluatorId);

    Double getAverageScore(Long topicId);

    Council findCouncilById(Long councilId);

}
