import apiInstance from './instance/apiInstance';

export const profileApi = {
  getMyProfile: async () => {
    return await apiInstance.get(`/my`);
  },

  updateMyProfile: async (data) => {
    return await apiInstance.patch(`/my`, data, { withCredentials: true });
  },
};
