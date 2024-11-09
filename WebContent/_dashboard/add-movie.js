// Function to handle the addition of a new movie
function addMovie() {
    const movieTitle = document.getElementById("movieTitle").value.trim();
    const movieYear = document.getElementById("movieYear").value.trim();
    const movieDirector = document.getElementById("movieDirector").value.trim();
    const starName = document.getElementById("starName").value.trim();
    const genreName = document.getElementById("genreName").value.trim();

    console.log("Movie Title:", movieTitle);
    console.log("Movie Year:", movieYear);
    console.log("Movie Director:", movieDirector);
    console.log("Star Name:", starName);
    console.log("Genre Name:", genreName);

    // Validate required fields
    if (!movieTitle || !movieYear || !movieDirector || !starName || !genreName) {
        displayMessage("All fields are required.", "error");
        return;
    }

    // Prepare form data to be sent to the servlet
    const formData = new URLSearchParams();
    formData.append("movie_title", movieTitle);
    formData.append("movie_year", movieYear);
    formData.append("movie_director", movieDirector);
    formData.append("star_name", starName);
    formData.append("genre_name", genreName);

    console.log("Sending POST request to /api/add_movie with data:", formData.toString());

    // Send AJAX request to the servlet
    fetch("../api/add_movie", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData.toString()
    })
        .then(response => {
            console.log("Response status:", response.status);
            // Check if the response is JSON; if not, handle it as an error
            if (!response.ok) {
                throw new Error("Network response was NOT ok.");
            }
            return response.json();
        })
        .then(data => {
            console.log("Response data:", data);
            // Display success or error message based on the server response
            if (data.success) {
                displayMessage(data.message, "success");
                document.getElementById("add-movie-form").reset(); // Clear the form on success
            } else {
                displayMessage(data.message, "error");
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
