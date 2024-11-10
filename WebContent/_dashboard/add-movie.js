// Function to handle the addition of a new movie
function addMovie() {
    const fields = ["movieTitle", "movieYear", "movieDirector", "starName","starBirthYear", "genreName"];
    const formData = new URLSearchParams();

    fields.forEach(field => {
        const value = document.getElementById(field).value.trim();
        if (value || field !== "starBirthYear") { // Only include starBirthYear if it has a value
            formData.append(field, value);
        }
        //formData.append(field, value);
    });

    console.log("fields:", fields);
    console.log("Sending POST request to /api/add_movie with data:", formData.toString());

    // Send AJAX request to the servlet
    fetch("../api/_dashboard_add-movie", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData.toString()
    })
        .then(response => response.json())
        .then(data => {
            console.log("Response data:", data);
            displayMessage(data.message, data.success ? "success" : "error");
            if (data.success) {
                document.getElementById("add-movie-form").reset();
            }
        })
        .catch(error => {
            console.error("Error:", error);
            displayMessage("An error occurred while processing your request.", "error");
        });
}

// Function to display response messages
function displayMessage(message, type) {
    const responseMessage = document.getElementById("response-message");
    responseMessage.textContent = message;

    // Apply styling based on message type
    responseMessage.style.color = type === "success" ? "green" : "red";
    responseMessage.style.fontWeight = "bold";
    responseMessage.style.marginTop = "10px";
}

// Add event listener for the "Add Movie" button
document.getElementById("add-movie-button").addEventListener("click", addMovie);
