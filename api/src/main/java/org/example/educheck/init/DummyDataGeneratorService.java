package org.example.educheck.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.commons.lang3.tuple.Pair;
import org.example.educheck.domain.course.entity.CourseStatus;
import org.example.educheck.domain.member.entity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DummyDataGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(DummyDataGeneratorService.class);
    private final JdbcTemplate jdbcTemplate;
    private final Faker faker = new Faker(new Locale("ko"));

    private final int CAMPUS_COUNT = 7;
    private final int COURSE_COUNT = 30;
    private final int STUDENTS_PER_COURSE = 50;
    private final int LECTURE_PER_COURSE = 50;

    public void generateDummyData() {

        log.info("======= 더미 데이터 insert 시작! ==========");

//        List<Long> campusIds = insertCampus();
//        List<Long> courseIds = insertCourses(campusIds);
//        insertLectures(courseIds);
        List<Long> memberIds = insertMembers();
        List<Long> studentIds = insertStudents(memberIds);
        insertRegistrations(memberIds, studentIds);

        log.info("======= 더미 데이터 insert 종료! ==========");
    }

    //테스트용
    @Transactional
    public void insertCourseWithCampus(List<Long> campusIds) {
        insertCourses(campusIds);
    }

    //SimpleJdbcInsert 방식의 경우, insert문 하나당 하나의 jdbc connection이 발생하므로 대용량의 경우 부적합
//    private List<Long> insertCampus(int count) {
//        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
//                .withTableName("campus")
//                .usingGeneratedKeyColumns("id");
//
//        List<Long> insertedIds = new ArrayList<>();
//
//        for (int i = 0; i < count; i++) {
//            Map<String, Object> parameters = new HashMap<>();
//            parameters.put("name", faker.university().name());
//            parameters.put("contact", faker.phoneNumber().phoneNumber());
//            parameters.put("gpsx", faker.address().longitude());
//            parameters.put("gpsy", faker.address().latitude());
//
//            Number key = insert.executeAndReturnKey(new MapSqlParameterSource(parameters));
//            insertedIds.add(key.longValue());
//        }
//        return insertedIds;
//    }

    // batchUpdate를 사용한 campus 삽입 (성능 개선)
//    @Transactional //private 메서드이면 안되는 이유는?
    @Transactional
    public List<Long> insertCampus() {
        String sql = "INSERT INTO campus (name, contact, gpsx, gpsy) VALUES (?, ?, ?, ?)";

        List<Map<String, Object>> campusData = new ArrayList<>();

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, faker.university().name());
                ps.setString(2, faker.phoneNumber().phoneNumber());
                ps.setDouble(3, Double.parseDouble(faker.address().longitude()));
                ps.setDouble(4, Double.parseDouble(faker.address().latitude()));
            }

            @Override
            public int getBatchSize() {
                return CAMPUS_COUNT;
            }
        });

        // 삽입된 campus ID들을 반환
        return jdbcTemplate.queryForList(
                "SELECT id FROM campus ORDER BY id DESC LIMIT " + CAMPUS_COUNT, Long.class
        );
    }

    @Transactional
    public List<Long> insertCourses(List<Long> campusIds) {

        log.info("course insert 시작!");
        if (campusIds.isEmpty()) throw new IllegalArgumentException("캠퍼스가 존재해야 강좌를 넣을 수 있습니다.");

        for (Long id : campusIds) {
            log.info("id : {}", id);
        }

        String sql = "INSERT INTO course (name, status, start_date, end_date, campus_id) VALUES (?, ?, ?, ?, ?)";

        //batchUpdate의 경우 RuntimeException 발생시 롤백이 됨! 로그가 나오지 않기 때문에, try 처리

        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    String name = "과정명" + i;

                    //현재를 기준으로 최대 60일 이전 날짜 중 랜덤으로 시작 날짜를 선정
                    LocalDate startDate = faker.timeAndDate().past(60, TimeUnit.DAYS)
                            .atZone(ZoneId.systemDefault()).toLocalDate();

                    LocalDate endDate = calculateEndDate(startDate);

                    LocalDate today = LocalDate.now();
                    CourseStatus status;

                    log.info("여기까지는 오니?");

                    if (today.isBefore(startDate)) {
                        status = CourseStatus.PENDING;
                    } else if (!today.isAfter(endDate)) {
                        status = CourseStatus.PROCESSING;
                    } else {
                        status = CourseStatus.FINISH;
                    }

                    Long campusId = campusIds.get(faker.random().nextInt(campusIds.size()));

                    log.info("Processing course #{}: name={}, status={}, dates={} to {}, campusId={}",
                            i, name, status, startDate, endDate, campusId);

                    ps.setString(1, name);
                    ps.setString(2, String.valueOf(status));

                    ps.setDate(3, java.sql.Date.valueOf(startDate));
                    ps.setDate(4, java.sql.Date.valueOf(endDate));
                    ps.setLong(5, campusId);
                }

                @Override
                public int getBatchSize() {
                    log.info("COURSE_COUNT : {}", COURSE_COUNT);
                    return COURSE_COUNT;
                }
            });
            log.info("배치 insert 성공!");

        } catch (Exception e) {
            log.error("Error setting values for course: {}", e.getMessage(), e);
            throw e;
        }

        return jdbcTemplate.queryForList(
                "SELECT id FROM course ORDER BY id DESC LIMIT " + COURSE_COUNT, Long.class
        );
    }

    private LocalDate calculateEndDate(LocalDate startDate) {
        int weekdaysCount = 0;
        LocalDate current = startDate;

        while (weekdaysCount < LECTURE_PER_COURSE) {
            DayOfWeek day = startDate.getDayOfWeek(); //요일을 표현하는 enum
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                weekdaysCount++;
            }
            current = current.plusDays(1);
        }
        return current.minusDays(1);
    }

    //상수는 클래스가 로드될 때 생성되므로 매개변수로 넘기지 않고 사용 가능
    private void insertLectures(List<Long> courseIds) {

        if (courseIds.isEmpty()) throw new IllegalArgumentException("과정이 존재해야 강의를 넣을 수 있습니다.");

        String query = "SELECT id, start_date, end_date FROM course WHERE id IN (" +
                String.join(",", Collections.nCopies(courseIds.size(), "?")) + ")";

        // query에 들어가는  IN 부분이 배열이기 때문에 toArray()로 넣음
        // 반환타입이 Map<String, Object>인 이유는 "id" : 1, "start_date" : java.sql.Date 타입 ... 형태이기 때문에!
        List<Map<String, Object>> courses = jdbcTemplate.queryForList(query, courseIds.toArray());

        String insertSql = "INSERT INTO lecture (date, start_time, end_time, session, course_id, title) VALUES (?, ?, ?, ?, ?, ?)";

        List<Object[]> lectureData = new ArrayList<>();

        for (Map<String, Object> course : courses) {
            Long courseId = (Long) course.get("id");
            LocalDate start = ((java.sql.Date) course.get("start_date")).toLocalDate();
            LocalDate end = ((java.sql.Date) course.get("end_date")).toLocalDate();

            LocalDate date = start;
            int session = 1;

            while (session <= LECTURE_PER_COURSE && !date.isAfter(end)) {
                if (date.getDayOfWeek().getValue() <= 5) { //요일을 enum -> 숫자화 (즉, 월~금 이라면)
                    lectureData.add(new Object[]{
                            java.sql.Date.valueOf(date),
                            Time.valueOf("08:00:00"),
                            Time.valueOf("20:00:00"),
                            session,
                            courseId,
                            faker.book().title()
                    });
                    session++;
                }
                date = date.plusDays(1);
            }
        }
        jdbcTemplate.batchUpdate(insertSql, lectureData);
    }

    private List<Long> insertMembers() {
        String sql = "INSERT INTO member (email, name, password, phone_number, role, birth_date) VALUES (?, ?, ?, ?, ?, ?)";
        List<Long> insertedIds = new ArrayList<>();

        List<Object[]> memberData = new ArrayList<>();

        for (int i = 0; i < COURSE_COUNT * STUDENTS_PER_COURSE; i++) {
            String baseEmail = faker.internet().emailAddress();
            String[] parts = baseEmail.split("@");

            String email = parts[0] + "_" + i + "@" + parts[1];

            String name = faker.name().fullName();
            String password = faker.internet().password();
            String phoneNumber = faker.phoneNumber().phoneNumber();

            int year = faker.number().numberBetween(1985, 2001);
            int month = faker.number().numberBetween(1, 13);
            int day = faker.number().numberBetween(1, YearMonth.of(year, month).lengthOfMonth() + 1);
            LocalDate birthday = LocalDate.of(year, month, day);

            memberData.add(new Object[]{email, name, password, phoneNumber, Role.STUDENT, java.sql.Date.valueOf(birthday)});
        }

        jdbcTemplate.batchUpdate(sql, memberData, memberData.size(), (ps, member) -> {
            ps.setString(1, (String) member[0]);
            ps.setString(2, (String) member[1]);
            ps.setString(3, (String) member[2]);
            ps.setString(4, (String) member[3]);
            ps.setString(5, (String) member[4]);
            ps.setDate(6, (java.sql.Date) member[5]);
        });

        return insertedIds;
    }

    private List<Long> insertStudents(List<Long> memberIds) {
        String sql = "INSERT INTO student (member_id, course_participation_status) VALUES (?, ?)";
        List<Long> insertedIds = new ArrayList<>();

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, memberIds.get(i));
                ps.setString(2, "T");
            }

            public int getBatchSize() {
                return memberIds.size();
            }
        });

        insertedIds.addAll(jdbcTemplate.queryForList(
                "SELECT id FROM student ORDER BY id DESC LIMIT " + memberIds.size(), Long.class
        ));
        Collections.reverse(insertedIds);

        return insertedIds;
    }

    private void insertRegistrations(List<Long> courseIds, List<Long> studentIds) {
        String sql = "INSERT INTO registration (course_id, student_id, completion_date, drop_date) VALUES (?, ?, ?, ?)";

        //1. course_id -> start_date, end_date 가져오기
        Map<Long, Pair<LocalDate, LocalDate>> courseDateMap = getCourseDates(courseIds);

        Random random = new Random();

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Long courseId = courseIds.get(i);
                Long studentId = studentIds.get(i);

                ps.setLong(1, courseId);
                ps.setLong(2, studentId);
//                completion_date, drop_date 는 course가 가지고 있는 start_date, end_date 값 사이에서 랜덤으로
//                부여하거나 그냥 null로 두거나를 랜덤으로 하고 싶음
//                비율은 null인 경우가 80%이상이면 좋겠음
                Pair<LocalDate, LocalDate> dateRange = courseDateMap.get(courseId);

//              2. 80& 확률로 null 20% 확률로 랜덤으로 날짜 부여
                double chance = random.nextDouble();

                LocalDate completionDate = null;
                LocalDate dropDate = null;

                if (chance < 0.1) {
                    dropDate = randomDateBetween(dateRange.getLeft(), dateRange.getRight());
                } else if (chance < 0.2) {
                    completionDate = randomDateBetween(dateRange.getLeft(), dateRange.getRight());
                }

                // completionDate 셋팅
                if (completionDate != null) {
                    ps.setDate(3, java.sql.Date.valueOf(completionDate));
                } else {
                    ps.setNull(3, Types.DATE);
                }

                // dropDate 셋팅
                if (dropDate != null) {
                    ps.setDate(4, java.sql.Date.valueOf(dropDate));
                } else {
                    ps.setNull(4, Types.DATE);
                }
            }

            public int getBatchSize() {
                return Math.min(courseIds.size(), studentIds.size());
            }
        });
    }

    //    두 날짜 사이의 랜덤 날짜 생성
    private LocalDate randomDateBetween(LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        long randomDay = (long) (Math.random() * (days + 1));
        return start.plusDays(randomDay);
    }


    /**
     * courseId로 start_date, end_date를 반환한다.
     * @param courseIds
     * @return
     */
    private Map<Long, Pair<LocalDate, LocalDate>> getCourseDates(List<Long> courseIds) {
//      :ids => 이름 있는 파라미터, 일반 JDBC에서 ? 대신 사용하는 방식!
        String sql = "SELECT id, start_date, end_date FROM course WHERE id IN (:ids)";
        Map<String, Object> params = Map.of("ids", courseIds); // 이름있는 파라미터 ids에 실제로 들어갈 값을 설정하는 부분!
        // 이름 있는 파라미터를 사용하는 이유 => 일반 jdbcTemplate를 사용하면 IN (?, ?, ..) ids 의 갯수맘큼 작성해줘야하는데
        // 그건 힘들고, 몇 개가 존재하는지 정확히 모르기때문에 이름있는 파라미터를 사용하면 편리함!

        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(jdbcTemplate);

        return namedJdbc.query(sql, params, rs -> {
            Map<Long, Pair<LocalDate, LocalDate>> map = new HashMap<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                LocalDate start = rs.getDate("start_date").toLocalDate();
                LocalDate end = rs.getDate("end_date").toLocalDate();
                map.put(id, Pair.of(start, end));
            }
            return map;
        });
    }




}
