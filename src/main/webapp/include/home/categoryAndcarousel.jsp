<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<%--此页面是一个大盒子，里面包住了分类菜单，轮播图，以及分类下的具体商品--%>

<div class="categoryWithCarousel">

    <div class="headbar show1">
        <div class="head ">

            <span style="margin-left:10px" class="glyphicon glyphicon-th-list"></span>
            <span style="margin-left:10px">商品分类</span>

        </div>

        <div class="rightMenu">

            <c:forEach items="${categories}" var="category" varStatus="st">
                <c:if test="${st.count<=7}">
                <span>
                <a href="forecategory?cid=${category.id}">
                        ${category.name}
                </a></span>
                </c:if>
            </c:forEach>
        </div>

    </div>

    <div style="position: relative">
        <%@include file="categoryMenu.jsp" %>
    </div>

<%--    鼠标移到分类项时会动态显示、会挡住轮播图的那个盒子，显示里面具体有哪些商品--%>
    <div style="position: relative;left: 0;top: 0;">
        <%@include file="productsAsideCategories.jsp" %>
    </div>

<%--    里面全是js代码--%>
    <%@include file="carousel.jsp" %>

    <div class="carouselBackgroundDiv">
    </div>

</div>

<script>
    function showProductsAsideCategorys(cid) {
        $("div.eachCategory[cid=" + cid + "]").css("background-color", "white");
        $("div.eachCategory[cid=" + cid + "] a").css("color", "#87CEFA");
        $("div.productsAsideCategorys[cid=" + cid + "]").show();
    }

    function hideProductsAsideCategorys(cid) {
        $("div.eachCategory[cid=" + cid + "]").css("background-color", "#e2e2e3");
        $("div.eachCategory[cid=" + cid + "] a").css("color", "#000");
        $("div.productsAsideCategorys[cid=" + cid + "]").hide();
    }

    $(function () {
        $("div.eachCategory").mouseenter(function () {
            var cid = $(this).attr("cid");
            showProductsAsideCategorys(cid);
        });
        $("div.eachCategory").mouseleave(function () {
            var cid = $(this).attr("cid");
            hideProductsAsideCategorys(cid);
        });
        $("div.productsAsideCategorys").mouseenter(function () {
            var cid = $(this).attr("cid");
            showProductsAsideCategorys(cid);
        });
        $("div.productsAsideCategorys").mouseleave(function () {
            var cid = $(this).attr("cid");
            hideProductsAsideCategorys(cid);
        });

        $("div.rightMenu span").mouseenter(function () {
            var left = $(this).position().left;
            var top = $(this).position().top;
            var width = $(this).css("width");
            var destLeft = parseInt(left) + parseInt(width) / 2;
            $("img#catear").css("left", destLeft);
            $("img#catear").css("top", top - 20);
            $("img#catear").fadeIn(500);

        });
        $("div.rightMenu span").mouseleave(function () {
            $("img#catear").hide();
        });

        var left = $("div#carousel-of-product").offset().left;
        $("div.categoryMenu").css("left", left - 20);
        $("div.categoryWithCarousel div.head").css("margin-left", left);
        $("div.productsAsideCategorys").css("left", left - 20);

    });
</script>