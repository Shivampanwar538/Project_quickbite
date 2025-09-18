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
            <button class="btn btn-success w-100" onclick="placeOrder(${item.id})">Order Now</button>
          </div>
        </div>
      </div>
    `).join('');
  });

function placeOrder(menuItemId) {
  fetch('/order/place', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({userId: 1, menuItemId, quantity: 1})
  })
  .then(() => alert('Order placed!'))
  .catch(() => alert('Failed to place order'));
}
