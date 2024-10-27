$(document).ready(function () {
    console.log("Confirmation JS loaded");

    // Call the API to get the sales data and fill the table
    function loadConfirmationData() {
        $.ajax({
            url: "api/confirm", // 对应 ConfirmationServlet
            method: "GET",
            success: function (response) {
                populateConfirmationTable(response.sales);
                updateTotalCost(response.totalPrice);
            },
            error: function () {
                alert("Failed to load confirmation details. Please try again.");
            }
        });
    }

    // Fill in the confirmation form data
    function populateConfirmationTable(sales) {
        let confirmationBody = $("#confirmation-body");
        confirmationBody.empty(); // 清空旧数据

        sales.forEach(sale => {
            const rowHTML = `
                <tr>
                    <td>${sale.saleId}</td>
                    <td>${sale.title}</td>
                    <td>${sale.quantity}</td>
                    <td>$${sale.price.toFixed(2)}</td>
                    <td>$${sale.singleMovieTotalPrice.toFixed(2)}</td>
                </tr>`;
            confirmationBody.append(rowHTML);
        });
    }

    // Updated total price display
    function updateTotalCost(totalPrice) {
        $("#total-cost").text(`$${totalPrice.toFixed(2)}`);
    }

    // Get confirmation data when the page loads
    loadConfirmationData();
});
