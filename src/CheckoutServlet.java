import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 从 session 中获取购物车
        HttpSession session = request.getSession();
        ArrayList<CartItem> shoppingCart = (ArrayList<CartItem>) session.getAttribute("shoppingCart");

        // 如果购物车为空，初始化购物车并存入 session
        if (shoppingCart == null) {
            shoppingCart = new ArrayList<>();
            session.setAttribute("shoppingCart", shoppingCart);
        }

        // 将购物车数据转为 JSON 格式
        JsonArray cartJsonArray = new JsonArray();
        for (CartItem item : shoppingCart) {
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("movieId", item.getMovieId());
            itemJson.addProperty("title", item.getTitle());
            itemJson.addProperty("price", item.getPrice());
            itemJson.addProperty("quantity", item.getQuantity());
            cartJsonArray.add(itemJson);
        }

        // 返回 JSON 响应给前端
        JsonObject responseJson = new JsonObject();
        responseJson.add("shoppingCart", cartJsonArray);

        response.setStatus(200);
        response.setContentType("application/json");
        response.getWriter().write(responseJson.toString());
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        // 从 session 中获取购物车，如果为空则创建新的购物车
        ArrayList<CartItem> shoppingCart = (ArrayList<CartItem>) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            shoppingCart = new ArrayList<>();
            session.setAttribute("shoppingCart", shoppingCart);
        }

        // 获取请求参数
        String action = request.getParameter("action"); // "add", "update", "remove"
        String movieId = request.getParameter("movieId");
        String title = request.getParameter("title");
        double price = Double.parseDouble(request.getParameter("price"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        JsonObject responseJson = new JsonObject();

        // 根据 action 执行相应的操作
        synchronized (shoppingCart) {
            switch (action) {
                case "add":
                    addItemToCart(shoppingCart, movieId, title, price, quantity);
                    System.out.println(shoppingCart);
                    break;
                case "update":
                    // 注意这里的quantity是change value： +1/-1
                    updateItemQuantity(shoppingCart, movieId, quantity);
                    System.out.println(shoppingCart);
                    break;
                case "remove":
                    removeItemFromCart(shoppingCart, movieId);
                    break;
                default:
                    response.setStatus(500); // Bad request
                    responseJson.addProperty("message", "Invalid action.");
                    response.getWriter().write(responseJson.toString());
                    return;
            }
        }

        // 将购物车内容转换为 JSON 并返回给前端
        JsonArray cartJsonArray = new JsonArray();
        for (CartItem item : shoppingCart) {
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("movieId", item.getMovieId());
            itemJson.addProperty("title", item.getTitle());
            itemJson.addProperty("price", item.getPrice());
            itemJson.addProperty("quantity", item.getQuantity());
            cartJsonArray.add(itemJson);
        }

        responseJson.add("shoppingCart", cartJsonArray);
        response.setStatus(200);
        response.setContentType("application/json");
        response.getWriter().write(responseJson.toString());
    }

    // 添加商品到购物车
    private void addItemToCart(ArrayList<CartItem> cart, String movieId, String title, double price, int quantity) {
        for (CartItem item : cart) {
            if (item.getMovieId().equals(movieId)) {
                // 如果商品已经在购物车中，则增加数量
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // 如果商品不在购物车中，添加新的商品
        cart.add(new CartItem(movieId, title, price, quantity));
    }

    // 更新购物车中某个商品的数量
    private void updateItemQuantity(ArrayList<CartItem> cart, String movieId, int quantity) {
        for (CartItem item : cart) {
            if (item.getMovieId().equals(movieId)) {
                if (quantity > 0) {
                    item.setQuantity(quantity);
                } else {
                    cart.remove(item); // 如果数量为 0，移除商品
                }
                return;
            }
        }
    }

    // 从购物车中移除商品
    private void removeItemFromCart(ArrayList<CartItem> cart, String movieId) {
        cart.removeIf(item -> item.getMovieId().equals(movieId));
    }

}
