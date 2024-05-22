document.getElementById('registrationForm').addEventListener('submit', function(event) {
    // Prevent the default form submission
    event.preventDefault();

    // Retrieve the values from the form fields
    const email = document.getElementById('email').value;
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Basic validation for email address
    if (!email.includes('@')) {
        alert('Please enter a valid email address.');
        return;
    }

    // Check if passwords match
    if (password !== confirmPassword) {
        alert('Passwords do not match.');
        return;
    }

    // Construct the request body with URL-encoded form data
    const bodyData = `email=${encodeURIComponent(email)}&username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;

    // Log the AJAX request to the server
    console.log("Sending AJAX request to server.");

    // Make the AJAX request to the server's register endpoint
    fetch('/tiw_js/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: bodyData
    })
    .then(response => {
        // Log the receipt of the response
        console.log("Received response from server.");

        // Check if the response was successful
        if (response.ok) {
            // Parse the response body as text
            return response.text();
        } else {
            // If the response is not successful, parse the text and reject the promise
            return response.text().then(text => Promise.reject(text));
        }
    })
    .then(data => {
        // Handle the successful registration response
        if (data === "Registration successful") {
            alert("Registration successful!");
            // Redirect the user to the login page
            window.location.href = '/tiw_js/login.html';
        } else {
            // Alert the user with the message from the server
            alert(data);
        }
    })
    .catch(error => {
        // Log and alert any errors
        console.error('Error during registration:', error);
        alert("Registration failed: " + error);
    });
});
