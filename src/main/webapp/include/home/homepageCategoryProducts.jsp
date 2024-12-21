<!DOCTYPE html>
<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<%--此页面是首页的主体，也就是在轮播图下方的很长很长的页面部分。用循环把每个分类的前5个产品显示出来，每个分类一行，一直循环下去，所以页面可以一直往下滚动。--%>

<c:if test="${empty param.categorycount}">
    <c:set var="categorycount" scope="page" value="100"/>
</c:if>

<c:if test="${!empty param.categorycount}">
    <c:set var="categorycount" scope="page" value="${param.categorycount}"/>
</c:if>

<div class="homepageCategoryProducts">
    <c:forEach items="${categories}" var="category" varStatus="stc">
        <c:if test="${stc.count<=categorycount}">
            <div class="eachHomepageCategoryProducts">
                <div class="left-mark"></div>
                <span class="categoryTitle">${category.name}</span>
                <br>
                <c:forEach items="${category.products}" var="p" varStatus="st">
                    <c:if test="${st.count<=5}">
                        <div class="productItem">
                            <a href="foreproduct?pid=${p.id}"><img width="100px"
                                                                   src="img/productSingle_middle/${p.firstProductImage.id}.jpg"></a>
                            <a class="productItemDescLink" href="foreproduct?pid=${p.id}">
                                <span class="productItemDesc">[热销]
                                ${fn:substring(p.name, 0, 20)}
                                </span>
                            </a>
                            <span class="productPrice">
                                <fmt:formatNumber type="number" value="${p.promotePrice}" minFractionDigits="2"/>
                            </span>
                        </div>
                    </c:if>
                </c:forEach>
                <div style="clear:both"></div>
            </div>
        </c:if>
    </c:forEach>

<%--    <img id="endpng" class="endpng" src="img/site/end.png">--%>

</div>