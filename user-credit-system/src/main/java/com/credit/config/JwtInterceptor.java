package com.credit.config;
import com.credit.common.result.Result;
import com.credit.module.user.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Resource
    private JwtUtil jwtUtil;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(om.writeValueAsString(Result.fail("未登录")));
            return false;
        }
        try {
            jwtUtil.getUserIdFromToken(token);
            return true;
        } catch (Exception e) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(om.writeValueAsString(Result.fail("token无效")));
            return false;
        }
    }
}

