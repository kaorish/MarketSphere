<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<div class="navitagorDiv">
<%--    这些链接有些类似于请求，均会被后台过滤器拦截，然后转发到对应的Servlet，里面有相应的方法跳转到对应的页面，此页面相当于是个门户--%>
    <nav class="navbar navbar-default navbar-fixed-top navbar-inverse">
<%--        <img style="margin-left:10px;margin-right:0px" class="pull-left" src="img/site/tmallbuy.png" height="45px">--%>
        <a class="navbar-brand" href="#">MarketSphere后台</a>
        <a class="navbar-brand" href="admin_category_list">分类管理</a>
        <a class="navbar-brand" href="admin_user_list">用户管理</a>
        <a class="navbar-brand" href="admin_order_list">订单管理</a>
    </nav>
</div>