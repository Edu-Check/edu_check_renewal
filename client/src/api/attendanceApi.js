import apiInstance from './instance/apiInstance';

export const attendanceApi = {
  submitAttendance: async (latitude, longitude) => {
    const response = await apiInstance.post(
      '/checkin',
      {
        longitude,
        latitude,
      },
      {
        withCredentials: true,
      },
    );
    return response.data;
  },
  submitCheckOut: async (latitude, longitude) => {
    const response = await apiInstance.patch(
      '/checkout',
      {
        longitude,
        latitude,
      },
      {
        withCredentials: true,
      },
    );
    return response.data;
  },
  getAbsenceAttendanceAndRate: async (courseId) => {
    const response = await apiInstance.get(
      `/my/course/${courseId}/attendances/stats`,
      {},
      {
        withCredentials: true,
      },
    );
    return response.data;
  },
  getAttendanceRecords: async (courseId, page = 0, size = 10) => {
    const response = await apiInstance.get(
      `/my/courses/${courseId}/attendances?page=${page}&size=${size}`,
      {},
      {
        withCredentials: true,
      },
    );
    return response.data;
  },
  getAttendanceRecordsByYearMonth: async (courseId, year, month, page = 0, size = 10) => {
    const response = await apiInstance.get(
      `/my/courses/${courseId}/attendances?year=${year}&month=${month}&page=${page}&size=${size}`,
      {},
      {
        withCredentials: true,
      },
    );
    return response.data;
  },

  // STAFF
  getTodayAttendances: async (courseId) => {
    const response = await apiInstance.get(`/courses/${courseId}/attendances/today`);
    return response.data.data;
  },

  getStudentAttendances: async (courseId, studentId, page) => {
    const response = await apiInstance.get(
      `/courses/${courseId}/students/${studentId}/attendances?page=${page}`,
    );
    return response;
  },

  getStudentAttendanceSheet: async (courseId, memberId) => {
    const response = await apiInstance.get(`/courses/${courseId}/members/${memberId}`, {
      baseURL: import.meta.env.VITE_APP_URL,
    });
    return response;
  },
};
