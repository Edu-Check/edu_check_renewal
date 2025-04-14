-- 코스
INSERT INTO course (id, name, start_date, end_date)
VALUES (1001, 'Spring Boot 기초','2025-04-10', '2025-04-15');

-- 강의 3개
INSERT INTO lecture (id, course_id, session, date, start_time, end_time, title)
VALUES
(2001, 1001, 1, '2025-04-10', '10:00:00', '12:00:00', 'Intro'),
(2002, 1001, 2, '2025-04-11', '10:00:00', '12:00:00', 'DI & IoC'),
(2003, 1001, 3, '2025-04-12', '10:00:00', '12:00:00', 'JPA Basics'),
(2004, 1001, 4, '2025-04-13', '10:00:00', '12:00:00', 'JPA Basics'),
(2005, 1001, 5, '2025-04-14', '10:00:00', '12:00:00', 'JPA Basics'),
(2006, 1001, 6, '2025-04-15', '10:00:00', '12:00:00', 'JPA Basics');

-- 멤버 (학생 정보)
INSERT INTO member (id, name, email, phone_number)
VALUES
(3001, '홍길동', 'hong@test.com', '010-1234-5678');

-- 학생
INSERT INTO student (id, member_id, course_participation_status)
VALUES
(4001, 3001, 'T');

-- 등록
INSERT INTO registration (id, student_id, course_id, completion_date, drop_date) VALUES
(5001, 4001, 1001, NULL, NULL);

-- 출결 정보 (지각 2, 조퇴 1, 결석1, 미출석2)
INSERT INTO attendance (id, student_id, lecture_id, check_in_timestamp, check_out_timestamp, attendance_status) VALUES
(6001, 4001, 2001, '2024-04-10 10:01:00', '2024-04-10 12:00:00', 'EARLY_LEAVE'),
(6002, 4001, 2002, '2024-04-11 10:20:00', '2024-04-11 12:00:00', 'LATE'),
(6003, 4001, 2003, '2024-04-12 10:20:00', '2024-04-11 11:00:00', 'EARLY_LEAVE'),
(6004, 4001, 2004, '2024-04-12 10:00:00', '2024-04-11 12:00:00', 'ATTENDANCE');

