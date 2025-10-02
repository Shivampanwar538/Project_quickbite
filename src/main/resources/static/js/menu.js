// Load user info on page load
document.addEventListener('DOMContentLoaded', () => {
  const auth = checkAuthStatus();

  loadMenu();
});

// Load and display menu items
function loadMenu() {
  const container = document.getElementById('menu-container');
  
  // Show loading state
  container.innerHTML = `
    <div class="col-12 text-center">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2">Loading delicious menu...</p>
    </div>
  `;
  
  fetch('/menu')
    .then(res => {
      if (!res.ok) {
        throw new Error('Failed to load menu');
      }
      return res.json();
    })
    .then(menuItems => {
      if (menuItems.length === 0) {
        container.innerHTML = `
          <div class="col-12 text-center">
            <div class="alert alert-info">
              <i class="bi bi-info-circle"></i> No menu items available at the moment.
            </div>
          </div>
        `;
        return;
      }
      
      container.innerHTML = menuItems.map(item => `
        <div class="col-md-4 mb-4">
          <div class="card shadow-sm h-100 menu-item-card">
            <div class="card-body d-flex flex-column">
              <div class="d-flex justify-content-between align-items-start mb-2">
                <h5 class="card-title text-primary">${escapeHtml(item.name)}</h5>
                <span class="badge bg-success fs-6">₹${item.price.toFixed(2)}</span>
              </div>
              <p class="card-text text-muted flex-grow-1">
                ${item.description ? escapeHtml(item.description) : 'Delicious food item'}
              </p>
              <div class="mt-auto">
                <div class="input-group mb-2">
                  <button class="btn btn-outline-secondary" type="button" 
                          onclick="decrementQuantity('${item.id}')">
                    <i class="bi bi-dash"></i>
                  </button>
                  <input type="number" class="form-control text-center" 
                         id="qty-${item.id}" value="1" min="1" max="10" readonly>
                  <button class="btn btn-outline-secondary" type="button" 
                          onclick="incrementQuantity('${item.id}')">
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
                <button class="btn btn-success w-100" onclick="placeOrder('${item.id}')">
                  <i class="bi bi-cart-plus"></i> Order Now
                </button>
              </div>
            </div>
          </div>
        </div>
      `).join('');
    })
    .catch(err => {
      console.error('Error loading menu:', err);
      container.innerHTML = `
        <div class="col-12">
          <div class="alert alert-danger">
            <i class="bi bi-exclamation-triangle"></i> 
            Failed to load menu. Please try again later.
          </div>
        </div>
      `;
    });
}

// Quantity controls
function incrementQuantity(itemId) {
  const qtyInput = document.getElementById(`qty-${itemId}`);
  let currentValue = parseInt(qtyInput.value) || 1;
  if (currentValue < 10) {
    qtyInput.value = currentValue + 1;
  }
}

function decrementQuantity(itemId) {
  const qtyInput = document.getElementById(`qty-${itemId}`);
  let currentValue = parseInt(qtyInput.value) || 1;
  if (currentValue > 1) {
    qtyInput.value = currentValue - 1;
  }
}

// Place order function
function placeOrder(menuItemId) {
  const auth = checkAuthStatus();
  
  if (!auth.isAuthenticated) {
    if (confirm('Please login first to place an order. Go to login page?')) {
      window.location.href = 'login.html';
    }
    return;
  }

  const userId = auth.userId;
  const qtyInput = document.getElementById(`qty-${menuItemId}`);
  const quantity = parseInt(qtyInput.value) || 1;

  // Validate quantity
  if (quantity < 1 || quantity > 10) {
    showNotification('Please select a valid quantity (1-10)', 'warning');
    return;
  }

  // Find the button and show loading state
  const btn = event.target.closest('button');
  const originalText = btn.innerHTML;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Ordering...';
  btn.disabled = true;

  fetch('/order/place', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
      userId: userId,
      menuItemId: menuItemId,
      quantity: quantity
    })
  })
  .then(res => {
    if (!res.ok) {
      return res.json().then(error => {
        throw new Error(error.message || 'Failed to place order');
      });
    }
    return res.json();
  })
  .then(data => {
    console.log('Order placed:', data);
    showNotification(`✅ Order placed successfully! Quantity: ${quantity}`, 'success');
    
    // Reset quantity to 1
    qtyInput.value = 1;
    
    // Show order confirmation
    showOrderConfirmation(data);
  })
  .catch(err => {
    console.error('Order failed:', err);
    showNotification(`❌ ${err.message}`, 'danger');
  })
  .finally(() => {
    btn.innerHTML = originalText;
    btn.disabled = false;
  });
}

// Show order confirmation modal/toast
function showOrderConfirmation(orderData) {
  const confirmationHtml = `
    <div class="modal fade" id="orderConfirmModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title">
              <i class="bi bi-check-circle"></i> Order Confirmed!
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p><strong>Order ID:</strong> ${orderData.id}</p>
            <p><strong>Item:</strong> ${orderData.itemName}</p>
            <p><strong>Quantity:</strong> ${orderData.quantity}</p>
            <p><strong>Status:</strong> <span class="badge bg-warning">${orderData.status}</span></p>
            <p class="text-muted">You can check your order status in the Orders section.</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Continue Shopping</button>
            <a href="orders.html" class="btn btn-primary">View Orders</a>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Remove existing modal if any
  const existingModal = document.getElementById('orderConfirmModal');
  if (existingModal) {
    existingModal.remove();
  }
  
  // Add new modal
  document.body.insertAdjacentHTML('beforeend', confirmationHtml);
  
  // Show modal
  const modal = new bootstrap.Modal(document.getElementById('orderConfirmModal'));
  modal.show();
  
  // Clean up after modal is hidden
  document.getElementById('orderConfirmModal').addEventListener('hidden.bs.modal', function () {
    this.remove();
  });
}

// Show notification toast
function showNotification(message, type = 'info') {
  const toastHtml = `
    <div class="toast align-items-center text-white bg-${type} border-0" role="alert">
      <div class="d-flex">
        <div class="toast-body">
          ${message}
        </div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    </div>
  `;
  
  // Create toast container if it doesn't exist
  let toastContainer = document.getElementById('toast-container');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
    document.body.appendChild(toastContainer);
  }
  
  // Add toast
  toastContainer.insertAdjacentHTML('beforeend', toastHtml);
  
  // Show toast
  const toastElement = toastContainer.lastElementChild;
  const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
  toast.show();
  
  // Remove after hidden
  toastElement.addEventListener('hidden.bs.toast', () => {
    toastElement.remove();
  });
}

// Utility function to escape HTML
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

// Add CSS for card hover effect
const style = document.createElement('style');
style.textContent = `
  .menu-item-card {
    transition: transform 0.2s, box-shadow 0.2s;
  }
  .menu-item-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 16px rgba(0,0,0,0.2) !important;
  }
`;
document.head.appendChild(style);