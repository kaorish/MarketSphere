package servlet;

import dao.*;
import org.apache.commons.lang.math.RandomUtils;
import bean.*;
import org.springframework.web.util.HtmlUtils;
import util.HttpUtils;
import util.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ForeServlet extends BaseForeServlet {

    // 从CategoryDAO中获取数据，然后再将数据传递给 JSP 文件，也就是页面跳转到home.jsp并将图片展示出来，因为页面需要用循环来展示商品和图片
    public String home(HttpServletRequest request, HttpServletResponse response, Page page) {
        List<Category> categories = new CategoryDAO().list();
        new ProductDAO().fill(categories);
        new ProductDAO().fillByRow(categories);
        request.setAttribute("categories", categories);
        return "home.jsp";
    }

    // SHA-256 + 加盐加密
    public String register(HttpServletRequest request, HttpServletResponse response, Page page) {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");

        // 转义 HTML，防止 XSS 攻击
        name = HtmlUtils.htmlEscape(name);
        System.out.println("Register Name: " + name);

        // 检查用户名是否已存在
        boolean exist = userDAO.isExist(name);
        if (exist) {
            request.setAttribute("msg", "用户名已经被使用，不能使用");
            return "register.jsp";
        }

        // 生成随机盐值
        String salt = generateSalt();
        System.out.println("Generated Salt: " + salt);

        // 使用盐值对密码进行 SHA-256 加密
        String encryptedPassword = hashPasswordWithSalt(password, salt);

        // 创建用户并存储到数据库
        User user = new User();
        user.setName(name);
        user.setPassword(encryptedPassword);
        user.setSalt(salt); // 将盐值存储到数据库
        user.setPhone(phone);
        System.out.println("Encrypted Password: " + encryptedPassword);

        userDAO.add(user);

        return "@registerSuccess.jsp"; // 重定向到注册成功页面
    }

    // 发送短信验证码
    public String sendCode(HttpServletRequest request, HttpServletResponse response, Page page) {
        response.setContentType("application/json;charset=UTF-8");
        String phone = request.getParameter("phone");
        System.out.println("Phone: " + phone);
        try {
            // 生成验证码
            String code = generateVerificationCode();
            boolean success = sendSms(phone, code); // 假设成功发送短信

            if (success) {
                // 将验证码存入 session
                request.getSession().setAttribute("verificationCode", code);
                System.out.println("Verification Code: " + code);
                String jsonResponse = "{\"success\": true, \"message\": \"Test response successful\"}";
                System.out.println("Before writing response");
                PrintWriter writer = response.getWriter();
                writer.write(jsonResponse);
                writer.flush(); // 刷新流
                writer.close(); // 明确关闭流
                System.out.println("After writing response");
                return null;
            } else {
                String jsonResponse = "{\"success\": false, \"message\": \"验证码发送失败\"}";
                PrintWriter writer = response.getWriter();
                writer.write(jsonResponse);
                writer.flush(); // 刷新流
                writer.close(); // 明确关闭流
                System.out.println("After writing response");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public String verifyCode(HttpServletRequest request, HttpServletResponse response, Page page) {
        System.out.println("this is verifyCode");
        response.setContentType("application/json;charset=UTF-8");
        String inputCode = request.getParameter("verificationCode");
        System.out.println("Input Code: " + inputCode);

        try {
            // 获取会话中的验证码
            String sessionCode = (String) request.getSession().getAttribute("verificationCode");

            if (sessionCode == null) {
                response.getWriter().write("{\"success\": false, \"message\": \"验证码已失效，请重新发送\"}");
                return null;
            }

            if (sessionCode.equals(inputCode)) {
                String jsonResponse = "{\"success\": true, \"message\": \"验证码验证成功\"}";
                System.out.println("verification success");
                PrintWriter writer = response.getWriter();
                writer.write(jsonResponse);
                writer.flush(); // 刷新流
                writer.close(); // 明确关闭流
            } else {
                String jsonResponse = "{\"success\": false, \"message\": \"验证码错误\"}";
                System.out.println("verification failed");
                PrintWriter writer = response.getWriter();
                writer.write(jsonResponse);
                writer.flush(); // 刷新流
                writer.close(); // 明确关闭流
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.getWriter().write("{\"success\": false, \"message\": \"服务器内部错误\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return null;
    }

    // 生成 6 位随机验证码
    private String generateVerificationCode() {
        String str = "0123456789";
        String code = "";
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * str.length());
            code += str.charAt(index);
        }
        return code;
    }

    private boolean sendSms(String phone, String code) {
        // 验证码
        String variables = code;
        // 验证码模版id
        String templateId = "CST_ptdie100";

        boolean success = false;

        String host = "https://dfsns.market.alicloudapi.com";
        String path = "/data/send_sms";
        String method = "POST";
        String appcode = "f73cdd6604394778862c75dcc60d075a";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", "code:" + variables);
        bodys.put("template_id", templateId);  //注意，CST_ptdie100该模板ID仅为调试使用，调试结果为"status": "OK" ，即表示接口调用成功，然后联系客服报备自己的专属签名模板ID，以保证短信稳定下发
        bodys.put("phone_number", phone);


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            org.apache.http.HttpResponse  response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
//            System.out.println(EntityUtils.toString(response.getEntity()));
            if (response.toString().contains("OK")) success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public String login(HttpServletRequest request, HttpServletResponse response, Page page) {
        String name = request.getParameter("name");
        String password = request.getParameter("password");

        // 转义 HTML，防止 XSS 攻击
        name = HtmlUtils.htmlEscape(name);

        // 根据用户名从数据库中获取用户信息（包括盐值）
        User user = userDAO.get(name);
        if (user == null) {
            request.setAttribute("msg", "账号密码错误");
            return "login.jsp";
        }

        // 使用存储的盐值对输入的密码进行加密
        String encryptedPassword = hashPasswordWithSalt(password, user.getSalt());

        // 验证加密后的密码是否与数据库中的密码匹配
        if (!encryptedPassword.equals(user.getPassword())) {
            request.setAttribute("msg", "账号密码错误");
            return "login.jsp";
        }

        // 登录成功
        //将用户存储到 Session
        request.getSession().setAttribute("user", user);

        // 重定向到首页，这里一定要用@forehome，因为要被过滤器拦截，然后加上必要的参数，这样才能正确显示商品的各种图片等等信息
        // 如果填@home.jsp或home.jsp，那么就是直接重定向回home.jsp，就会缺失这些信息，和在浏览器中直接输入没啥区别，不能正常显示
        return "@forehome";
    }

    /**
     * 生成随机盐值
     * @return 随机盐值
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 使用 SHA-256 和盐值对密码进行加密
     * @param password 明文密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    private String hashPasswordWithSalt(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hash = md.digest(saltedPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 加密失败", e);
        }
    }



    public String logout(HttpServletRequest request, HttpServletResponse response, Page page) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            // 移除 Session 中的用户信息
            request.getSession().removeAttribute("user");
        }
        return "@forehome";
    }

    public String product(HttpServletRequest request, HttpServletResponse response, Page page) {
        int pid = Integer.parseInt(request.getParameter("pid"));
        Product p = productDAO.get(pid);

        List<ProductImage> productSingleImages = productImageDAO.list(p, ProductImageDAO.type_single);
        List<ProductImage> productDetailImages = productImageDAO.list(p, ProductImageDAO.type_detail);
        p.setProductSingleImages(productSingleImages);
        p.setProductDetailImages(productDetailImages);

        List<PropertyValue> pvs = propertyValueDAO.list(p.getId());
        List<Review> reviews = reviewDAO.list(p.getId());

        productDAO.setSaleAndReviewNumber(p);

        request.setAttribute("reviews", reviews);

        request.setAttribute("p", p);
        request.setAttribute("pvs", pvs);
        return "product.jsp";
    }

    public String checkLogin(HttpServletRequest request, HttpServletResponse response, Page page) {
        User user = (User) request.getSession().getAttribute("user");
        if (null != user)
            return "%success";
        return "%fail";
    }

    public String loginAjax(HttpServletRequest request, HttpServletResponse response, Page page) {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        User user = userDAO.get(name, password);

        if (null == user) {
            return "%fail";
        }
        request.getSession().setAttribute("user", user);
        return "%success";
    }

    public String category(HttpServletRequest request, HttpServletResponse response, Page page) {
        int cid = Integer.parseInt(request.getParameter("cid"));
        Category category = new CategoryDAO().get(cid);
        new ProductDAO().fill(category);
        new ProductDAO().setSaleAndReviewNumber(category.getProducts());

        String sort = request.getParameter("sort");
        if (null != sort) {
            switch (sort) {
                case "review":
                    Collections.sort(category.getProducts(), new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return p2.getReviewCount() - p1.getReviewCount();
                        }
                    });
                    break;
                case "date":
                    Collections.sort(category.getProducts(), new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return p1.getCreateDate().compareTo(p2.getCreateDate());
                        }
                    });
                    break;
                case "saleCount":
                    Collections.sort(category.getProducts(), new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return p1.getSaleCount() - p2.getSaleCount();
                        }
                    });
                    break;
                case "price":
                    Collections.sort(category.getProducts(), new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return (int) (p1.getPromotePrice() - p2.getPromotePrice());
                        }
                    });
                    break;
                case "all":
                    Collections.sort(category.getProducts(), new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return p2.getReviewCount() * p2.getSaleCount() - p1.getReviewCount() * p1.getSaleCount();
                        }
                    });
                    break;

            }
        }
        request.setAttribute("category", category);
        return "category.jsp";
    }

    public String search(HttpServletRequest request, HttpServletResponse response, Page page) {
        String keyword = request.getParameter("keyword");
        List<Product> ps = new ProductDAO().search(keyword, 0, 20);
        request.setAttribute("ps", ps);
        return "searchResult.jsp";
    }

    public String buyone(HttpServletRequest request, HttpServletResponse response, Page page) {
        int pid = Integer.parseInt(request.getParameter("pid"));
        int num = Integer.parseInt(request.getParameter("num"));
        Product p = productDAO.get(pid);
        int orderItemId = 0;

        User user = (User) request.getSession().getAttribute("user");
        //遍历此用户对应的所有订单项，如果其产品id等于p.getId()，就对对应订单项的数量追加，也就是再买一件然后结账
        boolean found = false;
        List<OrderItem> orderItems = orderItemDAO.listByUser(user.getId());
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getProduct().getId() == p.getId()) {
                orderItem.setNumber(orderItem.getNumber() + num);
                orderItemDAO.update(orderItem);
                found = true;
                orderItemId = orderItem.getId();
                break;
            }
        }
        if (!found) {
            OrderItem orderItem = new OrderItem();
            orderItem.setUser(user);
            orderItem.setNumber(num);
            orderItem.setProduct(p);
            orderItemDAO.add(orderItem);
            orderItemId = orderItem.getId();
        }
        return "@forebuy?orderItemId=" + orderItemId;
    }

    public String buy(HttpServletRequest request, HttpServletResponse response, Page page) {
        String[] oiids = request.getParameterValues("orderItemId");
        List<OrderItem> orderItems = new ArrayList<>();
        float total = 0;
        for (String strid : oiids) {
            int orderItemId = Integer.parseInt(strid);
            OrderItem orderItem = orderItemDAO.get(orderItemId);
            total += orderItem.getProduct().getPromotePrice() * orderItem.getNumber();
            orderItems.add(orderItem);
        }
        request.getSession().setAttribute("orderItems", orderItems);
        request.setAttribute("total", total);
        return "buy.jsp";
    }

    public String addCart(HttpServletRequest request, HttpServletResponse response, Page page) {
        int pid = Integer.parseInt(request.getParameter("pid"));
        Product product = productDAO.get(pid);
        int num = Integer.parseInt(request.getParameter("num"));

        User user = (User) request.getSession().getAttribute("user");
        boolean found = false;

        List<OrderItem> orderItems = orderItemDAO.listByUser(user.getId());
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getProduct().getId() == product.getId()) {
                orderItem.setNumber(orderItem.getNumber() + num);
                orderItemDAO.update(orderItem);
                found = true;
                break;
            }
        }

        if (!found) {
            OrderItem orderItem = new OrderItem();
            orderItem.setUser(user);
            orderItem.setNumber(num);
            orderItem.setProduct(product);
            orderItemDAO.add(orderItem);
        }
        return "%success";
    }

    public String cart(HttpServletRequest request, HttpServletResponse response, Page page) {
        User user = (User) request.getSession().getAttribute("user");
        List<OrderItem> orderItems = orderItemDAO.listByUser(user.getId());
        request.setAttribute("orderItems", orderItems);
        return "cart.jsp";
    }

    public String changeOrderItem(HttpServletRequest request, HttpServletResponse response, Page page) {
        User user = (User) request.getSession().getAttribute("user");
        if (null == user) {
            return "%fail";
        }
        int pid = Integer.parseInt(request.getParameter("pid"));
        int number = Integer.parseInt(request.getParameter("number"));
        List<OrderItem> orderItems = orderItemDAO.listByUser(user.getId());
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getProduct().getId() == pid) {
                orderItem.setNumber(number);
                orderItemDAO.update(orderItem);
                break;
            }
        }
        return "%success";
    }

    public String deleteOrderItem(HttpServletRequest request, HttpServletResponse response, Page page) {
        User user = (User) request.getSession().getAttribute("user");
        if (null == user) {
            return "%fail";
        }
        int orderItemId = Integer.parseInt(request.getParameter("orderItem"));
        orderItemDAO.delete(orderItemId);
        return "%success";
    }

    public String createOrder(HttpServletRequest request, HttpServletResponse response, Page page) {
        User user = (User) request.getSession().getAttribute("user");
        List<OrderItem> orderItems = (List<OrderItem>) request.getSession().getAttribute("orderItems");
        if (orderItems.isEmpty()) {
            return "@login.jsp";
        }
        String address = request.getParameter("address");
        String post = request.getParameter("post");
        String receiver = request.getParameter("receiver");
        String mobile = request.getParameter("mobile");
        String userMessage = request.getParameter("userMessage");

        Order order = new Order();
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);

        order.setOrderCode(orderCode);
        order.setAddress(address);
        order.setPost(post);
        order.setReceiver(receiver);
        order.setMobile(mobile);
        order.setUserMessage(userMessage);
        order.setCreateDate(new Date());
        order.setUser(user);
        order.setStatus(OrderDAO.waitPay);

        orderDAO.add(order);
        float total = 0;
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
            orderItemDAO.update(orderItem);
            total += orderItem.getProduct().getPromotePrice() * orderItem.getNumber();
        }

        return "@forealipay?oid=" + order.getId() + "&total=" + total;
    }

    public String alipay(HttpServletRequest request, HttpServletResponse response, Page page) {
        return "alipay.jsp";
    }

    public String payed(HttpServletRequest request, HttpServletResponse response, Page page) {
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order order = orderDAO.get(oid);
        order.setStatus(OrderDAO.waitDelivery);
        order.setPayDate(new Date());
        new OrderDAO().update(order);
        request.setAttribute("o", order);
        return "payed.jsp";
    }

    public String bought(HttpServletRequest request, HttpServletResponse response, Page page) {
        User user = (User) request.getSession().getAttribute("user");
        List<Order> orders = orderDAO.list(user.getId(), OrderDAO.delete);
        orderItemDAO.fill(orders);
        request.setAttribute("os", orders);
        return "bought.jsp";
    }

    public String confirmPay(HttpServletRequest request, HttpServletResponse response, Page page) {
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        orderItemDAO.fill(o);
        request.setAttribute("o", o);
        request.setAttribute("o", o);
        return "confirmPay.jsp";
    }

    public String orderConfirmed(HttpServletRequest request, HttpServletResponse response, Page page) {
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        o.setStatus(OrderDAO.waitReview);
        o.setConfirmDate(new Date());
        orderDAO.update(o);
        return "orderConfirmed.jsp";
    }

    public String deleteOrder(HttpServletRequest request, HttpServletResponse response, Page page) {
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        o.setStatus(OrderDAO.delete);
        orderDAO.update(o);
        return "%success";
    }

    public String review(HttpServletRequest request, HttpServletResponse response, Page page) {
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        orderItemDAO.fill(o);
        Product p = o.getOrderItems().get(0).getProduct();
        List<Review> reviews = reviewDAO.list(p.getId());
        productDAO.setSaleAndReviewNumber(p);
        request.setAttribute("p", p);
        request.setAttribute("o", o);
        request.setAttribute("reviews", reviews);
        return "review.jsp";
    }

    public String doreview(HttpServletRequest request, HttpServletResponse response, Page page) {
        int oid = Integer.parseInt(request.getParameter("oid"));
        Order o = orderDAO.get(oid);
        o.setStatus(OrderDAO.finish);
        orderDAO.update(o);
        int pid = Integer.parseInt(request.getParameter("pid"));
        Product p = productDAO.get(pid);
        String content = request.getParameter("content");
        content = HtmlUtils.htmlEscape(content);

        User user = (User) request.getSession().getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setProduct(p);
        review.setCreateDate(new Date());
        review.setUser(user);
        reviewDAO.add(review);

        return "@forereview?oid=" + oid + "&showonly=true";
    }

    // 进入聊天室页面
    public String chat(HttpServletRequest request, HttpServletResponse response, Page page) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            // 设置用户在线状态
            userDAO.setOnlineStatus(user.getId(), true);
            request.getSession().setAttribute("username", user.getName());
        }
        return "chat.jsp";
    }

    // 离开聊天室，设置为离线状态
    public String leaveChat(HttpServletRequest request, HttpServletResponse response, Page page) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            // 设置用户离线状态
            userDAO.setOnlineStatus(user.getId(), false);
        }
        return "@forehome"; // 重定向回首页
    }

    // 获取在线用户列表
    public String getOnlineUsers(HttpServletRequest request, HttpServletResponse response, Page page) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            List<User> onlineUsers = userDAO.getOnlineUsers();
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"onlineCount\":").append(onlineUsers.size()).append(",\"users\":[");
            for (int i = 0; i < onlineUsers.size(); i++) {
                User user = onlineUsers.get(i);
                jsonBuilder.append("\"").append(user.getName()).append("\"");
                if (i < onlineUsers.size() - 1) {
                    jsonBuilder.append(",");
                }
            }
            jsonBuilder.append("]}");
            PrintWriter writer = response.getWriter();
            String jsonResponse = jsonBuilder.toString();
            writer.write(jsonResponse);
            writer.flush(); // 刷新流
            writer.close(); // 明确关闭流
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500); // 设置 HTTP 状态码为 500
        }
        return null; // AJAX 请求不返回页面
    }


    // 获取最近的聊天记录
    public String getMessages(HttpServletRequest request, HttpServletResponse response, Page page) {
        response.setContentType("application/json;charset=UTF-8");
        MessageDAO messageDAO = new MessageDAO();
        try {
            int limit = 50; // 默认获取最近 50 条消息
            List<Message> messages = messageDAO.getRecentMessages(limit);
            // 确保消息按照时间顺序排列（最早的在前，最新的在后）
            messages.sort(Comparator.comparing(Message::getTimestamp));

            // 使用DateTimeFormatter格式化时间为ISO 8601
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"messages\":[");
            for (int i = 0; i < messages.size(); i++) {
                Message message = messages.get(i);
                ZonedDateTime zdt = ZonedDateTime.ofInstant(message.getTimestamp().toInstant(), ZoneId.systemDefault());
                String formattedTimestamp = zdt.format(formatter);

                jsonBuilder.append("{")
                        .append("\"username\":\"").append(escapeJSON(message.getUsername())).append("\",")
                        .append("\"content\":\"").append(escapeJSON(message.getContent())).append("\",")
                        .append("\"timestamp\":\"").append(formattedTimestamp).append("\"")
                        .append("}");
                if (i < messages.size() - 1) {
                    jsonBuilder.append(",");
                }
            }
            jsonBuilder.append("]}");
            PrintWriter writer = response.getWriter();
            String jsonResponse = jsonBuilder.toString();
            writer.write(jsonResponse);
            writer.flush(); // 刷新流
            writer.close(); // 明确关闭流
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500); // 设置 HTTP 状态码为 500
        }
        return null; // AJAX 请求不返回页面
    }

    // 辅助方法：转义JSON特殊字符
    private String escapeJSON(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("/", "\\/")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    // 发送消息
    public String sendMessage(HttpServletRequest request, HttpServletResponse response, Page page) {
        response.setContentType("application/json;charset=UTF-8");
        MessageDAO MessageDAO = new MessageDAO();
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user != null) {
                String content = request.getParameter("content");
                if (content != null && !content.trim().isEmpty()) {
                    Message message = new Message();
                    message.setUserId(user.getId());
                    message.setUsername(user.getName());
                    message.setContent(content.trim());
                    message.setTimestamp(new Date());

                    MessageDAO.addMessage(message);
                    String jsonResponse = "{\"success\":true}";
                    PrintWriter writer = response.getWriter();
                    writer.write(jsonResponse);
                    writer.flush(); // 刷新流
                    writer.close(); // 明确关闭流
                } else {
                    String jsonResponse = "{\"success\":false,\"message\":\"消息内容不能为空\"}";
                    PrintWriter writer = response.getWriter();
                    writer.write(jsonResponse);
                    writer.flush(); // 刷新流
                    writer.close(); // 明确关闭流
                }
            } else {
                String jsonResponse = "{\"success\":false,\"message\":\"用户未登录\"}";
                PrintWriter writer = response.getWriter();
                writer.write(jsonResponse);
                writer.flush(); // 刷新流
                writer.close(); // 明确关闭流
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
        }
        return null;
    }


}
