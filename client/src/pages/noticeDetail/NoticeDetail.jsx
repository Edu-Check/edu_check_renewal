import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { noticeApi } from '../../api/noticeApi';
import styles from './NoticeDetail.module.css';
import { useSelector } from 'react-redux';

export default function NoticeDetail() {
  const { noticeId } = useParams();
  const [notice, setNotice] = useState(null);
  const courseId = useSelector((state) => state.auth.user.courseId);

  useEffect(() => {
    if (noticeId && courseId) {
      const fetchNoticeDetail = async () => {
        try {
          const response = await noticeApi.getNoticeDetail(courseId, noticeId);
          setNotice(response.data.data);
        } catch (error) {
          console.error('공지사항 상세 정보 조회 실패:', error);
        }
      };
      fetchNoticeDetail();
    }
  }, [noticeId, courseId]);

  if (!notice) {
    return <div className={styles.loading}>로딩 중...</div>;
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>{notice.title}</h1>
        <div className={styles.meta}>
          <span className={styles.author}>{notice.authorName}</span>
          <span className={styles.date}>{new Date(notice.createdAt).toLocaleString()}</span>
        </div>
      </div>
      <div className={styles.content}>
        <pre className={styles.textContent}>{notice.content}</pre>
      </div>
    </div>
  );
}
