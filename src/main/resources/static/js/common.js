document.addEventListener("DOMContentLoaded", () => {
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
          <li class="nav-item"><a class="nav-link" href="login.html">Login</a></li>
        </ul>
      </div>
    </div>
  </nav>`;
  document.body.insertAdjacentHTML("afterbegin", nav);
});
