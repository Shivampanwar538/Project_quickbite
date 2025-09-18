// ===================== LOGIN =====================
function login() {
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;

  fetch('/auth/login', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    credentials: 'include', // ✅ Send cookies with request
    body: JSON.stringify({username, password})
  })
  .then(res => {
    if (!res.ok) throw new Error('Login failed');
    return res.json();
  })
  .then(data => {
    // ✅ Save user details in localStorage for frontend checks
    localStorage.setItem('userId', data.id);
    localStorage.setItem('role', data.role);
    alert('Login successful!');
    window.location.href = 'menu.html';
  })
  .catch(() => alert('Invalid credentials or login failed'));
}

// ===================== REGISTER =====================
function register() {
  const username = document.getElementById('reg-username').value.trim();
  const password = document.getElementById('reg-password').value.trim();

  if (!username || !password) {
    document.getElementById('register-msg').textContent = '⚠️ Please fill in all fields';
    return;
  }

  fetch('/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
  .then(res => {
    if (!res.ok) throw new Error('Failed to register');
    return res.json();
  })
  .then(() => {
    document.getElementById('register-msg').textContent = '✅ Registered successfully! You can now log in.';
  })
  .catch(() => {
    document.getElementById('register-msg').textContent = '❌ Registration failed';
  });
}
