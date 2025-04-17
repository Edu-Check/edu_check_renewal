import apiInstance from './instance/apiInstance';

export const demoAuthApi = {
  demoMiddleAdminLogin: async () => {
    const response = await apiInstance.post(
      `/auth/demo-middle-admin-login`,
      {},
      {
        withCredentials: true,
      },
    );
    return response;
  },

  demoStudentLogin: async () => {
    const response = await apiInstance.post(
      `/auth/demo-student-login`,
      {},
      {
        withCredentials: true,
      },
    );
    return response;
  },
};
