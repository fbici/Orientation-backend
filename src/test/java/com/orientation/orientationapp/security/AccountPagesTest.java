package com.orientation.orientationapp.security;

import com.orientation.orientationapp.config.web.AccountPagesController;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountPagesTest {
    @ParameterizedTest
    @ValueSource(strings={"/auth/login", "/auth/forgot-password", "/auth/reset-password"})
    void browserGetServesAngularWithoutConsumingRecoveryToken(String path) throws Exception {
        MockMvcBuilders.standaloneSetup(new AccountPagesController()).build()
            .perform(get(path).param("token","synthetic-not-a-real-token"))
            .andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }
}
