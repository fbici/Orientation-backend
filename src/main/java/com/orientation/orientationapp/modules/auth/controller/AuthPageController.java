package com.orientation.orientationapp.modules.auth.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** Explicit public HTML pages; REST verification and protected API routes stay unchanged. */
@RestController
public class AuthPageController {
    @GetMapping(value={"/auth/login","/auth/register","/auth/verify-email","/auth/forgot-password","/auth/reset-password"},produces=MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> page() {
        Resource index = new ClassPathResource("static/index.html");
        if(!index.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .header("Referrer-Policy","no-referrer").contentType(MediaType.TEXT_HTML).body(index);
    }
}
