// Load user orders on page load
document.addEventListener('DOMContentLoaded', () => {
  const auth = checkAuthStatus();
  
  if (!auth.isAuthenticated) {
    showLoginRequired();
    return;
  }
  

  loadUserOrders(auth.userId);
});

// Show login required message
function showLoginRequired() {
  const container = document.querySelector('.container');
  container.innerHTML = `
    <div class="row justify-content-center mt-5">
      <div class="col-md-6">
        <div class="card text-center">
          <div class="card-body">
            <i class="bi bi-lock" style="font-size: 3rem; color: #ffc107;"></i>
            <h3 class="mt-3">Login Required</h3>
            <p class="text-muted">Please login to view your orders.</p>
            <a href="login.html" class="btn btn-primary">Go to Login</a>
          </div>
        </div>
      </div>
    </div>
  `;
}



// Load user orders
function loadUserOrders(userId) {
  const tableBody = document.getElementById('orders-table');
  
  // Show loading state
  tableBody.innerHTML = `
    <tr>
      <td colspan="6" class="text-center">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-2">Loading your orders...</p>
      </td>
    </tr>
  `;
  
  fetch(`/order/user/${userId}`)
    .then(res => {
      if (!res.ok) {
        throw new Error('Failed to load orders');
      }
      return res.json();
    })
    .then(orders => {
      if (orders.length === 0) {
        tableBody.innerHTML = `
          <tr>
            <td colspan="6" class="text-center">
              <div class="alert alert-info mb-0">
                <i class="bi bi-info-circle"></i> You haven't placed any orders yet.
                <a href="menu.html" class="alert-link">Browse Menu</a>
              </div>
            </td>
          </tr>
        `;
        return;
      }
      
      tableBody.innerHTML = orders.map(order => {
        const statusBadge = getStatusBadge(order.status);
        const statusIcon = getStatusIcon(order.status);
        
        return `
          <tr>
            <td>${order.id}</td>
            <td>
              <strong>${escapeHtml(order.itemName)}</strong>
            </td>
            <td class="text-center">${order.quantity}</td>
            <td>₹${calculateTotal(order)}</td>
            <td class="text-center">
              ${statusIcon} ${statusBadge}
            </td>
            <td>
              <button class="btn btn-sm btn-outline-primary" 
                      onclick="showOrderDetails('${order.id}')">
                <i class="bi bi-eye"></i> Details
              </button>
            </td>
          </tr>
        `;
      }).join('');
      
      // Show order summary
      showOrderSummary(orders);
    })
    .catch(err => {
      console.error('Error loading orders:', err);
      tableBody.innerHTML = `
        <tr>
          <td colspan="6" class="text-center">
            <div class="alert alert-danger mb-0">
              <i class="bi bi-exclamation-triangle"></i> 
              Failed to load orders. Please try again later.
            </div>
          </td>
        </tr>
      `;
    });
}

// Calculate order total (from menu price would be better, but using estimate)
function calculateTotal(order) {
  // Since we don't have price in order DTO, we'll need to fetch or estimate
  // For now, using quantity * estimated price
  return (order.quantity * 150).toFixed(2); // Default estimate
}

// Get status badge HTML
function getStatusBadge(status) {
  const badges = {
    'PENDING': '<span class="badge bg-warning text-dark">Pending</span>',
    'APPROVED': '<span class="badge bg-info">Approved</span>',
    'COMPLETED': '<span class="badge bg-success">Completed</span>',
    'DELIVERED': '<span class="badge bg-success">Delivered</span>',
    'REJECTED': '<span class="badge bg-danger">Rejected</span>'
  };
  return badges[status] || '<span class="badge bg-secondary">Unknown</span>';
}

// Get status icon
function getStatusIcon(status) {
  const icons = {
    'PENDING': '<i class="bi bi-clock-history text-warning"></i>',
    'APPROVED': '<i class="bi bi-check-circle text-info"></i>',
    'COMPLETED': '<i class="bi bi-check-circle-fill text-success"></i>',
    'DELIVERED': '<i class="bi bi-box-seam text-success"></i>',
    'REJECTED': '<i class="bi bi-x-circle text-danger"></i>'
  };
  return icons[status] || '<i class="bi bi-question-circle"></i>';
}

// Show order summary
function showOrderSummary(orders) {
  const summaryContainer = document.querySelector('.container');
  
  const totalOrders = orders.length;
  const pendingOrders = orders.filter(o => o.status === 'PENDING').length;
  const completedOrders = orders.filter(o => o.status === 'COMPLETED' || o.status === 'DELIVERED').length;
  
  const summaryHtml = `
    <div class="row mb-4">
      <div class="col-md-4">
        <div class="card text-center border-primary">
          <div class="card-body">
            <h5 class="card-title text-primary">
              <i class="bi bi-bag-check"></i> Total Orders
            </h5>
            <h2 class="display-4">${totalOrders}</h2>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card text-center border-warning">
          <div class="card-body">
            <h5 class="card-title text-warning">
              <i class="bi bi-clock-history"></i> Pending
            </h5>
            <h2 class="display-4">${pendingOrders}</h2>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card text-center border-success">
          <div class="card-body">
            <h5 class="card-title text-success">
              <i class="bi bi-check-circle"></i> Completed
            </h5>
            <h2 class="display-4">${completedOrders}</h2>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Insert before the table
  const table = document.querySelector('.table').closest('.card');
  table.insertAdjacentHTML('beforebegin', summaryHtml);
}

// Show order details modal
function showOrderDetails(orderId) {
  const auth = checkAuthStatus();
  
  fetch(`/order/user/${auth.userId}`)
    .then(res => res.json())
    .then(orders => {
      const order = orders.find(o => o.id === orderId);
      
      if (!order) {
        showNotification('Order not found', 'danger');
        return;
      }
      
      const modalHtml = `
        <div class="modal fade" id="orderDetailModal" tabindex="-1">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header bg-primary text-white">
                <h5 class="modal-title">
                  <i class="bi bi-receipt"></i> Order Details
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
              </div>
              <div class="modal-body">
                <div class="row mb-3">
                  <div class="col-6"><strong>Order ID:</strong></div>
                  <div class="col-6">${order.id}</div>
                </div>
                <div class="row mb-3">
                  <div class="col-6"><strong>Item:</strong></div>
                  <div class="col-6">${escapeHtml(order.itemName)}</div>
                </div>
                <div class="row mb-3">
                  <div class="col-6"><strong>Quantity:</strong></div>
                  <div class="col-6">${order.quantity}</div>
                </div>
                <div class="row mb-3">
                  <div class="col-6"><strong>Status:</strong></div>
                  <div class="col-6">${getStatusBadge(order.status)}</div>
                </div>
                <div class="row mb-3">
                  <div class="col-6"><strong>Estimated Total:</strong></div>
                  <div class="col-6">₹${calculateTotal(order)}</div>
                </div>
                <hr>
                <div class="alert alert-info mb-0">
                  <i class="bi bi-info-circle"></i>
                  ${getStatusMessage(order.status)}
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
              </div>
            </div>
          </div>
        </div>
      `;
      
      // Remove existing modal
      const existingModal = document.getElementById('orderDetailModal');
      if (existingModal) {
        existingModal.remove();
      }
      
      // Add and show modal
      document.body.insertAdjacentHTML('beforeend', modalHtml);
      const modal = new bootstrap.Modal(document.getElementById('orderDetailModal'));
      modal.show();
      
      // Clean up
      document.getElementById('orderDetailModal').addEventListener('hidden.bs.modal', function () {
        this.remove();
      });
    })
    .catch(err => {
      console.error('Error loading order details:', err);
      showNotification('Failed to load order details', 'danger');
    });
}

// Get status message
function getStatusMessage(status) {
  const messages = {
    'PENDING': 'Your order is being processed. Please wait for confirmation.',
    'APPROVED': 'Your order has been approved and is being prepared.',
    'COMPLETED': 'Your order is ready for pickup/delivery.',
    'DELIVERED': 'Your order has been delivered. Enjoy your meal!',
    'REJECTED': 'Sorry, your order was rejected. Please contact support.'
  };
  return messages[status] || 'Order status unknown.';
}

// Show notification
function showNotification(message, type = 'info') {
  const toastHtml = `
    <div class="toast align-items-center text-white bg-${type} border-0" role="alert">
      <div class="d-flex">
        <div class="toast-body">${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    </div>
  `;
  
  let toastContainer = document.getElementById('toast-container');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
    document.body.appendChild(toastContainer);
  }
  
  toastContainer.insertAdjacentHTML('beforeend', toastHtml);
  const toastElement = toastContainer.lastElementChild;
  const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
  toast.show();
  
  toastElement.addEventListener('hidden.bs.toast', () => toastElement.remove());
}

// Utility function
function escapeHtml(text) {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, m => map[m]);
}