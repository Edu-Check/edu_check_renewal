package org.example.educheck.domain.course.service;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.course.entity.Course;
import org.example.educheck.domain.course.repository.CourseRepository;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public Course getValidCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 강좌가 존재하지 않습니다."));
    }
}
