<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<%--此页面是鼠标移动到商品分类上时，显示的商品分类的具体内容，会挡住轮播图的那个盒子--%>

<c:forEach items="${categories}" var="category">
    <div cid="${category.id}" class="productsAsideCategorys">

        <c:forEach items="${category.productsByRow}" var="ps">
            <div class="row show1">
                <c:forEach items="${ps}" var="p">
                    <c:if test="${!empty p.subTitle}">
                        <a href="foreproduct?pid=${p.id}">
                            <c:forEach items="${fn:split(p.subTitle, ' ')}" var="title" varStatus="st">
                                <c:if test="${st.index==0}">
                                    ${title}
                                </c:if>
                            </c:forEach>
                        </a>
                    </c:if>
                </c:forEach>
                <div class="seperator"></div>
            </div>
        </c:forEach>
    </div>
</c:forEach>

<script>
    $(function () {
        $("div.productsAsideCategorys div.row a").each(function () {
            var v = Math.round(Math.random() * 6);
            if (v == 1)
                $(this).css("color", "#87CEFA");
        });
    });

</script>