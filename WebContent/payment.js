$(document).ready(function () {
    console.log("Payment JS loaded");
    const paymentForm = $("#payment-form");

    // 处理表单提交事件
    paymentForm.submit(function (event) {
        $("#payment-form").on("submit", function (event) {
            event.preventDefault(); // 阻止默认表单提交
            console.log("Submitting payment...");
        });

        // 获取用户输入
        const firstName = $("#first-name").val();
        const lastName = $("#last-name").val();
        const creditCardNumber = $("#credit-card-number").val();
        const expirationDate = $("#exp-date").val();

        // 发起 AJAX 请求，将支付信息发送到服务器
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
                handlePaymentResponse(resultData); // 处理服务器响应
            },
            error: function () {
                $("#error-message").text("An error occurred during payment. Please try again.");
            }
        });
    });

    // 处理服务器响应
    function handlePaymentResponse(resultData) {
        console.log("Payment response:", resultData);

        if (resultData.success) {
            window.location.href = "confirmation.html"; // 成功后跳转到确认页面
        } else {
            $("#error-message").text(resultData.message || "Payment failed. Please try again.");
        }
    }
});















// $(document).ready(function () {
//     console.log("JavaScript loaded");
//
//     let paymentForm = $("#paymentForm");
//
//     /**
//      * Handle the data returned by the server for session info and cart
//      * @param resultData JSON object with session and cart info
//      */
//     function handleSessionData(resultData) {
//
//     }
//
//     /**
//      * Handle the response from the server after submitting payment
//      * @param resultData JSON object with payment success or failure
//      */
//     function handlePaymentResponse(resultData) {
//         try {
//             console.log("handle payment response");
//             console.log(resultData);
//
//             if (resultData.success) {
//                 // Redirect to confirmation page on successful payment
//                 window.location.href = "confirmation.html";
//             } else {
//                 // Show error message if payment failed
//                 $("#errorMessage").text(resultData.errorMessage || "Payment failed. Please try again.");
//             }
//         } catch (e) {
//             console.error("Error handling payment response:", e);
//             $("#errorMessage").text("An error occurred during payment processing.");
//         }
//     }
//
//     /**
//      * Submit payment form with POST method
//      * @param event Form submit event
//      */
//     function handleCartInfo(event) {
//         console.log("submit payment form");
//
//         event.preventDefault();
//
//         $.ajax("api/payment", {
//             method: "POST",
//             data: {
//                 firstName: $("#firstName").val(),
//                 lastName: $("#lastName").val(),
//                 creditCardNumber: $("#creditCardNumber").val(),
//                 expirationDate: $("#expirationDate").val()
//             },
//             success: function (resultData) {
//                 handlePaymentResponse(resultData);
//
//                 // Clear input form only on successful submission
//                 if (resultData.success) {
//                     paymentForm[0].reset();
//                 }
//             },
//             error: function () {
//                 $("#errorMessage").text("An error occurred while processing your payment. Please try again.");
//             }
//         });
//     }
//
//     // Bind the submit action of the form to the correct event handler function
//     paymentForm.submit(handleCartInfo);
// });
