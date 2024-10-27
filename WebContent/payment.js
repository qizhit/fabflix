$(document).ready(function () {
    console.log("JavaScript loaded");

    let paymentForm = $("#paymentForm");

    /**
     * Handle the data returned by IndexServlet
     * @param resultDataString jsonObject, consists of session info
     */
    function handleSessionData(resultDataString) {
        let resultDataJson = JSON.parse(resultDataString);

        console.log("handle session response");
        console.log(resultDataJson);
        console.log(resultDataJson["sessionID"]);

        // show the session information
        $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
        $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

        // show cart information
        const totalPrice = resultDataJson["shoppingCart"].reduce((total, item) => total + item.price * item.quantity, 0);
        $("#totalPrice").text("Total Price: $" + totalPrice.toFixed(2));

    }

    /**
     * Handle the items in item list
     * @param resultDataString jsonObject, needs to be parsed to html
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
     * Submit form content with POST method
     * @param Event
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
    $.ajax("api/checkout", {
        method: "GET",
        success: handleSessionData,
        error: function () {
            console.log("Error occurred while loading session data");
        }
    });

    // Bind the submit action of the form to the event handler function
    paymentForm.submit(handlePaymentSubmission);
});