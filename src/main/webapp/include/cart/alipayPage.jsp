<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<title>支付页</title>

<div class="aliPayPageDiv">
    <div class="aliPayPageLogo">
        <a href="${contextPath}">
            <img id="simpleLogo" src="img/site/logo.jpg" class="simpleLogo">
        </a>
        <div style="clear:both"></div>
    </div>

    <div>
        <span class="confirmMoneyText">扫一扫付款（元）</span>
        <span class="confirmMoney">
        ￥<fmt:formatNumber type="number" value="${param.total}" minFractionDigits="2"/></span>

    </div>
    <div>
        <img class="aliPayImg" src="img/site/alipay2wei.png">
    </div>

    <div>
        <a href="forepayed?oid=${param.oid}&total=${param.total}">
            <button class="confirmPay">确认支付</button>
        </a>
    </div>

</div>