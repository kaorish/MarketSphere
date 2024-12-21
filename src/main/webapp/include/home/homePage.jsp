<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<%--拼积木，首页（这里指除去了top、header、footer的部分）就是由 分类菜单和轮播图共同组成的categoryAndcarousel.jsp 和 页面主体那可以一直往下滚动的并且
  每一行都是一个分类，一行展示该分类的5个商品的homepageCategoryProducts.jsp 两个jsp文件组成的。--%>

<title>MarketSphere</title>

<div class="homepageDiv">
    <%@include file="categoryAndcarousel.jsp" %>
    <%@include file="homepageCategoryProducts.jsp" %>
</div>