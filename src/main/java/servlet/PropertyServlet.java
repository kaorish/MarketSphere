package servlet;

import bean.Category;
import bean.Property;
import util.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class PropertyServlet extends BaseBackServlet {

    @Override
    public String add(HttpServletRequest request, HttpServletResponse response, Page page) {
        int cid = Integer.parseInt(request.getParameter("cid"));
        Category c = categoryDAO.get(cid);

        String name = request.getParameter("name");
        Property p = new Property();
        p.setCategory(c);
        p.setName(name);
        propertyDAO.add(p);
        return "@admin_property_list?cid=" + cid;
    }

    @Override
    public String delete(HttpServletRequest request, HttpServletResponse response, Page page) {
        int id = Integer.parseInt(request.getParameter("id"));
        Property p = propertyDAO.get(id);
        propertyDAO.delete(id);
        return "@admin_property_list?cid=" + p.getCategory().getId();
    }

    @Override
    public String edit(HttpServletRequest request, HttpServletResponse response, Page page) {
        int id = Integer.parseInt(request.getParameter("id"));
        Property p = propertyDAO.get(id);
        request.setAttribute("p", p);
        return "admin/editProperty.jsp";
    }

    @Override
    public String update(HttpServletRequest request, HttpServletResponse response, Page page) {
        int cid = Integer.parseInt(request.getParameter("cid"));
        Category c = categoryDAO.get(cid);

        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        Property p = new Property();
        p.setCategory(c);
        p.setId(id);
        p.setName(name);
        propertyDAO.update(p);
        return "@admin_property_list?cid=" + p.getCategory().getId();
    }

    @Override
    public String list(HttpServletRequest request, HttpServletResponse response, Page page) {
        String cidStr = request.getParameter("cid");
        System.out.println("cidStr: " + cidStr);
        if (cidStr == null || cidStr.isEmpty()) {
            throw new IllegalArgumentException("cid parameter is missing");
        }

        int cid = Integer.parseInt(cidStr);
        Category category = categoryDAO.get(cid);
        System.out.println("category:" + category.getId());
        System.out.println("category:" + category.getName());
        if (category == null) {
            throw new RuntimeException("Category with id " + cid + " not found!");
        }

        List<Property> ps = propertyDAO.list(cid, page.getStart(), page.getCount()); // ps是properties的缩写，由于前端页面使用的是ps，所以这里也使用ps
        int total = propertyDAO.getTotal(cid);
        page.setTotal(total);
        System.out.println("set total: " + page.getTotal());

        page.setParam("&cid=" + category.getId());
        System.out.println("set param: " + page.getParam());

        request.setAttribute("ps", ps);
        System.out.println("set ps: " + ps.size());

        request.setAttribute("category", category);
        System.out.println("set category name: " + category.getName());
        System.out.println("set category id: " + category.getId());

        request.setAttribute("page", page);
        System.out.println("set page: " + page.getCount());

        return "admin/listProperty.jsp";
    }

}
