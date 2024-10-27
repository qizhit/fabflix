$(document).ready(function () {
    console.log("Confirmation JS loaded");

    // 调用 API 获取销售数据并填充表格
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

    // 填充确认表格数据
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

    // 更新总价显示
    function updateTotalCost(totalPrice) {
        $("#total-cost").text(`$${totalPrice.toFixed(2)}`);
    }

    // 页面加载时获取确认数据
    loadConfirmationData();
});
