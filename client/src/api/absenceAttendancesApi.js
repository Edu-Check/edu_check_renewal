import apiInstance from './instance/apiInstance';

export const absenceAttendancesApi = {
  getAbsenceAttendancesByCourseId: async (courseId, page = 0) =>
    await apiInstance.get(`/course/${courseId}/absence-attendances?page=${page}`),

  processAbsenceAttendance: async (courseId, absenceAttendancedId, isApproved) =>
    await apiInstance.patch(`/course/${courseId}/absence-attendances/${absenceAttendancedId}`, {
      isApprove: isApproved,
    }),

  getAbsenceAttendance: async (courseId, absenceAttendancedId) =>
    await apiInstance.get(`/course/${courseId}/absence-attendances/${absenceAttendancedId}`),

  getAbsenceAttendanceListByStudent: async (courseId, page) =>
    await apiInstance.get(`/my/course/${courseId}/absence-attendances?page=${page}`),

  submitAbsenceAttendance: async (courseId, data) => {
    const response = await apiInstance.post(`/my/course/${courseId}/absence-attendances`, data, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response;
  },
  deleteAbsenceAttendance: async (courseId, absenceAttendancesId)=>{
    await apiInstance.delete(`/my/course/${courseId}/absence-attendances/${absenceAttendancesId}`)
  }
};
