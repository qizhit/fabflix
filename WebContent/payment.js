$(document).ready(function () {
    console.log("JavaScript loaded");

    let paymentForm = $("#paymentForm");

    /**
     * Handle the data returned by the server for session info and cart
     * @param resultDataString JSON object with session and cart info
     */
    function handleSessionData(resultDataString) {
        try {
            let resultDataJson = JSON.parse(resultDataString);


            console.log("handle session response");
            console.log(resultDataJson);

            // Display session information
            $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
            $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

            // Calculate and display total price from shopping cart items
            const shoppingCart = resultDataJson["shoppingCart"];
            if (shoppingCart && shoppingCart.length > 0) {
                const totalPrice = shoppingCart.reduce((total, item) => total + item.price * item.quantity, 0);
                $("#totalPrice").text("Total Price: $" + totalPrice.toFixed(2));
            } else {
                $("#totalPrice").text("Total Price: $0.00");
            }
        } catch (e) {
            console.error("Error parsing JSON response:", e);
        }
    }

    /**
     * Handle the response from the server after submitting payment
     * @param resultDataString JSON object with payment success or failure
     */
    function handlePaymentResponse(resultDataString) {
        let resultDataJson = JSON.parse(resultDataString);

        console.log("handle payment response");
        console.log(resultDataJson);

        if (resultDataJson.success) {
            // Redirect to confirmation page on successful payment
            window.location.href = "confirmation.html";
        } else {
            // Show error message if payment failed
            $("#errorMessage").text(resultDataJson.errorMessage);
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
            success: handlePaymentResponse,
            error: function () {
                $("#errorMessage").text("An error occurred while processing your payment. Please try again.");
            }
        });

        // Clear input form after submission
        paymentForm[0].reset();
    }

    // Load session data to show total price
    $.ajax("api/payment", {
        method: "GET",
        dataType:"json",
        success: handleSessionData,
        error: function () {
            console.log("Error occurred while loading session data");
        }
    });

    // Bind the submit action of the form to the correct event handler function
    paymentForm.submit(handleCartInfo);
});
