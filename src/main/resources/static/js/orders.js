fetch('/order/pending')
  .then(res => res.json())
  .then(orders => {
    const table = document.getElementById('orders-table');
    table.innerHTML = orders.map(o => `
      <tr>
        <td>${o.id}</td>
        <td>${o.itemName}</td>
        <td>${o.quantity}</td>
        <td><span class="badge bg-${o.status === 'PENDING' ? 'warning' : 'success'}">${o.status}</span></td>

      </tr>
    `).join('');
  });
