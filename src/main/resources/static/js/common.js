document.addEventListener("DOMContentLoaded", () => {
  const auth = checkAuthStatus();

  // Build navbar based on auth status
  let navLinks = '';

  if (auth.isAuthenticated) {
    // Logged in - show user info and logout
    navLinks = `
      <li class="nav-item"><a class="nav-link" href="index.html">Home</a></li>
      <li class="nav-item"><a class="nav-link" href="menu.html">Menu</a></li>
      <li class="nav-item"><a class="nav-link" href="orders.html">My Orders</a></li>
      ${auth.role === 'ADMIN' ? '<li class="nav-item"><a class="nav-link" href="admin.html">Admin</a></li>' : ''}
      <li class="nav-item">
        <span class="nav-link text-warning">
          <i class="bi bi-person-circle"></i> ${auth.username} (${auth.role})
        </span>
      </li>
      <li class="nav-item">
        <a class="nav-link btn btn-outline-danger btn-sm" href="#" onclick="logout(); return false;">
          <i class="bi bi-box-arrow-right"></i> Logout
        </a>
      </li>
    `;
  } else {
    // Not logged in - show login and register
    navLinks = `
      <li class="nav-item"><a class="nav-link" href="index.html">Home</a></li>
      <li class="nav-item"><a class="nav-link" href="menu.html">Menu</a></li>
      <li class="nav-item"><a class="nav-link" href="login.html">Login</a></li>
      <li class="nav-item"><a class="nav-link" href="register.html">Register</a></li>
    `;
  }

  const nav = `
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm fixed-top">
    <div class="container">
      <a class="navbar-brand fw-bold text-warning" href="index.html">
        <i class="bi bi-bag-heart-fill"></i> QuickBite
      </a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav ms-auto">
          ${navLinks}
        </ul>
      </div>
    </div>
  </nav>`;

  document.body.insertAdjacentHTML("afterbegin", nav);
});