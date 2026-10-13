package vn.edu.ute.capstone.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.ute.capstone.dto.SubmissionDTO;
import vn.edu.ute.capstone.entity.Submission;
import vn.edu.ute.capstone.entity.User;

import java.util.List;

public interface SubmissionService {

    Submission submitReport(SubmissionDTO dto, MultipartFile file, User currentLeader);

    List<Submission> getSubmissionsByGroup(Long groupId, User currentUser);

    Resource loadReportFile(Long submissionId, User currentUser);

    Submission findById(Long submissionId);

}
