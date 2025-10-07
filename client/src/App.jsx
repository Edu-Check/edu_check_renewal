import React, { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import { authApi } from './api/authApi';
import { login } from './store/slices/authSlice';
import { useDispatch } from 'react-redux';
import { onMessageListener } from './api/firebase';

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

  useEffect(() => {

    // 1. 메시지 수신 시 처리할 콜백 함수를 전달하여 리스너 설정 
    // onMessageListener는 unsubscribe 함수를 반환한다.
    const unsubscribe = onMessageListener((payload) => {
      console.log("포그라운드 메시지 수신:", payload);

      //TODO: 사용자에게 알림을 보여주는 UI 로직 구현
      alert(`[새 알림] ${payload.data.title}: ${payload.data.body}`);
    });

    // 2. 컴포넌트가 언마운트될 때 리스너를 정리(clean-up) 한다. -> 누락시 메모리 누수 발생 가능
    return () => {
      unsubscribe();
    };
  }, []);

  return (
    <>
      <RouterProvider router={router}></RouterProvider>
    </>
  );
}
