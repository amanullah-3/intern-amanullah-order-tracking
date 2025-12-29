import React from 'react';

const OrderTable = ({ orders, maxEvents }) => {
  const sortedOrders = Array.from(orders.values())
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    .slice(0, maxEvents);

  return (
    <div className="panel">
      <h3>🟢 Live Order Updates</h3>
      <div className="order-table-container">
        <table className="order-table">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Rider</th>
              <th>Status</th>
              <th>Time</th>
            </tr>
          </thead>
          <tbody>
            {sortedOrders.length === 0 ? (
              <tr>
                <td colSpan="4" style={{ textAlign: 'center', color: '#666' }}>
                  Waiting for live updates...
                </td>
              </tr>
            ) : (
              sortedOrders.map(order => (
                <tr key={order.orderId}>
                  <td><strong>{order.orderId}</strong></td>
                  <td>{order.riderId}</td>
                  <td>
                    <span className={`status-${order.status.toLowerCase().replace('_', '-')}`}>
                      {order.status}
                    </span>
                  </td>
                  <td>{new Date(order.timestamp).toLocaleTimeString()}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default OrderTable;