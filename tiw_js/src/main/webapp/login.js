document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const bodyData = `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;

    fetch('/tiw_js/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: bodyData,
        credentials: 'include'
    })
    .then(response => {
        const contentType = response.headers.get("content-type");
        if (!response.ok) {
            if (contentType && contentType.includes("application/json")) {
                return response.json().then(json => {
                    throw new Error(json.message || 'Login failed');
                });
            }
            return response.text().then(text => {
                throw new Error('Login failed: ' + text);
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.status === "success") {
            localStorage.setItem('userId', data.userId);
            window.location.href = '/tiw_js/home.html';
        } else {
            throw new Error(data.message || 'Login failed');
        }
    })
    .catch(error => {
        console.error('Error during login:', error);
        alert('Login failed: ' + error.message);
    });
});

