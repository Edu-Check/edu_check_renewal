import React, { useEffect, useState } from 'react';
import styles from './StudentSetting.module.css';
import InputBox from '../../components/inputBox/InputBox';
import MainButton from '../../components/buttons/mainButton/MainButton';
import { profileApi } from '../../api/profileApi';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../../store/slices/authSlice';
import { useNavigate } from 'react-router-dom';

export default function StudentSetting() {
  const accessToken = useSelector((state) => state.auth.accessToken);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [input, setInput] = useState({
    phoneNumber: '',
    password: '',
    confirmPassword: '',
    currentPassword: '',
    year: '',
    month: '',
    day: '',
  });

  const formatPhoneNumber = (value) => {
    const cleaned = value.replace(/\D/g, '');

    if (!cleaned) return '';

    if (cleaned.startsWith('010')) {
      if (cleaned.length < 4) return cleaned;

      if (cleaned.length < 7) {
        return `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
      }

      if (cleaned.length === 10) {
        return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 6)}-${cleaned.slice(6, 10)}`;
      }

      if (cleaned.length >= 11) {
        return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7, 11)}`;
      }
    }

    if (cleaned.length < 4) return cleaned;
    if (cleaned.length < 7) {
      return `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
    }
    if (cleaned.length < 11) {
      return `${cleaned.slice(0, 3)}-${cleaned.slice(3, cleaned.length - 4)}-${cleaned.slice(-4)}`;
    }
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7, 11)}`;
  };

  const handlePhoneChange = (e) => {
    const formattedPhone = formatPhoneNumber(e.target.value);
    setInput((prev) => ({
      ...prev,
      phoneNumber: formattedPhone,
    }));
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setInput((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  useEffect(() => {
    if (!accessToken) return;

    const fetchMyProfile = async () => {
      try {
        const response = await profileApi.getMyProfile();
        const data = response.data.data;
        setInput((prev) => ({
          ...prev,
          phoneNumber: data.phoneNumber || '',
          ...(data.birthDate
            ? (() => {
                const [year, month, day] = data.birthDate.split('-');
                return { year, month, day };
              })()
            : { year: '', month: '', day: '' }),
        }));
      } catch (error) {
        console.error(error);
      }
    };

    fetchMyProfile();
  }, [accessToken]);

  const validatePhoneNumber = (phoneNumber) => {
    const phoneRegex = /^[0-9]{3}-[0-9]{3,4}-[0-9]{4}$/;
    return phoneRegex.test(phoneNumber);
  };

  const validatePassword = (password) => {
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,16}$/;
    return passwordRegex.test(password);
  };

  const phoneError =
    input.phoneNumber && !validatePhoneNumber(input.phoneNumber)
      ? '전화번호 형식이 올바르지 않습니다.'
      : '';
  const passwordError =
    input.password && !validatePassword(input.password)
      ? '비밀번호는 8자 이상, 대소문자, 숫자, 특수문자를 포함해야 합니다.'
      : '';
  const confirmPasswordError =
    input.password && input.confirmPassword && input.password !== input.confirmPassword
      ? '비밀번호가 일치하지 않습니다.'
      : '';
  const currentPasswordError =
    input.currentPassword === '' ? '현재 비밀번호는 필수 입력값입니다.' : '';

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (phoneError || passwordError || confirmPasswordError || currentPasswordError) {
      alert('입력값을 확인해 주세요.');
      return;
    }

    const updateData = {
      currentPassword: input.currentPassword,
    };

    if (input.phoneNumber) {
      updateData.phoneNumber = input.phoneNumber;
    }

    if (input.year && input.month && input.day) {
      updateData.birthDate = `${input.year}-${input.month}-${input.day}`;
    }

    if (input.password) {
      updateData.newPassword = input.password;
    }

    try {
      await profileApi.updateMyProfile(updateData);
      alert('개인정보가 수정 되었습니다.\n다시 로그인 해주세요.');
      dispatch(logout());
      navigate('/');
    } catch (error) {
      console.error(error);
      alert('수정에 실패했습니다.');
    }
  };

  return (
    <form className={styles.studentSetting} onSubmit={handleSubmit}>
      <InputBox
        name="phoneNumber"
        type="text"
        title="연락처"
        disabled={false}
        onChange={handlePhoneChange}
        label="연락처"
        isSelect="(선택)"
        value={input.phoneNumber}
        content={phoneError}
      />

      <div className={styles.smallInputBox}>
        <InputBox
          name="year"
          type="text"
          title="년도"
          disabled={false}
          onChange={handleChange}
          label="생년월일"
          isSelect="(선택)"
          value={input.year}
          content=""
        />
        <InputBox
          name="month"
          type="text"
          title="월"
          disabled={false}
          onChange={handleChange}
          value={input.month}
          content=""
        />
        <InputBox
          name="day"
          type="text"
          title="일"
          disabled={false}
          onChange={handleChange}
          value={input.day}
          content=""
        />
      </div>

      <InputBox
        name="password"
        type="password"
        title="비밀번호"
        disabled={false}
        onChange={handleChange}
        label="비밀번호 변경"
        isSelect="(선택)"
        value={input.password}
        content={passwordError}
      />

      <div className={styles.noneInputBox}>
        <InputBox
          name="confirmPassword"
          type="password"
          title="비밀번호 확인"
          disabled={false}
          onChange={handleChange}
          isSelect={true}
          value={input.confirmPassword}
          content={confirmPasswordError}
        />
      </div>

      <InputBox
        name="currentPassword"
        type="password"
        title="현재 비밀번호"
        disabled={false}
        onChange={handleChange}
        label="현재 비밀번호"
        isSelect="(필수)"
        value={input.currentPassword}
        content={currentPasswordError}
      />

      <MainButton title="수정" handleClick={handleSubmit} isEnable={true} />
    </form>
  );
}
