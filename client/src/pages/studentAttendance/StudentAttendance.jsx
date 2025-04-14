import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import styles from './StudentAttendance.module.css';
import DashBoardItem from '../../components/dashBoardItem/DashBoardItem';
import DataBoard from '../../components/dataBoard/DataBoard';
import ProgressBar from '../../components/progressBar/ProgressBar';
import Calendar from '../../components/calendar/Calendar';
import { attendanceApi } from '../../api/attendanceApi';
import MainButton from '../../components/buttons/mainButton/MainButton';

export default function StudentAttendance() {
  const courseId = useSelector((state) => state.auth.user.courseId);

  const [attendanceStats, setAttendanceStats] = useState({
    attendanceRate: 0,
    lateCount: 0,
    earlyLeaveCount: 0,
    absenceCount: 0,
    adjustedAbsenceCount: 0,
    startDate: ' ',
    endDate: ' ',
  });

  const [attendanceCalendarData, setAttendanceCalendarData] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const currentDate = new Date();
  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth() + 1;

  const [selectedYear, setSelectedYear] = useState(currentYear);
  const [selectedMonth, setSelectedMonth] = useState(currentMonth);

  const handleMonthChange = (year, month) => {
    setSelectedYear(year);
    setSelectedMonth(month);
  };

  useEffect(() => {
    const fetchAttendanceStats = async () => {
      try {
        setIsLoading(true);
        const response = await attendanceApi.getAbsenceAttendanceAndRate(courseId);
        if (response) {
          setAttendanceStats({
            attendanceRate: Math.round(response.attendanceRate) || 0,
            lateCount: response.lateCount || 0,
            earlyLeaveCount: response.earlyLeaveCount || 0,
            absenceCount: response.absenceCount || 0,
            adjustedAbsenceCount: response.adjustedAbsenceCount || 0,
            startDate: response.startDate,
            endDate: response.endDate,
          });
        }
      } catch (err) {
        console.error('출석 통계 데이터를 가져오는 중 오류 발생:', err);
        setError('출석 통계를 불러오는 데 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    if (courseId) {
      fetchAttendanceStats();
    } else {
      setIsLoading(false);
    }
  }, [courseId]);

  // 달력 월별 데이터
  useEffect(() => {
    const fetchAttendanceRecordsByYearMonth = async () => {
      try {
        setIsLoading(true);
        const response = await attendanceApi.getAttendanceRecordsByYearMonth(
          courseId,
          selectedYear,
          selectedMonth,
        );

        if (response) {
          setAttendanceCalendarData(response.records);
        }
      } catch (error) {
        const errorMessage = error.response?.data?.message ?? '오류가 발생했습니다.';
        alert(errorMessage);
      } finally {
        setIsLoading(false);
      }
    };

    if (courseId) {
      fetchAttendanceRecordsByYearMonth();
    }
  }, [courseId, selectedYear, selectedMonth]);

  // if (!courseId) return <div>코스 정보를 불러올 수 없습니다.</div>;
  // if (isLoading) return <div>로딩 중...</div>;
  // if (error) return <div>{error}</div>;

  console.log(attendanceCalendarData);
  
  return (
    <>
      <div className={styles.dashBoardItemDiv}>
        <div className={styles.attendanceStatistics}>
          <DashBoardItem width="100%">
            <div className={styles.headerContainer}>
              <p className="subTitle">출석률 {attendanceStats.attendanceRate}%</p>
              <MainButton
                title={'출석부 확인'}
                isEnable={true}
                handleClick={() => {
                  window.open('/attendanceSheet', '_blank');
                }}
              />
            </div>
            <div className={styles.progressBarBottom}>
              <ProgressBar
                value={attendanceStats.attendanceRate}
                max={100}
                startDate={attendanceStats.startDate}
                endDate={attendanceStats.endDate}
              ></ProgressBar>
            </div>
          </DashBoardItem>

          <DashBoardItem width="100%">
            <p className="subTitle">결석 현황</p>
            <div className={styles.attendanceType}>
              <DataBoard title="지각" data={`${attendanceStats.lateCount} 회`}></DataBoard>
              <DataBoard title="조퇴" data={`${attendanceStats.earlyLeaveCount} 회`}></DataBoard>
              <DataBoard title="결석" data={`${attendanceStats.absenceCount} 회`}></DataBoard>
              <DataBoard
                title="누적 결석"
                data={`${attendanceStats.adjustedAbsenceCount} 회`}
              ></DataBoard>
            </div>
          </DashBoardItem>
        </div>

        <div className={styles.attendanceCalendar}>
          <DashBoardItem width={'100%'}>
            <div className={styles.legendContainer}>
              <div className={styles.legend}>
                <div className={styles.legendItem}>
                  <span className={styles.attendanceIndicator}></span>
                  <span>출석</span>
                </div>
                <div className={styles.legendItem}>
                  <span className={styles.lateIndicator}></span>
                  <span>지각</span>
                </div>
                <div className={styles.legendItem}>
                  <span className={styles.earlyLeaveIndicator}></span>
                  <span>조퇴</span>
                </div>
                <div className={styles.legendItem}>
                  <span className={styles.absentIndicator}></span>
                  <span>결석</span>
                </div>
              </div>
            </div>
            <div className={styles.calendarWrapper}>
              <Calendar
                attendanceData={attendanceCalendarData}
                onMonthChange={handleMonthChange}
              ></Calendar>
            </div>
          </DashBoardItem>
        </div>
      </div>
    </>
  );
}
