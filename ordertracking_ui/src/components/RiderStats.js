import React from 'react';

const RiderStats = ({ 
  riderStats, 
  dateInfo
}) => {
  return (
    <div className="panel">
      <h3>🟠 Rider Performance</h3>
      <div style={{ marginBottom: '15px' }}>
        <div className="date-info" style={{ marginBottom: '10px', fontWeight: 'bold', color: '#666' }}>
          {dateInfo}
        </div>
      </div>
      <div className="rider-stats">
        {riderStats.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#666' }}>
            Loading rider statistics...
          </div>
        ) : (
          riderStats.map(summary => (
            <div key={summary.riderId} className="rider-card">
              <h4>{summary.riderId}</h4>
              <div className="stat">
                <span>Delivered Today:</span>
                <strong>{summary.deliveredOrders || 0}</strong>
              </div>
              <div className="stat">
                <span>Avg Time:</span>
                <strong>{summary.avgDeliveryTimeMinutes || 'N/A'} min</strong>
              </div>
              <div className="stat">
                <span>Delays:</span>
                <strong>{summary.delayedOrders || 0}</strong>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default RiderStats;