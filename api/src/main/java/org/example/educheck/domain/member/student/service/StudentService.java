package org.example.educheck.domain.member.student.service;

import lombok.RequiredArgsConstructor;
import org.example.educheck.domain.member.student.entity.Student;
import org.example.educheck.domain.member.student.repository.StudentRepository;
import org.example.educheck.domain.registration.repository.RegistrationRepository;
import org.example.educheck.global.common.exception.custom.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;

    public Student getEnrolledStudent(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 학생이 존재하지 않습니다."));
        student.validateEnrolledInCourse(courseId, registrationRepository);
        return student;
    }

}
