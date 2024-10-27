$(document).ready(function () {
    console.log("JavaScript loaded");

    let cart = $("#cart");

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
        handleCartArray(resultDataJson["shoppingCart"]);
    }

    /**
     * Handle the items in item list
     * @param resultArray jsonObject, needs to be parsed to html
     */
    function handleCartArray(cartItems) {
        console.log("Cart Items:", cartItems);
        // change it to html list
        let itemListElement = $("#item_list");
        let cartContent = "<ul>";

        if (cartItems.length === 0) {
            cartContent = "<p>Your cart is empty</p>";
        } else {
            cartItems.forEach(item => {
                cartContent += `<li>
                                    ${item.title} - Quantity: ${item.quantity}, 
                                    Price: $${item.price.toFixed(2)}, 
                                    Total: $${(item.quantity * item.price).toFixed(2)}
                                </li>`;
            });
            cartContent += "</ul>";
        }

        // Refresh the item list on the front end
        itemListElement.html(cartContent);
    }

    /**
     * Submit form content with POST method
     * @param cartEvent
     */
    function handleCartInfo(cartEvent) {
        console.log("submit cart form");
        /**
         * When users click the submit button, the browser will not direct
         * users to the url defined in HTML form. Instead, it will call this
         * event handler when the event is triggered.
         */
        cartEvent.preventDefault();

        $.ajax("api/checkout", {
            method: "POST",
            data: {
                item: $("#item").val(),
                action: "add"
            },
            success: function (resultDataString) {
                let resultDataJson = JSON.parse(resultDataString);
                handleCartArray(resultDataJson["shoppingCart"]);
            },
            error: function () {
                console.log("Error occurred while submitting the cart data");
            }
        });

        // clear input form
        cart[0].reset();
    }

    $.ajax("api/checkout", {
        method: "GET",
        success: handleSessionData,
        error: function () {
            console.log("Error occurred while loading session data");
        }
    });

// Bind the submit action of the form to a event handler function
    cart.submit(handleCartInfo);
});
