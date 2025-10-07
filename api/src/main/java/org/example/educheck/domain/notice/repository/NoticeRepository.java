package org.example.educheck.domain.notice.repository;

import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByCourseOrderByCreatedAtDesc(Course course);

    Optional<Notice> findByIdAndCourse_Id(Long noticeId, Long courseId);
}
