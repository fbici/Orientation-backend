package com.orientation.orientationapp.security;
import com.orientation.orientationapp.security.filter.AuthRateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import static org.junit.jupiter.api.Assertions.*;
class AuthRateLimitTest {
    @Test void forwardedHeaderCannotBypassPeerLimit() throws Exception {
        var filter=new AuthRateLimitFilter();
        for(int i=0;i<61;i++) {
            var request=new MockHttpServletRequest("POST","/auth/login");
            request.setServletPath("/auth/login");request.setRemoteAddr("127.0.0.42");
            request.addHeader("X-Forwarded-For","192.0.2."+i);
            var response=new MockHttpServletResponse();
            filter.doFilter(request,response,(req,res)->{});
            assertEquals(i<60?200:429,response.getStatus());
            if(i==60) assertNotNull(response.getHeader("Retry-After"));
        }
    }
}
