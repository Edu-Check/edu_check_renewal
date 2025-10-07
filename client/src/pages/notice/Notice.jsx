import React, { useEffect } from 'react';
import MainButton from '../../components/buttons/mainButton/MainButton';
import { useSelector } from 'react-redux';
import Modal from '../../components/modal/Modal';
import { useState } from 'react';
import styles from './Notice.module.css';
import { noticeApi } from '../../api/noticeApi';
import NoticeListItem from '../../components/listItem/noticeListItem/NoticeListItem';
import { format } from 'date-fns';

export default function Notice() {
  const userRole = useSelector((state) => state.auth.user.role);
  const courseId = useSelector((state) => state.auth.user.courseId);

  //공지사항 목록, 모달 상태, 데이터 리프레시 상태 관리
  const [notices, setNotices] = useState([]);
  const [openModal, setOpenModal] = useState(false);
  const [refresh, setRefresh] = useState(false);

  //모달에서 사용할 새 공지사항 데이터 상태
  const [newNotice, setNewNotice] = useState({
    title: '',
    content: '',
    courseId: courseId,
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newNotice.title || !newNotice.content) {
      alert('제목과 내용을 모두 입력해주세요.');
      return;
    }

    try {
      await noticeApi.createNotice(newNotice);
      setRefresh((prev) => !prev);
      setOpenModal(false);

      setNewNotice({ title: '', content: '', courseId: courseId });
    } catch (error) {
      console.error('공지사항 등록 실패:', error);
      alert(error.response?.data?.message || '등록에 실패했습니다.');
    }
  };

  useEffect(() => {
    if (!courseId) return;

    const fetchNotices = async () => {
      try {
        const response = await noticeApi.getNoticeList(courseId);
        console.log(response.data.data);
        setNotices(response.data.data);
      } catch (error) {
        console.error('공지사항 로딩 실패:', error);
      }
    };
    fetchNotices();
  }, [courseId, refresh]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNewNotice((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };

  //모달에 들어갈 공지사항 작성 폼
  const noticeForm = (
    <form onSubmit={handleSubmit}>
      <div className={styles.inputContainer}>
        <label>제목</label>
        <input
          className={styles.smallInputBox} // StaffStudentManage.module.css의 스타일 재활용
          name="title"
          type="text"
          placeholder="공지사항 제목을 입력하세요"
          value={newNotice.title}
          onChange={handleChange}
        />

        <label>내용</label>
        <textarea
          className={styles.largeInputBox} // 내용 입력창을 위해 CSS에 별도 스타일 추가 필요
          name="content"
          placeholder="내용을 입력하세요"
          value={newNotice.content}
          onChange={handleChange}
        />
      </div>
      <div className={styles.MainButton}>
        <button type="submit" className={styles.button}>
          등록
        </button>
      </div>
    </form>
  );

  return (
    <>
      <div className={styles.container}>
        <div className={styles.buttonContainer}>
          {userRole === 'MIDDLE_ADMIN' && (
            <MainButton
              title="공지 작성"
              isEnable={true}
              handleClick={() => setOpenModal(true)}
            ></MainButton>
          )}
        </div>

        <div className={styles.listHeader}>
          <p className={styles.headerTitle}>타이틀</p>
          <span className={styles.headerAuthor}>작성자</span>
          <span className={styles.headerDate}>작성시간</span>
        </div>

        {notices.map((notice) => (
          <NoticeListItem
            key={notice.id}
            title={notice.title}
            author={notice.authorName} // 백엔드에서 받은 작성자 이름
            date={new Date(notice.createdAt).toLocaleString()} // 날짜+시간 형식
            // onClick={() => alert(공지 ID: ${notice.id})} // 클릭 이벤트 추가 가능
          />
        ))}

        {/* 모달  */}
        <Modal content={noticeForm} isOpen={openModal} onClose={() => setOpenModal(false)}></Modal>
      </div>
    </>
  );
}
