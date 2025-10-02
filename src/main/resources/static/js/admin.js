// ============ AUTHENTICATION CHECK ============
document.addEventListener("DOMContentLoaded", () => {
  // Check if user is admin
  const auth = checkAuthStatus();

  if (!auth.isAuthenticated) {
    window.location.href = 'login.html';
    return;
  }

  if (auth.role !== 'ADMIN') {
    alert('Access denied. Admin privileges required.');
    window.location.href = 'menu.html';
    return;
  }

  // Navbar with dynamic auth display
  const nav = `
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm fixed-top">
    <div class="container">
      <a class="navbar-brand fw-bold text-warning" href="index.html">
        <i class="bi bi-bag-heart-fill"></i> QuickBite Admin
      </a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav ms-auto">
          <li class="nav-item"><a class="nav-link" href="index.html">Home</a></li>
          <li class="nav-item"><a class="nav-link" href="menu.html">Menu</a></li>
          <li class="nav-item"><a class="nav-link" href="orders.html">My Orders</a></li>
          <li class="nav-item"><a class="nav-link active" href="admin.html">Admin</a></li>
          <li class="nav-item">
            <span class="nav-link text-warning">
              <i class="bi bi-person-circle"></i> ${auth.username}
            </span>
          </li>
          <li class="nav-item">
            <a class="nav-link btn btn-outline-danger btn-sm" href="#" onclick="logout(); return false;">
              <i class="bi bi-box-arrow-right"></i> Logout
            </a>
          </li>
        </ul>
      </div>
    </div>
  </nav>`;
  document.body.insertAdjacentHTML("afterbegin", nav);

  loadMenu();
  loadOrders();
  loadDashboardStats();
});

// ============ DASHBOARD STATISTICS ============
function loadDashboardStats() {
  Promise.all([
    fetch('/menu').then(r => r.json()),
    fetch('/order/all').then(r => r.json()),
    fetch('/auth').then(r => r.json())
  ])
  .then(([menuItems, orders, users]) => {
    const statsHtml = `
      <div class="row mb-4">
        <div class="col-md-3">
          <div class="card text-center border-primary">
            <div class="card-body">
              <i class="bi bi-shop" style="font-size: 2rem; color: #0d6efd;"></i>
              <h3 class="mt-2">${menuItems.length}</h3>
              <p class="text-muted mb-0">Menu Items</p>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card text-center border-warning">
            <div class="card-body">
              <i class="bi bi-receipt" style="font-size: 2rem; color: #ffc107;"></i>
              <h3 class="mt-2">${orders.length}</h3>
              <p class="text-muted mb-0">Total Orders</p>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card text-center border-info">
            <div class="card-body">
              <i class="bi bi-clock-history" style="font-size: 2rem; color: #0dcaf0;"></i>
              <h3 class="mt-2">${orders.filter(o => o.status === 'PENDING').length}</h3>
              <p class="text-muted mb-0">Pending</p>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card text-center border-success">
            <div class="card-body">
              <i class="bi bi-people" style="font-size: 2rem; color: #198754;"></i>
              <h3 class="mt-2">${users.length}</h3>
              <p class="text-muted mb-0">Users</p>
            </div>
          </div>
        </div>
      </div>
    `;

    const container = document.querySelector('.container');
    const heading = container.querySelector('h2');
    heading.insertAdjacentHTML('afterend', statsHtml);
  })
  .catch(err => console.error('Error loading stats:', err));
}

// ============ MENU MANAGEMENT ============

function loadMenu() {
  fetch('/menu')
    .then(res => {
      if (!res.ok) throw new Error('Failed to load menu');
      return res.json();
    })
    .then(items => {
      const tbody = document.querySelector("#menu-table tbody");

      if (items.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="5" class="text-center">
              <div class="alert alert-info mb-0">No menu items yet. Add your first item above!</div>
            </td>
          </tr>
        `;
        return;
      }

      tbody.innerHTML = items.map(i => `
        <tr>
          <td><code>${i.id}</code></td>
          <td><strong>${escapeHtml(i.name)}</strong></td>
          <td>${escapeHtml(i.description || '-')}</td>
          <td class="text-success fw-bold">₹${i.price.toFixed(2)}</td>
          <td>
            <button class="btn btn-sm btn-warning me-2" onclick="editItem('${i.id}', '${escapeHtml(i.name)}', '${escapeHtml(i.description)}', ${i.price})">
              <i class="bi bi-pencil"></i> Edit
            </button>
            <button class="btn btn-sm btn-danger" onclick="deleteItem('${i.id}')">
              <i class="bi bi-trash"></i> Delete
            </button>
          </td>
        </tr>
      `).join('');
    })
    .catch(err => {
      console.error('Error loading menu:', err);
      showNotification('Failed to load menu items', 'danger');
    });
}

// Add new item
document.getElementById("add-item-form").addEventListener("submit", e => {
  e.preventDefault();

  const name = document.getElementById("itemName").value.trim();
  const description = document.getElementById("itemDescription").value.trim();
  const price = parseFloat(document.getElementById("itemPrice").value);

  // Client-side validation
  if (name.length < 2 || name.length > 100) {
    showNotification('Item name must be between 2 and 100 characters', 'warning');
    return;
  }

  if (description.length > 500) {
    showNotification('Description must not exceed 500 characters', 'warning');
    return;
  }

  if (price <= 0) {
    showNotification('Price must be greater than 0', 'warning');
    return;
  }

  const newItem = { name, description, price };

  const submitBtn = e.target.querySelector('button[type="submit"]');
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Adding...';

  fetch('/menu', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(newItem)
  })
  .then(res => {
    if (!res.ok) {
      return res.json().then(error => {
        throw new Error(error.message || 'Failed to add item');
      });
    }
    return res.json();
  })
  .then(() => {
    showNotification('Menu item added successfully!', 'success');
    loadMenu();
    e.target.reset();
  })
  .catch(err => {
    console.error('Error adding item:', err);
    showNotification(err.message, 'danger');
  })
  .finally(() => {
    submitBtn.disabled = false;
    submitBtn.innerHTML = 'Add Item';
  });
});

// Edit item
function editItem(id, name, description, price) {
  // Create modal for editing
  const modalHtml = `
    <div class="modal fade" id="editModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-warning">
            <h5 class="modal-title"><i class="bi bi-pencil"></i> Edit Menu Item</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <form id="edit-form">
              <div class="mb-3">
                <label class="form-label">Item Name *</label>
                <input type="text" class="form-control" id="edit-name" value="${name}" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Description</label>
                <textarea class="form-control" id="edit-desc" rows="3">${description}</textarea>
              </div>
              <div class="mb-3">
                <label class="form-label">Price (₹) *</label>
                <input type="number" step="0.01" class="form-control" id="edit-price" value="${price}" required>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
            <button type="button" class="btn btn-warning" onclick="saveEdit('${id}')">
              <i class="bi bi-save"></i> Save Changes
            </button>
          </div>
        </div>
      </div>
    </div>
  `;

  const existingModal = document.getElementById('editModal');
  if (existingModal) existingModal.remove();

  document.body.insertAdjacentHTML('beforeend', modalHtml);
  const modal = new bootstrap.Modal(document.getElementById('editModal'));
  modal.show();
}

function saveEdit(id) {
  const newName = document.getElementById('edit-name').value.trim();
  const newDesc = document.getElementById('edit-desc').value.trim();
  const newPrice = parseFloat(document.getElementById('edit-price').value);

  if (!newName || newPrice <= 0) {
    showNotification('Please fill in all required fields correctly', 'warning');
    return;
  }

  fetch(`/menu/${id}`, {
    method: 'PUT',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
      name: newName,
      description: newDesc,
      price: newPrice
    })
  })
  .then(res => {
    if (!res.ok) {
      return res.json().then(error => {
        throw new Error(error.message || 'Failed to update item');
      });
    }
    return res.json();
  })
  .then(() => {
    showNotification('Menu item updated successfully!', 'success');
    loadMenu();
    bootstrap.Modal.getInstance(document.getElementById('editModal')).hide();
  })
  .catch(err => {
    console.error('Error updating item:', err);
    showNotification(err.message, 'danger');
  });
}

// Delete item
function deleteItem(id) {
  if (!confirm("Are you sure you want to delete this menu item?")) {
    return;
  }

  fetch(`/menu/${id}`, { method: 'DELETE' })
    .then(res => {
      if (!res.ok) {
        return res.json().then(error => {
          throw new Error(error.message || 'Failed to delete item');
        });
      }
      showNotification('Menu item deleted successfully!', 'success');
      loadMenu();
    })
    .catch(err => {
      console.error('Error deleting item:', err);
      showNotification(err.message, 'danger');
    });
}

// ============ ORDER MANAGEMENT ============

function loadOrders() {
  fetch('/order/all')
    .then(res => {
      if (!res.ok) throw new Error('Failed to load orders');
      return res.json();
    })
    .then(orders => {
      const tbody = document.querySelector("#orders-table tbody");

      if (orders.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="6" class="text-center">
              <div class="alert alert-info mb-0">No orders yet.</div>
            </td>
          </tr>
        `;
        return;
      }

      tbody.innerHTML = orders.map(o => {
        const statusClass = {
          'PENDING': 'warning',
          'APPROVED': 'info',
          'COMPLETED': 'success',
          'DELIVERED': 'success',
          'REJECTED': 'danger'
        }[o.status] || 'secondary';

        // Use quantity from order, default to 1 if not present
        const quantity = o.quantity || 1;

        return `
          <tr>
            <td><code>${o.id}</code></td>
            <td>${escapeHtml(o.username || 'N/A')}</td>
            <td><strong>${escapeHtml(o.itemName)}</strong></td>
            <td class="text-center"><span class="badge bg-primary">${quantity}</span></td>
            <td>
              <span class="badge bg-${statusClass}">${o.status}</span>
            </td>
            <td>
              <div class="btn-group" role="group">
                <button class="btn btn-sm btn-success" onclick="updateOrderStatus('${o.id}', 'APPROVED')"
                        ${o.status !== 'PENDING' ? 'disabled' : ''}>
                  <i class="bi bi-check"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="updateOrderStatus('${o.id}', 'REJECTED')"
                        ${o.status !== 'PENDING' ? 'disabled' : ''}>
                  <i class="bi bi-x"></i>
                </button>
                <button class="btn btn-sm btn-primary" onclick="updateOrderStatus('${o.id}', 'DELIVERED')"
                        ${o.status !== 'APPROVED' ? 'disabled' : ''}>
                  <i class="bi bi-box-seam"></i>
                </button>
              </div>
            </td>
          </tr>
        `;
      }).join('');
    })
    .catch(err => {
      console.error('Error loading orders:', err);
      showNotification('Failed to load orders', 'danger');
    });
}

// Update order status
function updateOrderStatus(orderId, status) {
  const confirmMessages = {
    'APPROVED': 'Approve this order?',
    'REJECTED': 'Reject this order?',
    'DELIVERED': 'Mark this order as delivered?'
  };

  if (!confirm(confirmMessages[status] || 'Update order status?')) {
    return;
  }

  fetch(`/order/${orderId}/status?status=${status}`, { method: 'PUT' })
    .then(res => {
      if (!res.ok) {
        return res.json().then(error => {
          throw new Error(error.message || 'Failed to update order');
        });
      }
      return res.json();
    })
    .then(() => {
      showNotification(`Order ${status.toLowerCase()} successfully!`, 'success');
      loadOrders();
    })
    .catch(err => {
      console.error('Error updating order:', err);
      showNotification(err.message, 'danger');
    });
}

// ============ UTILITIES ============

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
    toastContainer.style.zIndex = '9999';
    document.body.appendChild(toastContainer);
  }

  toastContainer.insertAdjacentHTML('beforeend', toastHtml);
  const toastElement = toastContainer.lastElementChild;
  const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
  toast.show();

  toastElement.addEventListener('hidden.bs.toast', () => toastElement.remove());
}

function escapeHtml(text) {
  if (!text) return '';
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, m => map[m]);
}