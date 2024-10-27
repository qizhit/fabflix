$(document).ready(function () {
    console.log("Checkout JS Loaded");

    // Load the cart contents and update the table and total price
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

    // Update cart table contents
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

    // Updated total price display
    function updateTotalPrice(cartItems) {
        let totalPrice = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
        $("#proceed-button").val(`Proceed to Payment - $${totalPrice.toFixed(2)}`);
    }

    // Increase or decrease the quantity of goods
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
                loadCart(); // Reload the cart contents
            },
            error: function () {
                alert("Failed to update quantity. Please try again.");
            }
        });
    });

    // Delete items from your shopping cart
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
                loadCart(); // Reload the cart contents
            },
            error: function () {
                alert("Failed to remove item from cart. Please try again.");
            }
        });
    });

    // The cart contents are loaded when the page loads
    loadCart();
});