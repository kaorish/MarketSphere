package servlet;


import bean.Category;
import util.ImageUtil;
import util.Page;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryServlet extends BaseBackServlet {

    public String add(HttpServletRequest request, HttpServletResponse response, Page page) {
        Map<String, String> params = new HashMap<>();
        InputStream is = super.parseUpload(request, params);

        String name = params.get("name");
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        // 创建分类对象并保存到数据库
        Category c = new Category();
        c.setName(name);
        categoryDAO.add(c); // 数据库中生成新分类并获取 ID

        // 定义运行时路径和源代码路径
        String runtimePath = request.getSession().getServletContext().getRealPath("img/category");
        String sourcePath = "D:/Code/Web/MarketSphere/src/main/webapp/img/category"; // 修改为源代码路径

        File runtimeFolder = new File(runtimePath);
        if (!runtimeFolder.exists()) {
            runtimeFolder.mkdirs(); // 确保运行时目录存在
        }

        File sourceFolder = new File(sourcePath);
        if (!sourceFolder.exists()) {
            sourceFolder.mkdirs(); // 确保源代码目录存在
        }

        // 创建图片文件
        File runtimeFile = new File(runtimeFolder, c.getId() + ".jpg");
        File sourceFile = new File(sourceFolder, c.getId() + ".jpg");

        try {
            if (is != null && is.available() > 0) {
                // 保存图片到运行时目录
                try (FileOutputStream fos = new FileOutputStream(runtimeFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();
                }

                // 转换图片为 JPG 格式（可选）
                BufferedImage img = ImageUtil.change2jpg(runtimeFile);
                ImageIO.write(img, "jpg", runtimeFile);

                // 拷贝图片到源代码目录
                Files.copy(runtimeFile.toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image saved successfully to runtime and source folders.");
            } else {
                System.out.println("No image file uploaded.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to upload and save the image.");
        }

        return "@admin_category_list";
    }


    public String delete(HttpServletRequest request, HttpServletResponse response, Page page) {
        int id = Integer.parseInt(request.getParameter("id"));
        categoryDAO.delete(id);
        return "@admin_category_list";
    }


    public String edit(HttpServletRequest request, HttpServletResponse response, Page page) {
        int id = Integer.parseInt(request.getParameter("id"));
        Category c = categoryDAO.get(id);
        request.setAttribute("c", c);
        return "admin/editCategory.jsp";
    }


    public String update(HttpServletRequest request, HttpServletResponse response, Page page) {
        Map<String, String> params = new HashMap<>();
        InputStream is = super.parseUpload(request, params);

        System.out.println(params);
        String name = params.get("name");
        int id = Integer.parseInt(params.get("id"));

        Category c = new Category();
        c.setId(id);
        c.setName(name);
        categoryDAO.update(c);

        File imageFolder = new File(request.getSession().getServletContext().getRealPath("img/category"));
        File file = new File(imageFolder, c.getId() + ".jpg");
        file.getParentFile().mkdirs();

        try {
            if (null != is && 0 != is.available()) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte b[] = new byte[1024 * 1024];
                    int length = 0;
                    while (-1 != (length = is.read(b))) {
                        fos.write(b, 0, length);
                    }
                    fos.flush();
                    //通过如下代码，把文件保存为jpg格式
                    BufferedImage img = ImageUtil.change2jpg(file);
                    ImageIO.write(img, "jpg", file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "@admin_category_list";

    }


    // 转发至分类管理页面，由于后台没有特定制作首页，所以直接用分类管理页面作为首页，也就是调用此方法
    public String list(HttpServletRequest request, HttpServletResponse response, Page page) {
        List<Category> categories = categoryDAO.list(page.getStart(), page.getCount());
        int total = categoryDAO.getTotal();
        page.setTotal(total);

        request.setAttribute("thecs", categories);
        request.setAttribute("page", page);

        return "admin/listCategory.jsp"; // 因为只有后台才会查看所有种类，所以调用这个方法的一定是后台，所以直接返回“admin/listCategory.jsp”即可，而且是服务器内部直接跳转
    }
}
