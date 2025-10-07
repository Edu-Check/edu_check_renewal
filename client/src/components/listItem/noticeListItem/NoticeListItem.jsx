import React from 'react';
import styles from './NoticeListItem.module.css';

export default function NoticeListItem({ title, author, date, onClick }) {
  return (
    <div className={styles.itemContainer} onClick={onClick}>
      <div className={styles.info}>
        <p className={styles.title}>{title}</p>
        <span className={styles.author}>{author}</span>
        <span className={styles.date}>{date}</span>
      </div>
    </div>
  );
}
