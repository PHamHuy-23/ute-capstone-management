package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ute.capstone.entity.Submission;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByGroupIdOrderBySubmittedAtDesc(Long groupId);

    long countByGroupId(Long groupId);

}
