fetch('/menu')
  .then(res => res.json())
  .then(menuItems => {
    const container = document.getElementById('menu-container');
    container.innerHTML = menuItems.map(item => `
      <div class="col-md-4 mb-3">
        <div class="card shadow-sm h-100">
          <div class="card-body">
            <h5 class="card-title">${item.name}</h5>
            <p class="card-text">
              <span class="badge bg-info">${item.description}</span>
              <br>₹${item.price}
            </p>
            <button class="btn btn-success w-100" onclick="placeOrder('${item.id}')">Order Now</button>
          </div>
        </div>
      </div>
    `).join('');
  });

function placeOrder(menuItemId) {
  // Get userId from localStorage, default to a sample user ID if not found
  const userId = localStorage.getItem('userId') || getDefaultUserId();

  if (!userId) {
    alert('Please login first to place an order!');
    window.location.href = 'login.html';
    return;
  }

  fetch('/order/place', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
      userId: userId,
      menuItemId: menuItemId,
      quantity: 1
    })
  })
  .then(res => {
    if (!res.ok) {
      throw new Error('Failed to place order');
    }
    return res.json();
  })
  .then(() => {
    alert('Order placed successfully!');
  })
  .catch(err => {
    console.error('Order failed:', err);
    alert('Failed to place order. Please try again.');
  });
}

// Helper function to get a default user ID for testing
function getDefaultUserId() {
  // This will fetch the first user's ID for testing purposes
  // In production, you should always require proper authentication
  return fetch('/auth')
    .then(res => res.json())
    .then(users => users.length > 0 ? users[0].id : null)
    .catch(() => null);
}