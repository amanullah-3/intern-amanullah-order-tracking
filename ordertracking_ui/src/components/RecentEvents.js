import React from 'react';

const RecentEvents = ({ events }) => {
  return (
    <div className="panel">
      <h3>🔵 Recent Activity Feed</h3>
      <div className="recent-events">
        {events.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#666', padding: '20px' }}>
            Waiting for activity updates...
          </div>
        ) : (
          events.map((event, index) => (
            <div key={index} className="event-item">
              <span className="event-time">{event.timestamp}</span>
              {event.message}
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default RecentEvents;