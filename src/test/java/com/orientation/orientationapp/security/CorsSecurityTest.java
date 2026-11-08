package com.orientation.orientationapp.security;
import com.orientation.orientationapp.config.web.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.*;
import static org.junit.jupiter.api.Assertions.*;
class CorsSecurityTest {
    private WebConfig config(String origin) {
        var config=new WebConfig();
        ReflectionTestUtils.setField(config,"allowedOrigins",origin);
        ReflectionTestUtils.setField(config,"allowedMethods","GET,POST,OPTIONS");
        ReflectionTestUtils.setField(config,"allowedHeaders","*");
        ReflectionTestUtils.setField(config,"allowCredentials",true);
        ReflectionTestUtils.setField(config,"maxAge",3600L);return config;
    }
    @Test void refusesCredentialedWildcardConfiguration() {
        assertThrows(IllegalStateException.class,()->config("*").corsFilter());
    }
    @Test void rejectsUnknownOriginAndAllowsExplicitPreflight() throws Exception {
        for(String origin:new String[]{"https://evil.example.test","http://localhost:4200"}) {
            var request=new MockHttpServletRequest("OPTIONS","/auth/login");
            request.addHeader("Origin",origin);request.addHeader("Access-Control-Request-Method","POST");
            var response=new MockHttpServletResponse();
            config("http://localhost:4200").corsFilter().doFilter(request,response,(req,res)->{});
            assertEquals(origin.startsWith("https")?403:200,response.getStatus());
            if(!origin.startsWith("https")) assertEquals(origin,response.getHeader("Access-Control-Allow-Origin"));
        }
    }
}
