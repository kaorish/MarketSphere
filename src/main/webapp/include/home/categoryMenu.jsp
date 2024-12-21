<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<%--商品分类这一列中显示出的分类，只显示前17个分类，每个分类具体的商品会在鼠标移到该分类上时显示出来--%>

<div class="categoryMenu">
    <c:forEach items="${categories}" var="category" varStatus="st">
        <c:if test="${st.count<=17}">
            <div cid="${category.id}" class="eachCategory">
                <span class="glyphicon glyphicon-link"></span>
                <a href="forecategory?cid=${category.id}">
                        ${category.name}
                </a>
            </div>
        </c:if>
    </c:forEach>
</div>