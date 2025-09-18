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

    localStorage.setItem('userId', data.id);
    localStorage.setItem('role', data.role);
    localStorage.setItem("username", data.username);
    alert(`Welcome ${data.username}`);
    if (data.role === 'ADMIN') {
      window.location.href = 'admin.html';
    } else {
      window.location.href = 'menu.html';
    }
  })
  .catch(() => alert('Invalid credentials or login failed'));
}

// ===================== REGISTER =====================
function register() {
  const username = document.getElementById('reg-username').value.trim();
  const password = document.getElementById('reg-password').value.trim();

  console.log("Sending:", username, password); // Debug
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
    document.getElementById('register-msg').textContent =
      '✅ Registered successfully! You can now log in.';
    alert('Registration successful!');
    window.location.href = 'menu.html';
  })
  .catch(err => {
    console.error("Register error:", err);
    document.getElementById('register-msg').textContent =
      '❌ Registration failed';
  });
}
