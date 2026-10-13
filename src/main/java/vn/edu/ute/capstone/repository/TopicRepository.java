package vn.edu.ute.capstone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.ute.capstone.entity.Topic;
import vn.edu.ute.capstone.entity.TopicStatus;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    boolean existsByPeriodId(Long periodId);

    long countByPeriodId(Long periodId);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Topic t WHERE t.id = :id")
    java.util.Optional<Topic> findByIdForUpdate(@Param("id") Long id);

    List<Topic> findByPeriodId(Long periodId);

    List<Topic> findByDepartmentId(Long departmentId);

    List<Topic> findByDepartmentIdAndPeriodId(Long departmentId, Long periodId);

    @Query("SELECT t FROM Topic t JOIN t.advisors a WHERE a.lecturer.id = :lecturerId ORDER BY t.createdAt DESC")
    List<Topic> findByLecturerId(Long lecturerId);

    @Query("SELECT t FROM Topic t JOIN t.advisors a WHERE a.lecturer.id = :lecturerId AND t.period.id = :periodId ORDER BY t.createdAt DESC")
    List<Topic> findByLecturerIdAndPeriodId(Long lecturerId, Long periodId);

    // PHÂN TRANG (PAGINATION)
    Page<Topic> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<Topic> findByDepartmentIdAndPeriodId(Long departmentId, Long periodId, Pageable pageable);

    Page<Topic> findByPeriodIdAndStatusIn(Long periodId, List<TopicStatus> statuses, Pageable pageable);

    Page<Topic> findByStatusIn(List<TopicStatus> statuses, Pageable pageable);

    @Query("SELECT t FROM Topic t JOIN t.advisors a WHERE a.lecturer.id = :lecturerId ORDER BY t.createdAt DESC")
    Page<Topic> findByLecturerId(@Param("lecturerId") Long lecturerId, Pageable pageable);

    @Query("SELECT t FROM Topic t JOIN t.advisors a WHERE a.lecturer.id = :lecturerId AND t.period.id = :periodId ORDER BY t.createdAt DESC")
    Page<Topic> findByLecturerIdAndPeriodId(@Param("lecturerId") Long lecturerId, @Param("periodId") Long periodId, Pageable pageable);

    Page<Topic> findAll(Pageable pageable);

    Page<Topic> findByPeriodId(Long periodId, Pageable pageable);

    @Query("SELECT d.name, COUNT(t) FROM Topic t JOIN t.department d GROUP BY d.id, d.name")
    List<Object[]> countTopicsGroupByDepartment();

    @Query("SELECT t.status, COUNT(t) FROM Topic t GROUP BY t.status")
    List<Object[]> countTopicsGroupByStatus();
}

