<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<title>MarketSphere-${category.name}</title>
<div id="category">
    <div class="categoryPageDiv">
        <img src="img/category/${category.id}.jpg">
        <%@include file="sortBar.jsp" %>
        <%@include file="productsByCategory.jsp" %>
    </div>

</div>