import java.util.ArrayList;
import java.util.List;

public class User {
    private final String username;
    private final List<CartItem> shoppingCart;

    public User(String username) {
        this.username = username;
        this.shoppingCart = new ArrayList<>();
    }

    public List<CartItem> getShoppingCart() {
        return shoppingCart;
    }

    public void addToCart(String movieId, String title, double price, int quantity) {
        for (CartItem item : shoppingCart) {
            if (item.getMovieId().equals(movieId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        shoppingCart.add(new CartItem(movieId, title, price, quantity));
    }

    public void removeFromCart(String movieId) {
        shoppingCart.removeIf(item -> item.getMovieId().equals(movieId));
    }

    public void updateQuantity(String movieId, int quantity) {
        for (CartItem item : shoppingCart) {
            if (item.getMovieId().equals(movieId)) {
                if (quantity > 0) {
                    item.setQuantity(quantity);
                } else {
                    shoppingCart.remove(item);
                }
                return;
            }
        }
    }
}






///**
// * This User class only has the username field in this example.
// * You can add more attributes such as the user's shopping cart items.
// */
//import java.util.ArrayList;
//import java.util.List;
//
//public class User {
//
//    private final String username;
//    private final List<CartItem> shoppingCart;
//
//    public User(String username) {
//
//        this.username = username;
//        this.shoppingCart = new ArrayList<>();
//    }
//
//
//    public List<CartItem> getShoppingCart() {
//        return shoppingCart;
//    }
//
//    public void addToCart(String movieId, String title, double price, int quantity) {
//        for (CartItem item : shoppingCart) {
//            if (item.getMovieId().equals(movieId)) {
//                item.setQuantity(item.getQuantity() + quantity); // Update quantity
//                return;
//            }
//        }
//        shoppingCart.add(new CartItem(movieId, title, price, quantity)); // Add new item if it doesn't exist
//    }
//
//    public void removeFromCart(String movieId) {
//        shoppingCart.removeIf(item -> item.getMovieId().equals(movieId));
//    }
//
//    public void updateQuantity(String movieId, int newQuantity) {
//        for (CartItem item : shoppingCart) {
//            if (item.getMovieId().equals(movieId)) {
//                if (newQuantity > 0) {
//                    item.setQuantity(newQuantity); // Set new quantity
//                } else {
//                    shoppingCart.remove(item); // Remove item if quantity is set to 0
//                }
//                break;
//            }
//        }
//    }
//
//    public double getTotalPrice() {
//        return shoppingCart.stream().mapToDouble(CartItem::getTotalPrice).sum();
//    }
//
//}
//
