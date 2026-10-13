package vn.edu.ute.capstone.service;

import vn.edu.ute.capstone.dto.TopicApprovalDTO;
import vn.edu.ute.capstone.dto.TopicProposalDTO;
import vn.edu.ute.capstone.entity.Topic;
import vn.edu.ute.capstone.entity.User;

import java.util.List;

public interface TopicService {

    Topic createTopic(TopicProposalDTO dto, User currentLecturer);

    Topic updateTopic(Long topicId, TopicProposalDTO dto, User currentLecturer);

    void deleteTopic(Long topicId, User currentLecturer);

    Topic approveOrRejectTopic(TopicApprovalDTO dto, User approver);

    Topic findById(Long id);

    List<Topic> getTopicsForLecturer(Long lecturerId, Long periodId);

    List<Topic> getTopicsForDepartment(Long departmentId, Long periodId);

    List<Topic> getAllTopics(Long periodId);

}
