package vn.edu.ute.capstone.service;

import vn.edu.ute.capstone.dto.CreateGroupDTO;
import vn.edu.ute.capstone.entity.StudentGroup;
import vn.edu.ute.capstone.entity.Topic;
import vn.edu.ute.capstone.entity.User;

import java.util.List;
import java.util.Optional;

public interface StudentGroupService {

    StudentGroup createGroup(CreateGroupDTO dto, User leader);

    StudentGroup addMember(Long groupId, String studentUsername, User currentLeader);

    void removeMember(Long groupId, Long memberId, User currentLeader);

    void disbandGroup(Long groupId, User currentLeader);

    StudentGroup registerTopic(Long groupId, Long topicId, User currentLeader);

    void cancelTopicRegistration(Long groupId, User currentLeader);

    Optional<StudentGroup> getGroupByStudentAndPeriod(Long studentId, Long periodId);

    List<StudentGroup> getGroupsByStudent(Long studentId);

    StudentGroup findById(Long groupId);

    List<Topic> getAvailableTopicsForStudent(Long periodId);

}
