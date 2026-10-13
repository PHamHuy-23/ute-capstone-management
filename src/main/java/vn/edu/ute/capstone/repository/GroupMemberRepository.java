package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.ute.capstone.entity.GroupMember;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByStudentIdAndPeriodId(Long studentId, Long periodId);

    Optional<GroupMember> findByStudentIdAndPeriodId(Long studentId, Long periodId);

    List<GroupMember> findByGroupId(Long groupId);

    long countByGroupId(Long groupId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.student.id = :studentId ORDER BY gm.group.createdAt DESC")
    List<GroupMember> findAllByStudentId(Long studentId);

}
