package org.example.educheck.init;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.example.educheck.domain.course.entity.CourseStatus;
import org.example.educheck.domain.member.entity.Role;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DummyDataGeneratorService {

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker = new Faker(new Locale("ko"));

    private final int CAMPUS_COUNT = 5;
    private final int COURSE_COUNT = 100;
    private final int STUDENTS_PER_COURSE = 50;

    public void generateDummyData() {

//        insertCampuses(CAMPUS_COUNT);
//        List<Long> courseIds = insertCourses(COURSE_COUNT);
//        insertLectures(courseIds);
//        List<Long> memberIds = insertMembers(COURSE_COUNT * STUDENTS_PER_COURSE);
    }

    private List<Long> insertMembers(int count) {
        String sql = "INSERT INTO member (email, name, password, phone_number, role) VALUES (?, ?, ?, ?, ?)";
        List<Long> insertedIds = new ArrayList<>();

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                String email = faker.internet().emailAddress();
                String name = faker.name().fullName();
                String password = faker.internet().password();
                String phoneNumber = faker.phoneNumber().phoneNumber();

                ps.setString(1, email);
                ps.setString(2, name);
                ps.setString(3, password);
                ps.setString(4, phoneNumber);
                ps.setString(5, Role.STUDENT.toString());
            }

            public int getBatchSize() {
                return count;
            }
        });

        insertedIds.addAll(jdbcTemplate.queryForList(
                "SELECT id FROM member ORDER BY id DESC LIMIT "+ count, Long.class
        ));
        Collections.reverse(insertedIds);

        return insertedIds;

    }

    private void insertCampuses(int count) {
        String sql = "INSERT INTO campus (name, contact, gpsx, gpsy) VALUES (?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                String name = faker.university().name();
                String contact = faker.phoneNumber().phoneNumber();
                String gpsx = faker.address().longitude();
                String gpsy = faker.address().latitude();

                ps.setString(1, name);
                ps.setString(2, contact);
                ps.setDouble(3, Double.parseDouble(gpsx));
                ps.setDouble(4, Double.parseDouble(gpsy));
            }

            public int getBatchSize() {
                return count;
            }
        });
    }

    private List<Long> insertCourses(int count) {
        List<Long> campusIds = jdbcTemplate.queryForList("SELECT id FROM campus", Long.class);
        if (campusIds.isEmpty()) throw new IllegalArgumentException("캠퍼스가 존재해야 강좌를 넣을 수 있습니다.");

        List<Long> insertedIds = new ArrayList<>();

        String sql = "INSERT INTO course (name, status, start_date, end_date, campus_id) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                String name = faker.educator().course();

                LocalDate startDate = faker.timeAndDate().past(60, TimeUnit.DAYS)
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                LocalDate endDate = startDate.plusWeeks(faker.random().nextInt(4, 12));

                LocalDate today = LocalDate.now();
                CourseStatus status;
                if (today.isBefore(startDate)) {
                    status = CourseStatus.PENDING;
                } else if (!today.isAfter(endDate)) {
                    status = CourseStatus.PROCESSING;
                } else {
                    status = CourseStatus.FINISH;
                }

                Long campusId = campusIds.get(faker.random().nextInt(campusIds.size()));

                ps.setString(1, name);
                ps.setString(2, String.valueOf(status));

                ps.setDate(3, java.sql.Date.valueOf(startDate));
                ps.setDate(4, java.sql.Date.valueOf(endDate));
                ps.setLong(5, campusId);
            }

            public int getBatchSize() {
                return count;
            }
        });

        insertedIds.addAll(jdbcTemplate.queryForList(
                "SELECT id FROM course ORDER BY id DESC LIMIT " + count, Long.class
        ));
        Collections.reverse(insertedIds);

        return insertedIds;
    }

    private void insertLectures(List<Long> courseIds) {
        String query = "SELECT id, start_date, end_date FROM course";
        List<Map<String, Object>> courses = jdbcTemplate.queryForList(query);

        String insertSql  = "INSERT INTO lecture (date, start_time, end_time, session, course_id, title) VALUES (?, ?, ?, ?, ?, ?)";

        for (Map<String, Object> course :courses) {
            Long courseId = (Long) course.get("id");
            LocalDate start = ((java.sql.Date) course.get("start_date")).toLocalDate();
            LocalDate end = ((java.sql.Date) course.get("end_date")).toLocalDate();

            List<LocalDate> allDates = start.datesUntil(end.plusDays(1))
                    .filter(date -> date.getDayOfWeek().getValue() <= 5)
                    .collect(Collectors.toList());

            Collections.shuffle(allDates);
            int lectureCount = faker.random().nextInt(5, 10);
            List<LocalDate> lectureDays = allDates.stream()
                    .limit(Math.min(lectureCount, allDates.size()))
                    .sorted()
                    .collect(Collectors.toList());

            AtomicInteger session = new AtomicInteger(1);

            for (LocalDate lectureDate : lectureDays) {
                LocalTime startTime = LocalTime.of(faker.random().nextInt(9, 20), 0);
                LocalTime endTime = startTime.plusHours(6);
                String title = faker.educator().subjectWithNumber();

                jdbcTemplate.update(insertSql, ps -> {
                    ps.setDate(1, java.sql.Date.valueOf(lectureDate));
                    ps.setTime(2, java.sql.Time.valueOf(startTime));
                    ps.setTime(3, java.sql.Time.valueOf(endTime));
                    ps.setInt(4, session.getAndIncrement());
                    ps.setLong(5, courseId);
                    ps.setString(6, title);
                });
            }
        }

    }

//    private List<Long> insertMembers(int count) {
//        String sql =
//    }
}
