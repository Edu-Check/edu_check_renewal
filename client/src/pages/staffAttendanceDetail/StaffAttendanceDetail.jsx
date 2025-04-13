import React, { useEffect, useState } from 'react';
import styles from './StaffAttendanceDetail.module.css';
import DashBoardItem from '../../components/dashBoardItem/DashBoardItem';
import BaseListItem from '../../components/listItem/baseListItem/BaseListItem';
import DataBoard from '../../components/dataBoard/DataBoard';
import { attendanceApi } from '../../api/attendanceApi';
import { useParams } from 'react-router-dom';
import { useSelector } from 'react-redux';
import PaginationComponent from '../../components/paginationComponent/PaginationComponent';

export default function StaffAttendanceDetail() {
  const { courseId, studentId } = useParams();
  const accessToken = useSelector((state) => state.auth.accessToken);
  const [studentAttendance, setStudentAttendance] = useState({
    studentName: '',
    studentPhoneNumber: '',
    studentEmail: '',
    attendanceRecords: {
      attendanceRecords: [],
      pageInfo: {
        pageNumber: 0,
        totalPages: 1,
      },
    },
    attendanceRate: {},
  });
  const [currentPage, setCurrentPage] = useState(1);

  // 출석 상태 텍스트 변환 함수
  function getAttendanceStatusText(status) {
    const statusMap = {
      ATTENDANCE: '출석',
      LATE: '지각',
      EARLY_LEAVE: '조퇴',
      ABSENCE: '결석',
      NOT_CHECKIN: '미출석',
    };
    return statusMap[status] || status;
  }

  useEffect(() => {
    const fetchStudentAttendance = async () => {
      if (!courseId || !studentId || !accessToken) return;
      try {
        const response = await attendanceApi.getStudentAttendances(
          courseId,
          studentId,
          currentPage - 1,
        );
        const studentAttendanceData = response.data.data;
        console.log(studentAttendanceData);
        setStudentAttendance(studentAttendanceData);
      } catch (error) {
        console.error('수강생 출결 현황 조회 실패:', error);
      }
    };

    fetchStudentAttendance();
  }, [courseId, studentId, accessToken, currentPage]);

  const handlePageChange = (page) => {
    setCurrentPage(page);
  };

  return (
    <div className={styles.container}>
      <div className={styles.dashboardContainer}>
        <DashBoardItem>
          <div className={styles.userInfoContainer}>
            <div className={styles.userInfo}>
              <h2 className={styles.userName}>{studentAttendance.studentName}</h2>
              <p className={styles.userPhone}>{studentAttendance.studentPhoneNumber}</p>
            </div>
          </div>
          <div className={styles.databoardContainer}>
            <DataBoard
              title="금일 기준 출석률"
              data={`${Math.round(studentAttendance.attendanceRate.attendanceRateUntilToday) || 0}%`}
            />
            <DataBoard
              title="전체 출석률"
              data={`${Math.round(studentAttendance.attendanceRate.totalAttendanceRate) || 0}%`}
            />
            <DataBoard
              title="과정 진행률"
              data={`${Math.round(studentAttendance.attendanceRate.courseProgressRate) || 0}%`}
            />
          </div>
        </DashBoardItem>
      </div>

      <div className={styles.contentWrapper}>
        <div className={styles.listContainer}>
          {studentAttendance.attendanceRecords.attendanceRecords &&
            studentAttendance.attendanceRecords.attendanceRecords.map((item, index) => (
              <div key={index}>
                <BaseListItem
                  content={item.lectureDate}
                  //TODO: API에서 lectureTitle 뽑아 온 후 넣어주기
                  lectureTitle={item.lectureSession}
                  tagTitle={getAttendanceStatusText(item.attendanceStatus)}
                />
              </div>
            ))}
        </div>
        {/* 페이지네이션 컴포넌트 */}
        <div className={styles.paginationWrapper}>
          <PaginationComponent
            totalPages={studentAttendance.attendanceRecords.pageInfo.totalPages}
            onPageChange={handlePageChange}
          />
        </div>
      </div>
    </div>
  );
}
