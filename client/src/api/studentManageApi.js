import apiInstance from './instance/apiInstance';

export const studentManageApi = {
  getStudentList: async (courseId) => await apiInstance.get(`/course/${courseId}/students`),

  registerNewStudent: async (studentData) => await apiInstance.post('/auth/signup', studentData),

  modifyStudentStatus: async (courseId, studentId, status) => {
    switch (status) {
      case 'COMPLETED':
        return await apiInstance.patch(`/course/${courseId}/students/${studentId}`, {
          completionDate: new Date(Date.now() + 9 * 60 * 60 * 1000),
        });
      case 'ACTIVE':
        return await apiInstance.patch(`/course/${courseId}/students/${studentId}`, {
          completionDate: null,
          dropDate: null,
        });
      case 'DROPPED':
        return await apiInstance.patch(`/course/${courseId}/students/${studentId}`, {
          completionDate: null,
          dropDate: new Date(Date.now() + 9 * 60 * 60 * 1000),
        });
    }
  },
};
