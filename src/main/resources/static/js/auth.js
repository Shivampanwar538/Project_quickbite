// ===================== UTILITIES =====================
function showError(elementId, message) {
  const errorElement = document.getElementById(elementId);
  if (errorElement) {
    errorElement.textContent = message;
    errorElement.className = 'alert alert-danger show';
    errorElement.style.display = 'block';
    setTimeout(() => {
      errorElement.style.display = 'none';
    }, 5000);
  }
}

function showSuccess(elementId, message) {
  const successElement = document.getElementById(elementId);
  if (successElement) {
    successElement.textContent = message;
    successElement.className = 'alert alert-success show';
    successElement.style.display = 'block';
    setTimeout(() => {
      successElement.style.display = 'none';
    }, 3000);
  }
}

function displayValidationErrors(errors) {
  let errorMessage = 'Validation failed:\n';
  for (const [field, message] of Object.entries(errors)) {
    errorMessage += `${field}: ${message}\n`;
  }
  return errorMessage;
}

// ===================== LOGIN =====================
function login() {
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value.trim();

  // Client-side validation
  if (!username || username.length < 3) {
    showError('login-msg', 'Username must be at least 3 characters');
    return;
  }

  if (!password || password.length < 6) {
    showError('login-msg', 'Password must be at least 6 characters');
    return;
  }

  // Check for symbol in password
  const symbolPattern = /[@#$%^&+=!*()]/;
  if (!symbolPattern.test(password)) {
    showError('login-msg', 'Password must contain at least one symbol (@#$%^&+=!*())');
    return;
  }

  // Show loading state
  const loginBtn = document.querySelector('button[onclick="login()"]');
  const originalText = loginBtn.textContent;
  loginBtn.textContent = 'Logging in...';
  loginBtn.disabled = true;

  fetch('/auth/login', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    credentials: 'include',
    body: JSON.stringify({username, password})
  })
  .then(res => {
    if (!res.ok) {
      return res.json().then(error => {
        throw new Error(error.message || 'Login failed');
      });
    }
    return res.json();
  })
  .then(data => {
    // Store user data
    localStorage.setItem('userId', data.id);
    localStorage.setItem('role', data.role);
    localStorage.setItem('username', data.username);

    showSuccess('login-msg', `Welcome ${data.username}!`);

    // Redirect based on role
    setTimeout(() => {
      if (data.role === 'ADMIN') {
        window.location.href = 'admin.html';
      } else {
        window.location.href = 'menu.html';
      }
    }, 1000);
  })
  .catch(err => {
    console.error('Login error:', err);
    showError('login-msg', err.message);
  })
  .finally(() => {
    loginBtn.textContent = originalText;
    loginBtn.disabled = false;
  });
}

// ===================== REGISTER =====================
function register() {
  const username = document.getElementById('reg-username').value.trim();
  const password = document.getElementById('reg-password').value.trim();

  // Client-side validation
  if (!username || username.length < 3 || username.length > 50) {
    showError('register-msg', 'Username must be between 3 and 50 characters');
    return;
  }

  // Check username pattern (alphanumeric and underscore only)
  const usernamePattern = /^[a-zA-Z0-9_]+$/;
  if (!usernamePattern.test(username)) {
    showError('register-msg', 'Username can only contain letters, numbers, and underscores');
    return;
  }

  if (!password || password.length < 6) {
    showError('register-msg', 'Password must be at least 6 characters');
    return;
  }

  // Check for symbol in password
  const symbolPattern = /[@#$%^&+=!*()]/;
  if (!symbolPattern.test(password)) {
    showError('register-msg', 'Password must contain at least one symbol (@#$%^&+=!*())');
    return;
  }

  // Show loading state
  const registerBtn = document.querySelector('button[onclick="register()"]');
  const originalText = registerBtn.textContent;
  registerBtn.textContent = 'Registering...';
  registerBtn.disabled = true;

  console.log("Registering user:", username);

  fetch('/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
  .then(res => {
    if (!res.ok) {
      return res.json().then(error => {
        // Handle validation errors
        if (error.errors) {
          const errorMsg = displayValidationErrors(error.errors);
          throw new Error(errorMsg);
        }
        throw new Error(error.message || 'Registration failed');
      });
    }
    return res.json();
  })
  .then(data => {
    console.log("Registration successful:", data);

    // Store user data
    localStorage.setItem('userId', data.id);
    localStorage.setItem('username', data.username);
    localStorage.setItem('role', data.role);

    showSuccess('register-msg', 'Registration successful! Redirecting...');

    // Redirect to menu page
    setTimeout(() => {
      window.location.href = 'menu.html';
    }, 1500);
  })
  .catch(err => {
    console.error("Registration error:", err);
    showError('register-msg', err.message);
  })
  .finally(() => {
    registerBtn.textContent = originalText;
    registerBtn.disabled = false;
  });
}

// ===================== LOGOUT =====================
function logout() {
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  localStorage.removeItem('role');

  // Show success message if on a page with message container
  const msgElement = document.getElementById('login-msg') || document.getElementById('register-msg');
  if (msgElement) {
    showSuccess(msgElement.id, 'Logged out successfully');
  }

  // Redirect to home page
  setTimeout(() => {
    window.location.href = 'index.html';
  }, 500);
}

// ===================== CHECK AUTH STATUS =====================
function checkAuthStatus() {
  const userId = localStorage.getItem('userId');
  const username = localStorage.getItem('username');

  if (userId && username) {
    return {
      isAuthenticated: true,
      userId: userId,
      username: username,
      role: localStorage.getItem('role') || 'STUDENT'
    };
  }

  return {
    isAuthenticated: false
  };
}

// ===================== ENTER KEY SUPPORT =====================
document.addEventListener('DOMContentLoaded', () => {
  // Login page
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');

  if (usernameInput && passwordInput) {
    [usernameInput, passwordInput].forEach(input => {
      input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
          login();
        }
      });
    });
  }

  // Register page
  const regUsernameInput = document.getElementById('reg-username');
  const regPasswordInput = document.getElementById('reg-password');

  if (regUsernameInput && regPasswordInput) {
    [regUsernameInput, regPasswordInput].forEach(input => {
      input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
          register();
        }
      });
    });
  }
});