<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>

<form method="post" action="foreregister" class="registerForm">

    <div class="registerDiv">
        <div class="registerErrorMessageDiv">
            <div class="alert alert-danger" role="alert">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close"></button>
                <span class="errorMessage"></span>
            </div>
        </div>

        <table class="registerTable" align="center">
            <tr>
                <td class="registerTip registerTableLeftTD">设置会员名</td>
                <td></td>
            </tr>
            <tr>
                <td class="registerTableLeftTD">登陆名</td>
                <td class="registerTableRightTD"><input id="name" name="name" placeholder="会员名一旦设置成功，无法修改"></td>
            </tr>
            <tr>
                <td class="registerTip registerTableLeftTD">设置登陆密码</td>
                <td class="registerTableRightTD">登陆时验证，保护账号信息</td>
            </tr>
            <tr>
                <td class="registerTableLeftTD">登陆密码</td>
                <td class="registerTableRightTD"><input id="password" name="password" type="password"
                                                        placeholder="设置你的登陆密码"></td>
            </tr>
            <tr>
                <td class="registerTableLeftTD">密码确认</td>
                <td class="registerTableRightTD"><input id="repeatpassword" type="password" placeholder="请再次输入你的密码">
                </td>
            </tr>

            <tr>
                <td class="registerTableLeftTD">手机号</td>
                <td class="registerTableRightTD"><input type="text" id="phone" name="phone" placeholder="请输入手机号"></td>
            </tr>
            <tr>
                <td class="registerTableLeftTD"></td>
                <td class="registerTableRightTD">
                    <button type="button" id="sendCodeButton" onclick="sendCode()">发送验证码</button>
                </td>
            </tr>
            <tr>
                <td class="registerTableLeftTD">验证码</td>
                <td class="registerTableRightTD"><input type="text" id="verificationCode" name="verificationCode" placeholder="请输入验证码"></td>
            </tr>

            <tr>
                <td colspan="2" class="registerButtonTD">
                    <a href="../registerSuccess.jsp">
                        <button>提 交</button>
                    </a>
                </td>
            </tr>
        </table>
    </div>
</form>

<script>
    function sendCode() {
        const phone = $("#phone").val();

        if (!phone || phone.length !== 11 || !/^\d+$/.test(phone)) {
            $("span.errorMessage").html("请输入有效的手机号");
            $("div.registerErrorMessageDiv").css("visibility", "visible");
            return;
        }

        $.ajax({
            type: "POST",
            url: "/foresendCode", // 确保路径与后端一致
            data: { mobile: phone },
            dataType: "json",
            success: function (data) {
                if (data.success) {
                    alert("验证码发送成功");
                    var button = $("#sendCodeButton");
                    button.prop("disabled", true);
                    var count = 60;
                    var interval = setInterval(function () {
                        button.text(count + "秒后重新发送");
                        count--;
                        if (count < 0) {
                            clearInterval(interval);
                            button.prop("disabled", false);
                            button.text("发送验证码");
                        }
                    }, 1000);
                } else {
                    alert("验证码发送失败，请重试");
                }
            },
            error: function () {
                alert("请求失败，请检查网络或联系管理员");
            }
        });
    }

    function verifyCode(callback) {
        const verificationCode = $("#verificationCode").val();

        if (!verificationCode || verificationCode.length === 0) {
            $("span.errorMessage").html("请输入验证码");
            $("div.registerErrorMessageDiv").css("visibility", "visible");
            return;
        }

        $.ajax({
            type: "POST",
            url: "/foreverifyCode", // Ensure the path matches the backend
            data: { verificationCode: verificationCode },
            dataType: "json",
            success: function (data) {
                if (data.success) {
                    alert("验证码正确");
                    callback(true); // Verification successful
                } else {
                    $("span.errorMessage").html("验证码错误");
                    $("div.registerErrorMessageDiv").css("visibility", "visible");
                    callback(false); // Verification failed
                }
            },
            error: function () {
                alert("请求失败，请检查网络或联系管理员");
                callback(false); // Verification failed
            }
        });
    }

    $(function () {
        <c:if test="${!empty msg}">
        $("span.errorMessage").html("${msg}");
        $("div.registerErrorMessageDiv").css("visibility", "visible");
        </c:if>

        $(".registerForm").submit(function () {
            if (0 == $("#name").val().length) {
                $("span.errorMessage").html("请输入用户名");
                $("div.registerErrorMessageDiv").css("visibility", "visible");
                return false;
            }
            if (0 == $("#password").val().length) {
                $("span.errorMessage").html("请输入密码");
                $("div.registerErrorMessageDiv").css("visibility", "visible");
                return false;
            }
            if (0 == $("#repeatpassword").val().length) {
                $("span.errorMessage").html("请输入重复密码");
                $("div.registerErrorMessageDiv").css("visibility", "visible");
                return false;
            }
            if ($("#password").val() != $("#repeatpassword").val()) {
                $("span.errorMessage").html("重复密码不一致");
                $("div.registerErrorMessageDiv").css("visibility", "visible");
                return false;
            }

            if (0 == $("#phone").val().length) {
                $("span.errorMessage").html("请输入手机号");
                $("div.registerErrorMessageDiv").css("visibility", "visible");
                return false;
            }

            if (0 == $("#verificationCode").val().length) {
                $("span.errorMessage").html("请输入验证码");
                $("div.registerErrorMessageDiv").css("visibility", "visible");
                return false;
            }

            // 验证验证码, 验证失败则返回，不提交表单，成功就继续后面的逻辑，只验证是否失败，失败了就阻值后续逻辑
            // Verify the code before submitting the form
            verifyCode(function (isVerified) {
                if (isVerified) {
                    $(".registerForm")[0].submit(); // Submit the form if verification is successful
                }
            });

            return false; // Prevent the form from submitting immediately
        });
    });
</script>





