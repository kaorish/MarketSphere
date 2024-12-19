package servlet;

import dao.*;
import util.Page;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 这个类是ForeServlet的基类，定义了service方法用于处理所有前端方面的请求（即以“fore”开头的请求）。
 * ForeServlet不定义service方法，而是直接继承本类的service方法，而ForeServletFilter中统一将前端请求转发到ForeServlet，
 * 也就意味着，这些请求到达ForeServlet后，都会调用BaseForeServlet中的service方法，然后根据不同条件再执行相应的逻辑。
 * 这个类就是一个分类中转站。
 *
 * 所有的请求分为两类：前端请求和后端请求。
 * 前端请求是url以“fore”开头的请求，由本Servlet处理；后端请求是url以“admin”开头的请求，这些请求由BaseBackServlet处理。
 * 而前端请求又分为三类，以“/”开头的、以“@”开头的、以“%”开头的。
 *
 * 以“/”开头的请求，是为了调用ForeServlet中的方法，如“/forehome”就调用ForeServlet中的home方法。保持地址栏不变.
 *
 * 以“@”开头的请求，是为了重定向到某个地址，如“@foreregisterSucess”就直接重定向到rgisterSucess.jsp页面，
 * 而且这些页面都是直属与webapp的页面。以防止浏览器刷新页面时重复提交表单数据。而且因为是重定向，地址栏的页面地址会刷新。
 *
 * 以“%”开头的请求，是为了直接向响应写入字符串内容，用于ajax响应，如“%forecheckLogin”就直接向响应写入字符串“success”。
 */

public class BaseForeServlet extends HttpServlet {
    protected CategoryDAO categoryDAO = new CategoryDAO();
    protected OrderDAO orderDAO = new OrderDAO();
    protected OrderItemDAO orderItemDAO = new OrderItemDAO();
    protected ProductDAO productDAO = new ProductDAO();
    protected ProductImageDAO productImageDAO = new ProductImageDAO();
    protected PropertyDAO propertyDAO = new PropertyDAO();
    protected PropertyValueDAO propertyValueDAO = new PropertyValueDAO();
    protected ReviewDAO reviewDAO = new ReviewDAO();
    protected UserDAO userDAO = new UserDAO();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int start = 0;
            int count = 10;
            if (null != req.getParameter("page.start") && null != req.getParameter("page.count")) {
                start = Integer.parseInt(req.getParameter("page.start"));
                count = Integer.parseInt(req.getParameter("page.count"));
            }

            Page page = new Page(start, count);
            String method = (String) req.getAttribute("method");
            Method m = this.getClass().getMethod(method, HttpServletRequest.class, HttpServletResponse.class, Page.class);
            String redirect = m.invoke(this, req, resp, page).toString();
            if (redirect.startsWith("@")) {
                resp.sendRedirect(redirect.substring(1));
            } else if (redirect.startsWith("%")) {
                resp.getWriter().print(redirect.substring(1));
            } else {
                req.getRequestDispatcher(redirect).forward(req, resp);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
