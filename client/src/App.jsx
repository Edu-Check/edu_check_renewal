import React, { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import { authApi } from './api/authApi';
import { login } from './store/slices/authSlice';
import { useDispatch } from 'react-redux';

export default function App() {
  const dispatch = useDispatch();

  useEffect(() => {
    const fetchRefreshToken = async () => {
      try {
        const response = await authApi.reissue();
        const accessToken = response.headers?.authorization ?? '';
        dispatch(
          login({
            ...response.data.data,
            accessToken: accessToken,
          }),
        );
        const isCheckIn = response.data.data.isCheckIn;
      if (isCheckIn) {
        const expiryDate = new Date();
        expiryDate.setUTCDate(expiryDate.getUTCDate() + 1);
        expiryDate.setUTCHours(15, 0, 0, 0);
        document.cookie = `${response.data.data.email}=checkIn; expires=${expiryDate.toUTCString()}; path=/`;
      }
      } catch (error) {
        // console.error(error);
      }
    };

    fetchRefreshToken();
  }, []);

  return (
    <>
      <RouterProvider router={router}></RouterProvider>
    </>
  );
}
