import React from 'react';
import styles from './Modal.module.css';
import MainButton from '../buttons/mainButton/MainButton';
import CloseButton from '../buttons/closeButton/CloseButton';

export default function Modal({
  isOpen,
  onClose,
  mainClick,
  subClick,
  mainText,
  subText,
  content,
}) {
  if (!isOpen) return null;
  return (
    <div className={styles.modal}>
      <div className={styles.backGround}></div>
      <div className={styles.container}>
        <CloseButton handleClick={onClose}></CloseButton>

        <div className={styles.contentWrapper}>
          <div>{content}</div>

          <div className={`${styles.buttonBox} ${!mainClick && `${styles.active}`}`}>
            {subClick && (
              <MainButton
                handleClick={subClick}
                title={subText || '확인'}
                isEnable={true}
              ></MainButton>
            )}
            {mainClick && (
              <MainButton
                handleClick={mainClick}
                title={mainText || '취소'}
                isEnable={true}
              ></MainButton>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
