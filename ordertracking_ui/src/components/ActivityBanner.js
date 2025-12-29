import React, { useEffect, useRef } from 'react';

const ActivityBanner = ({ message }) => {
  const bannerRef = useRef(null);

  useEffect(() => {
    if (bannerRef.current) {
      // Add animation effect
      bannerRef.current.style.transform = 'scale(1.02)';
      setTimeout(() => {
        if (bannerRef.current) {
          bannerRef.current.style.transform = 'scale(1)';
        }
      }, 200);
    }
  }, [message]);

  return (
    <div className="activity-banner" ref={bannerRef}>
      {message}
    </div>
  );
};

export default ActivityBanner;