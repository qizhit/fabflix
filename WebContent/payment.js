$(document).ready(function () {
    console.log("JavaScript loaded");

    let paymentForm = $("#paymentForm");

    /**
     * Handle the data returned by the server for session info and cart
     * @param resultData JSON object with session and cart info
     */
    function handleSessionData(resultData) {
        try {
            console.log("handle session response");
            console.log(resultData);

            // Display session information
            if (resultData.sessionID && resultData.lastAccessTime) {
                $("#sessionID").text("Session ID: " + resultData.sessionID);
                $("#lastAccessTime").text("Last access time: " + resultData.lastAccessTime);
            }

            // Calculate and display total price from shopping cart items
            const shoppingCart = resultData.shoppingCart;
            if (Array.isArray(shoppingCart) && shoppingCart.length > 0) {
                const totalPrice = shoppingCart.reduce((total, item) => total + item.price * item.quantity, 0);
                $("#totalPrice").text("Total Price: $" + totalPrice.toFixed(2));
            } else {
                $("#totalPrice").text("Total Price: $0.00");
            }
        } catch (e) {
            console.error("Error handling session data:", e);
            $("#errorMessage").text("An error occurred while retrieving session data.");
        }
    }

    /**
     * Handle the response from the server after submitting payment
     * @param resultData JSON object with payment success or failure
     */
    function handlePaymentResponse(resultData) {
        try {
            console.log("handle payment response");
            console.log(resultData);

            if (resultData.success) {
                // Redirect to confirmation page on successful payment
                window.location.href = "confirmation.html";
            } else {
                // Show error message if payment failed
                $("#errorMessage").text(resultData.errorMessage || "Payment failed. Please try again.");
            }
        } catch (e) {
            console.error("Error handling payment response:", e);
            $("#errorMessage").text("An error occurred during payment processing.");
        }
    }

    /**
     * Submit payment form with POST method
     * @param event Form submit event
     */
    function handleCartInfo(event) {
        console.log("submit payment form");

        event.preventDefault();

        $.ajax("api/payment", {
            method: "POST",
            data: {
                firstName: $("#firstName").val(),
                lastName: $("#lastName").val(),
                creditCardNumber: $("#creditCardNumber").val(),
                expirationDate: $("#expirationDate").val()
            },
            success: function (resultData) {
                handlePaymentResponse(resultData);

                // Clear input form only on successful submission
                if (resultData.success) {
                    paymentForm[0].reset();
                }
            },
            error: function () {
                $("#errorMessage").text("An error occurred while processing your payment. Please try again.");
            }
        });
    }

    // Load session data to show total price
    $.ajax("api/payment", {
        method: "GET",
        dataType: "json", // Expecting JSON response
        success: handleSessionData,
        error: function () {
            console.log("Error occurred while loading session data");
            $("#errorMessage").text("An error occurred while retrieving session data.");
        }
    });

    // Bind the submit action of the form to the correct event handler function
    paymentForm.submit(handleCartInfo);
});
