<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<%--当点击页面上的“累计评价”链接时，页面会发送AJAX请求到后端Servlet，获取评价数据，并更新页面的部分内容，
将商品详情图片替换为评价列表。这样，其他部分都不变，仅仅是商品详情图片变成了评价列表。--%>

<div class="productReviewDiv">
    <div class="productReviewTopPart">
        <a href="#" class="productReviewTopPartSelectedLink">商品详情</a>
        <a href="#" class="selected">累计评价 <span class="productReviewTopReviewLinkNumber">${p.reviewCount}</span>
        </a>
    </div>

    <div class="productReviewContentPart">
        <c:forEach items="${reviews}" var="r">
            <div class="productReviewItem">

                <div class="productReviewItemDesc">
                    <div class="productReviewItemContent">
                            ${r.content }
                    </div>
                    <div class="productReviewItemDate"><fmt:formatDate value="${r.createDate}"
                                                                       pattern="yyyy-MM-dd"/></div>
                </div>
                <div class="productReviewItemUserInfo">

                        ${r.user.anonymousName}<span class="userInfoGrayPart">（匿名）</span>
                </div>

                <div style="clear:both"></div>

            </div>
        </c:forEach>
    </div>

</div>