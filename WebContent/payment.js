$(document).ready(function () {
    console.log("Payment JS loaded");
    const paymentForm = $("#payment-form");

    const totalPrice = sessionStorage.getItem("totalPrice");
    if (totalPrice) {
        $("#total-price").text(`Total Price: $${totalPrice}`);
    }

    // Handle form submission events
    paymentForm.submit(function (event) {
        event.preventDefault();
        console.log("Submitting payment...");

        // Get user input
        const firstName = $("#first-name").val();
        const lastName = $("#last-name").val();
        const creditCardNumber = $("#credit-card-number").val();
        const expirationDate = $("#exp-date").val();

        // Make an AJAX request to send payment information to the server
        $.ajax({
            url: "api/payment",
            method: "POST",
            data: {
                firstName: firstName,
                lastName: lastName,
                creditCardNumber: creditCardNumber,
                expirationDate: expirationDate
            },
            success: function (resultData) {
                handlePaymentResponse(resultData); // Processing server response
            },
            error: function () {
                $("#error-message").text("An error occurred during payment. Please try again.");
            }
        });
    });

    function handlePaymentResponse(resultData) {
        console.log("Payment response:", resultData);

        if (resultData.success) {
            alert("Order placed successfully!");
            window.location.href = "confirmation.html"; // The confirmation page is displayed
        } else {
            $("#error-message").text(resultData.message || "Payment failed. Please try again.");
        }

        if (resultData.totalPrice) {
            $("#total-price").text(`Total Price: $${resultData.totalPrice}`);
        }
    }
});
