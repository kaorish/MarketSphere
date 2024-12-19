package filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;

public class IpFilter implements Filter {

    private List<String> allowedIpRanges; // 存储多个允许的 IP 范围

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        allowedIpRanges = new ArrayList<>();
        allowedIpRanges.add("127.0.0.1/32");  // IPv4 本地回环
        allowedIpRanges.add("::1/128");       // IPv6 本地回环
        allowedIpRanges.add("39.144.169.0/24");  // 其他允许范围
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取客户端 IP
        String clientIp = getClientIp(httpRequest);
        System.out.println("Client IP: " + clientIp);

        // 检查是否允许访问
        boolean isAllowed = isIpInAnyRange(clientIp);
        System.out.println("Is allowed: " + isAllowed);

        if (!isAllowed) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: Access is denied");
            return;
        }

        chain.doFilter(request, response);
    }



    @Override
    public void destroy() {
        // 可以进行资源清理
    }

    /**
     * 获取客户端的真实 IP 地址（处理代理的情况）
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr(); // 获取请求方的网络 IP
        } else {
            String[] ipArray = clientIp.split(",");
            clientIp = ipArray[0].trim(); // 获取真实客户端 IP
        }
        System.out.println("Detected Client IP: " + clientIp); // 调试输出
        return clientIp;
    }



    /**
     * 判断 IP 是否在给定的 CIDR 范围内
     */
    private boolean isIpInRange(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] ipAddress = InetAddress.getByName(ip).getAddress();
            byte[] networkAddress = network.getAddress();

            // 检查前缀长度
            int bytesToCheck = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            // 检查前缀部分
            for (int i = 0; i < bytesToCheck; i++) {
                if (ipAddress[i] != networkAddress[i]) {
                    return false;
                }
            }

            // 检查剩余的位
            if (remainingBits > 0) {
                byte mask = (byte) (0xFF << (8 - remainingBits));
                if ((ipAddress[bytesToCheck] & mask) != (networkAddress[bytesToCheck] & mask)) {
                    return false;
                }
            }

            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 检查 IP 是否在多个允许的范围内
     */
    private boolean isIpInAnyRange(String ip) {
        for (String range : allowedIpRanges) {
            System.out.println("Checking IP " + ip + " against range: " + range);  // 调试输出
            if (isIpInRange(ip, range)) {
                return true;
            }
        }
        return false;
    }
}
