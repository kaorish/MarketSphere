<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<div>
    <a href="/forehome">
        <img id="simpleLogo" class="simpleLogo" src="img/site/logo.jpg">
    </a>
    <form action="foresearch" method="post">
        <div class="searchDiv pull-right">
            <input type="text" placeholder="平衡车 原汁机" name="keyword" style="padding-left: 10px">
            <button class="searchButton" type="submit">搜索</button>
            <div class="searchBelow">
                <c:forEach items="${categories}" var="category" varStatus="st">
                    <c:if test="${st.count >= 8 and st.count <= 11}">
                        <span>
                            <a href="forecategory?cid=${category.id}">
                                    ${category.name}
                            </a>
                            <c:if test="${st.count != 11}">
                                <span>|</span>
                            </c:if>
                        </span>
                    </c:if>
                </c:forEach>
            </div>
        </div>
    </form>
    <div style="clear:both"></div>
</div>
