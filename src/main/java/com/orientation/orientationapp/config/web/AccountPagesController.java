package com.orientation.orientationapp.config.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** GET serves Angular pages; the existing POST authentication API is unchanged. */
@Controller
public class AccountPagesController {
    @GetMapping({"/auth/login", "/auth/forgot-password", "/auth/reset-password"})
    public String accountPage() {
        return "forward:/index.html";
    }
}
