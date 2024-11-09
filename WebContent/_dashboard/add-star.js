// Function to handle the addition of a new star
function addStar() {
    const starName = document.getElementById("starName").value.trim();
    const birthYear = document.getElementById("birthYear").value.trim();
    console.log("Star Name:", starName);
    console.log("Birth Year:", birthYear);

    // Prepare form data to be sent to the servlet
    const formData = new URLSearchParams();
    formData.append("starName", starName);
    formData.append("birthYear", birthYear);

    console.log("Sending POST request to /api/add_star with data:", formData.toString());

    // Send AJAX request to the servlet
    fetch("../api/_dashboard_add_star", {

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
                document.getElementById("add-star-form").reset();
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

// Add event listener for the "Add Star" button
document.getElementById("add-star-button").addEventListener("click", addStar);
