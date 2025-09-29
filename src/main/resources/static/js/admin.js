document.addEventListener("DOMContentLoaded", () => {
  // Navbar
  const nav = `
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
    <div class="container">
      <a class="navbar-brand fw-bold text-warning" href="index.html">
        <i class="bi bi-bag-heart-fill"></i> QuickBite
      </a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav ms-auto">
          <li class="nav-item"><a class="nav-link" href="index.html">Home</a></li>
          <li class="nav-item"><a class="nav-link" href="menu.html">Menu</a></li>
          <li class="nav-item"><a class="nav-link" href="orders.html">Orders</a></li>
          <li class="nav-item"><a class="nav-link active" href="admin.html">Admin</a></li>
        </ul>
      </div>
    </div>
  </nav>`;
  document.body.insertAdjacentHTML("afterbegin", nav);

  loadMenu();
  loadOrders();
});

// ---------------- MENU MANAGEMENT ----------------

// Load menu items
function loadMenu() {
  fetch('/menu')
    .then(res => res.json())
    .then(items => {
      const tbody = document.querySelector("#menu-table tbody");
      tbody.innerHTML = items.map(i => `
        <tr>
          <td>${i.id}</td>
          <td>${i.name}</td>
          <td>${i.description || '-'}</td>
          <td>₹${i.price.toFixed(2)}</td>
          <td>
            <button class="btn btn-sm btn-warning me-2" onclick="editItem(${i.id}, '${i.name}', '${i.description}', ${i.price})">Edit</button>
            <button class="btn btn-sm btn-danger" onclick="deleteItem(${i.id})">Delete</button>
          </td>
        </tr>
      `).join('');
    });
}

// Add new item
document.getElementById("add-item-form").addEventListener("submit", e => {
  e.preventDefault();
  const newItem = {
    name: document.getElementById("itemName").value,
    description: document.getElementById("itemDescription").value,
    price: parseFloat(document.getElementById("itemPrice").value)
  };
  fetch('/menu', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(newItem)
  })
  .then(() => {
    alert("Item added!");
    loadMenu();
    e.target.reset();
  });
});

// Edit item
function editItem(id, name, description, price) {
  const newName = prompt("Enter new name:", name);
  const newDesc = prompt("Enter new description:", description);
  const newPrice = prompt("Enter new price:", price);

  if (newName && newPrice) {
    fetch(`/menu/${id}`, {
      method: 'PUT',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        name: newName,
        description: newDesc,
        price: parseFloat(newPrice)
      })
    })
    .then(() => {
      alert("Item updated!");
      loadMenu();
    });
  }
}

// Delete item
function deleteItem(id) {
  if (confirm("Are you sure you want to delete this item?")) {
    fetch(`/menu/${id}`, { method: 'DELETE' })
      .then(() => {
        alert("Item deleted!");
        loadMenu();
      });
  }
}

// ---------------- ORDER MANAGEMENT ----------------

// Load orders
function loadOrders() {
  fetch('/order/all')
    .then(res => res.json())
    .then(orders => {
      const tbody = document.querySelector("#orders-table tbody");
      tbody.innerHTML = orders.map(o => `
        <tr>
          <td>${o.id}</td>
          <td>${o.user ? o.user.username : 'N/A'}</td>
          <td>${o.itemName}</td>
          <td>${o.quantity}</td>
          <td><span class="badge bg-${o.status === 'PENDING' ? 'warning' : (o.status === 'APPROVED' ? 'success' : 'secondary')}">${o.status}</span></td>
          <td>
            <button class="btn btn-sm btn-success me-2" onclick="updateOrderStatus(${o.id}, 'APPROVED')">Approve</button>
            <button class="btn btn-sm btn-danger me-2" onclick="updateOrderStatus(${o.id}, 'REJECTED')">Reject</button>
            <button class="btn btn-sm btn-primary" onclick="updateOrderStatus(${o.id}, 'DELIVERED')">Deliver</button>
          </td>
        </tr>
      `).join('');
    });
}

// Update order status
function updateOrderStatus(orderId, status) {
  fetch(`/order/${orderId}/status?status=${status}`, { method: 'PUT' })
    .then(() => {
      alert(`Order ${status}!`);
      loadOrders();
    });
}
