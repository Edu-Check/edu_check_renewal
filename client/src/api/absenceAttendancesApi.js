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

  getAbsenceAttendanceListByStudent: async (courseId, page) => {
    const response = await apiInstance.get(
      `/my/course/${courseId}/absence-attendances?page=${page}`,
    );
    return response.data.data;
  },

  submitAbsenceAttendance: async (courseId, data) => {
    const response = await apiInstance.post(`/my/course/${courseId}/absence-attendances`, data);
    return response;
  },

  deleteAbsenceAttendance: async (courseId, absenceAttendancesId) => {
    await apiInstance.delete(`/my/course/${courseId}/absence-attendances/${absenceAttendancesId}`);
  },

  getPresignedUrls: async (fileNames, courseId) => {
    const response = await apiInstance.get(`/my/course/${courseId}/presigned-upload`, {
      params: {
        fileNames: fileNames,
      },
      paramsSerializer: (params) => {
        return Object.keys(params)
          .map((key) => params[key].map((value) => `${key}=${encodeURIComponent(value)}`).join('&'))
          .join('&');
      },
    });
    return response.data.data;
  },

  saveUploadedFiles: async (courseId, files) => {
    const response = await apiInstance.post(`/my/course/${courseId}/absence-attendances/files`, {
      files,
    });
    return response.data;
  },
};
