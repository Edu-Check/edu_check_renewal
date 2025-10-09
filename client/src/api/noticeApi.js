import apiInstance from './instance/apiInstance';

export const noticeApi = {
    createNotice: async (noticeData) => {
        return await apiInstance.post(
                `/courses/${noticeData.courseId}/notices`,
                noticeData,
                { withCredentials: true ,
                    headers: {
                    'Content-Type': 'application/json'
                    }
                });
    },
    getNoticeList: async (courseId) => {
            return apiInstance.get(`/courses/${courseId}/notices`);
    },
    getNoticeDetail: async (courseId, noticeId) => {
        return apiInstance.get(`/courses/${courseId}/notices/${noticeId}`);
    }
}