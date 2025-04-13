import { createSlice } from '@reduxjs/toolkit';
import { jwtDecode } from 'jwt-decode';

const initialState = {
  accessToken: '',
  isLoggedIn: false,
  attendanceStatus: {
    isCheckedIn: false,
    checkInTime: null,
    attendanceDate: null,
    isCompleted: false,
  },
  user: {
    memberId: '',
    role: '',
    name: '',
    birthDate: '',
    campusId: '',
    campusName: '',
    courseId: '',
    courseName: '',
    email: '',
    phoneNumber: '',
    lastLoginDate: '',
    lastPasswordChangeDateTime: '',
  },
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    login: (state, action) => {
      const accessToken = action.payload.accessToken.replace('Bearer ', '');
      const decodedToken = jwtDecode(accessToken);
      state.accessToken = accessToken;
      state.isLoggedIn = true;
      state.user = {
        memberId: action.payload.id || '',
        role: decodedToken.roles[0] || '',
        email: decodedToken.sub || '',
        name: action.payload.name || '',
        birthDate: action.payload.birthDate || '',
        campusId: action.payload.campusId || '',
        campusName: action.payload.campusName || '',
        courseId: action.payload.courseId || '',
        courseName: action.payload.courseName || '',
        phoneNumber: action.payload.phoneNumber || '',
        lastLoginDate: action.payload.lastLoginDate || '',
        lastPasswordChangeDateTime: action.payload.lastPasswordChangeDateTime || '',
      };
    },
    logout: (state) => {
      state.accessToken = '';
      state.isLoggedIn = false;
      state.attendanceStatus = { ...initialState.attendanceStatus };
      state.user = { ...initialState.user };
    },
    checkIn: (state) => {
      state.attendanceStatus = {
        isCheckedIn: true,
        checkInTime: new Date().toISOString(),
        attendanceDate: new Date().toISOString().split('T')[0],
        isCompleted: false,
      };
    },
    completeAttendance: (state) => {
      state.attendanceStatus = {
        ...state.attendanceStatus,
        isCompleted: true,
      };
    },
    resetAttendanceStatus: (state) => {
      state.attendanceStatus = { ...initialState.attendanceStatus };
    },
  },
});

export const { login, logout, checkIn, completeAttendance, resetAttendanceStatus } =
  authSlice.actions;
export default authSlice.reducer;
