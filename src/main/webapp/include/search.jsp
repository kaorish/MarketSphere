<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<a href="/forehome">
    <img id="simpleLogo" src="img/site/logo.jpg" class="simpleLogo">
</a>

<form action="foresearch" method="post">
    <div class="searchDiv">
        <input name="keyword" type="text" placeholder="时尚男鞋  太阳镜 " style="padding-left: 10px">
        <button type="submit" class="searchButton">搜索</button>
        <div class="searchBelow">
            <%--获取分类集合，取第5到第8一共四个类显示--%>
            <c:forEach items="${categories}" var="category" varStatus="st">
                <c:if test="${st.count>=5 and st.count<=8}">
                        <span>
                            <a href="forecategory?cid=${category.id}">
                                    ${category.name}
                            </a>
                            <c:if test="${st.count!=8}">
                                <span>|</span>
                            </c:if>
                        </span>
                </c:if>
            </c:forEach>
        </div>
    </div>
</form>