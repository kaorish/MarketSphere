package filter;


import org.apache.commons.lang.StringUtils;
import bean.Category;
import bean.OrderItem;
import bean.User;
import dao.CategoryDAO;
import dao.OrderItemDAO;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 这个过滤器用于过滤所有以“/fore”开头的请求，同时会给该请求进行初始化，
 * 添加一些必要的参数，如user、categories、cartTotalItemNumber、method。
 * 通过反射机制，将url的“/fore”后面的内容设置为 method，将请求转发到 ForeServlet 中处理。
 * 因为所有以“/fore”开头的请求都是为了访问ForeServlet当的某个方法，所以要对其进行初始化，比如设置好method。
 * 之后，ForeServlet 会根据 method 的不同，调用不同的方法，处理不同的请求。
 * 如此，所有需要调用ForeServlet中的方法的前端请求就都被统一处理了。
 */

public class ForeServletFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String contextPath = request.getServletContext().getContextPath();
        request.getServletContext().setAttribute("contextPath", contextPath); // 保存 contextPath，即保存当前路径或者说url、目的地址，用于后续进行转发

        User user = (User) request.getSession().getAttribute("user");
        int cartTotalItemNumber = 0;
        if (null != user) {
            List<OrderItem> orderItems = new OrderItemDAO().listByUser(user.getId());
            for (OrderItem orderItem : orderItems) {
                cartTotalItemNumber += orderItem.getNumber();
            }
        }
        request.setAttribute("cartTotalItemNumber", cartTotalItemNumber);

        List<Category> categories = (List<Category>) request.getAttribute("categories");
        if (null == categories) {
            categories = new CategoryDAO().list();
            request.setAttribute("categories", categories);
        }

        String uri = request.getRequestURI();
        uri = StringUtils.remove(uri, contextPath);
        if (uri.startsWith("/fore") && !uri.startsWith("/foreServlet")) {
            String method = StringUtils.substringAfterLast(uri, "/fore");
            request.setAttribute("method", method);
            servletRequest.getRequestDispatcher("/foreServlet").forward(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
