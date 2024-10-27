$(document).ready(function () {
    console.log("Payment JS loaded");
    const paymentForm = $("#payment-form");

    // 处理表单提交事件
    paymentForm.submit(function (event) {
        event.preventDefault();
        console.log("Submitting payment...");

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

    function handlePaymentResponse(resultData) {
        console.log("Payment response:", resultData);

        if (resultData.success) {
            alert("Order placed successfully!");
            window.location.href = "confirmation.html"; // 成功后跳转到确认页面
        } else {
            $("#error-message").text(resultData.message || "Payment failed. Please try again.");
        }

        if (resultData.totalPrice) {
            $("#total-price").text(`Total Price: $${resultData.totalPrice}`);
        }
    }
});
