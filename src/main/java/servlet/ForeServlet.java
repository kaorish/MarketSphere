package servlet;

import org.apache.commons.lang.math.RandomUtils;
import bean.*;
import dao.CategoryDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import dao.ProductImageDAO;
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
import java.util.*;

public class ForeServlet extends BaseForeServlet {

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
        System.out.println("Encrypted Password: " + encryptedPassword);

        userDAO.add(user);

        return "@registerSuccess.jsp";
    }

    // 发送短信验证码

    public String sendCode(HttpServletRequest request, HttpServletResponse response, Page page) {
        response.setContentType("application/json;charset=UTF-8");
        String mobile = request.getParameter("mobile");
        System.out.println("Mobile: " + mobile);
        try {
            // 生成验证码
            String code = generateVerificationCode();
            boolean success = sendSms(mobile, code); // 假设成功发送短信

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

    private boolean sendSms(String mobile, String code) {
        // 调用短信发送服务
        // 接收验证码的11位手机号
        String phone = mobile;

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
        request.getSession().setAttribute("user", user);
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
        request.getSession().removeAttribute("user");
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
        //遍历此用户对应的所有订单项，如果其产品id等于p.getId()，就对对应订单项的数量追加
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
}
