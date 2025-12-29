import React, { useState, useEffect, useRef } from 'react';
import ActivityBanner from './components/ActivityBanner';
import OrderTable from './components/OrderTable';
import RiderStats from './components/RiderStats';
import RecentEvents from './components/RecentEvents';

function App() {
  const [orders, setOrders] = useState(new Map());
  const [recentEvents, setRecentEvents] = useState([]);
  const [activityMessage, setActivityMessage] = useState('Waiting for rider events...');
  const [riderStats, setRiderStats] = useState([]);
  const [dateInfo, setDateInfo] = useState("Today's Progress (Live)");
  
  const eventSourceRef = useRef(null);
  const maxEvents = 50;
  const maxRecentEvents = 20;

  // SSE Connection Management
  const connectSSE = () => {
    console.log('Attempting to connect to SSE...');
    // In development, connect directly to Spring Boot backend
    const sseUrl = process.env.NODE_ENV === 'development' 
      ? 'http://localhost:8080/stream/order-updates'
      : '/stream/order-updates';
    
    console.log('SSE URL:', sseUrl);
    console.log('Attempting SSE connection...');
    
    try {
      eventSourceRef.current = new EventSource(sseUrl);
    } catch (error) {
      console.error('Failed to create EventSource:', error);
      setIsConnected(false);
      setActivityMessage('Failed to create SSE connection');
      return;
    }
    
    eventSourceRef.current.onopen = () => {
      console.log('SSE connection opened successfully');
      setActivityMessage('Waiting for rider events...');
    };
    
    // Only use addEventListener for specific event types, not onmessage
    eventSourceRef.current.addEventListener('ORDER_UPDATE', (event) => {
      console.log('ORDER_UPDATE received:', event.data);
      try {
        const data = JSON.parse(event.data);
        handleOrderUpdate(data);
      } catch (e) {
        console.error('Error parsing ORDER_UPDATE:', e);
      }
    });
    
    eventSourceRef.current.onerror = (error) => {
      console.error('SSE error occurred:', error);
      console.error('EventSource readyState:', eventSourceRef.current?.readyState);
      console.error('EventSource url:', eventSourceRef.current?.url);
      setActivityMessage('Connection lost. Attempting to reconnect...');
      
      // Attempt to reconnect after 5 seconds
      setTimeout(() => {
        if (eventSourceRef.current?.readyState === EventSource.CLOSED) {
          console.log('Attempting to reconnect...');
          connectSSE();
        }
      }, 5000);
    };
  };

  const handleOrderUpdate = (data) => {
    if (data.orderId && data.riderId && data.status) {
      // Update orders map
      setOrders(prevOrders => {
        const newOrders = new Map(prevOrders);
        newOrders.set(data.orderId, {
          orderId: data.orderId,
          riderId: data.riderId,
          status: data.status,
          timestamp: data.eventTimestamp || new Date().toISOString(),
          message: data.message
        });
        return newOrders;
      });
      
      // Create a proper activity message
      const activityMessage = data.message || createActivityMessage(data.riderId, data.orderId, data.status);
      
      // Add to recent events
      setRecentEvents(prevEvents => {
        const newEvents = [{
          message: activityMessage,
          timestamp: new Date().toLocaleTimeString()
        }, ...prevEvents];
        
        return newEvents.slice(0, maxRecentEvents);
      });
      
      setActivityMessage(activityMessage);
    }
  };

  // Helper function to create consistent activity messages
  const createActivityMessage = (riderId, orderId, status) => {
    switch (status.toLowerCase()) {
      case 'picked_up':
      case 'picked-up':
        return `Rider ${riderId} picked up Order ${orderId}`;
      case 'in_transit':
      case 'in-transit':
        return `Rider ${riderId} is delivering Order ${orderId}`;
      case 'delivered':
        return `Rider ${riderId} delivered Order ${orderId}`;
      default:
        return `Rider ${riderId} updated Order ${orderId} to ${status}`;
    }
  };

  // Load rider statistics (defaults to today)
  const loadRiderStats = async () => {
    console.log('Loading rider statistics (today)...');
    try {
      const response = await fetch('/metrics/daily-summary');
      console.log('Rider stats response status:', response.status);
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      console.log('Rider stats data:', data);
      setRiderStats(data);
      setDateInfo("Today's Progress (Live)");
    } catch (error) {
      console.error('Error loading rider stats:', error);
      setRiderStats([]);
    }
  };

  // Initialize everything
  useEffect(() => {
    console.log('Component mounted, initializing...');
    connectSSE();
    loadRiderStats();
    
    // Refresh rider stats every 10 seconds for real-time updates
    const interval = setInterval(loadRiderStats, 10000);
    
    // Cleanup on unmount
    return () => {
      clearInterval(interval);
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div>
      <div className="dashboard">
        <div className="header">
          <h1>Live Order Tracking Dashboard</h1>
        </div>
        
        <ActivityBanner message={activityMessage} />
        
        <div className="panels">
          <OrderTable orders={orders} maxEvents={maxEvents} />
          
          <RiderStats 
            riderStats={riderStats}
            dateInfo={dateInfo}
          />
        </div>
        
        <RecentEvents events={recentEvents} />
      </div>
    </div>
  );
}

export default App;