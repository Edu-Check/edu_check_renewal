import React, { useEffect, useState } from 'react';
import styles from './PaginationComponent.module.css';

export default function PaginationComponent({
  totalPages,
  onPageChange,
  goToPreviousPage,
  goToNextPage,
  currentPage,
}) {
  const [inputPage, setInputPage] = useState(currentPage);

  useEffect(() => {
    setInputPage(currentPage);
  }, [currentPage]);

  const handleInputChange = (e) => {
    const value = parseInt(e.target.value, 10);
    if (!isNaN(value) && value >= 1 && value <= totalPages) {
      setCurrentPage(value);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && currentPage >= 1 && currentPage <= totalPages) {
      onPageChange(currentPage);
    }
  };

  // const goToNextPage = () => {
  //   if (currentPage < totalPages) {
  //     setCurrentPage((prev) => prev + 1);
  //     onPageChange(currentPage + 1);
  //   }
  // };

  return (
    <div className={styles.container}>
      <button className={styles.button} onClick={goToPreviousPage} disabled={currentPage === 1}>
        <img src="/assets/arrowBackIcon.svg" alt="arrowPre" className={styles.arrowPrevious} />
      </button>
      <div className={styles.pageIndicator}>
        <input
          className={styles.input}
          type="number"
          value={inputPage}
          onChange={handleInputChange}
          onKeyPress={handleKeyPress}
          min="1"
          max={totalPages}
        />
        <span className={styles.pageCount}> / {totalPages}</span>
      </div>
      <button
        className={styles.button}
        onClick={goToNextPage}
        disabled={currentPage === totalPages}
      >
        <img src="/assets/arrowBackIcon.svg" alt="arrowNext" className={styles.arrowNext} />
      </button>
    </div>
  );
}
