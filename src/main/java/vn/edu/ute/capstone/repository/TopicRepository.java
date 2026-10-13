package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ute.capstone.entity.Topic;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    boolean existsByPeriodId(Long periodId);

    long countByPeriodId(Long periodId);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Topic> findByPeriodId(Long periodId);

    List<Topic> findByDepartmentId(Long departmentId);

    List<Topic> findByDepartmentIdAndPeriodId(Long departmentId, Long periodId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Topic t JOIN t.advisors a WHERE a.lecturer.id = :lecturerId ORDER BY t.createdAt DESC")
    List<Topic> findByLecturerId(Long lecturerId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Topic t JOIN t.advisors a WHERE a.lecturer.id = :lecturerId AND t.period.id = :periodId ORDER BY t.createdAt DESC")
    List<Topic> findByLecturerIdAndPeriodId(Long lecturerId, Long periodId);

}

