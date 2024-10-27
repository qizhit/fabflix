$(document).ready(function () {
    console.log("Checkout JS Loaded");

    // 加载购物车内容并更新表格和总价
    function loadCart() {
        $.ajax({
            url: "api/checkout",
            method: "GET",
            success: function (response) {
                updateCartTable(response.shoppingCart);
                updateTotalPrice(response.shoppingCart);
            },
            error: function () {
                alert("Failed to load cart. Please refresh the page.");
            }
        });
    }

    // 更新购物车表格内容
    function updateCartTable(cartItems) {
        let cartBody = $("#cart-body");
        cartBody.empty(); // 清空旧的内容

        cartItems.forEach(item => {
            const total = (item.price * item.quantity).toFixed(2); // 计算每项的总价
            const rowHTML = `
                <tr>
                    <td>${item.title}</td>
                    <td>
                        <button class="quantity-btn" data-id="${item.movieId}" data-title="${item.title}" 
                                data-quantity="${item.quantity}" data-price="${item.price}" data-action="decrease">↓</button>
                        <span>${item.quantity}</span>
                        <button class="quantity-btn" data-id="${item.movieId}" data-title="${item.title}" 
                                data-quantity="${item.quantity}" data-price="${item.price}" data-action="increase">↑</button>
                    </td>
                    <td><button class="delete-btn" data-id="${item.movieId}" data-title="${item.title}" 
                                data-quantity="${item.quantity}" data-price="${item.price}" >X</button></td>
                    <td>$${item.price.toFixed(2)}</td>
                    <td>$${total}</td>
                </tr>`;
            cartBody.append(rowHTML);
        });
    }

    // 更新总价显示
    function updateTotalPrice(cartItems) {
        let totalPrice = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
        $("#proceed-button").val(`Proceed to Payment - $${totalPrice.toFixed(2)}`);
    }

    // 增加或减少商品数量
    $(document).on('click', '.quantity-btn', function () {
        const movieId = $(this).data('id');
        const action = $(this).data('action');
        const movieTitle = $(this).data('title');
        const moviePrice = $(this).data('price');
        const currentQuantity = $(this).data('quantity');
        const newQuantity = action === 'increase' ? currentQuantity + 1 : currentQuantity - 1;

        $.ajax({
            url: "api/checkout",
            method: "POST",
            data: {
                action: "update",
                movieId: movieId,
                title: movieTitle,
                price: moviePrice,
                quantity: newQuantity
            },
            success: function () {
                loadCart(); // 重新加载购物车内容
            },
            error: function () {
                alert("Failed to update quantity. Please try again.");
            }
        });
    });

    // 删除购物车中的商品
    $(document).on('click', '.delete-btn', function () {
        const movieId = $(this).data('id');
        const movieTitle = $(this).data('title');
        const moviePrice = $(this).data('price');
        const movieQuantity = $(this).data('quantity');

        $.ajax({
            url: "api/checkout",
            method: "POST",
            data: {
                action: "remove",
                movieId: movieId,
                title: movieTitle,
                price: moviePrice,
                quantity: movieQuantity
            },
            success: function () {
                loadCart(); // 重新加载购物车内容
            },
            error: function () {
                alert("Failed to remove item from cart. Please try again.");
            }
        });
    });

    // 页面加载时加载购物车内容
    loadCart();
});