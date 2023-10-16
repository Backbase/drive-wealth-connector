package com.backbase.modelbank;

import com.backbase.modelbank.config.DWObtainAccessTokenConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("it")
@SpringBootTest(classes = PortfolioOutboundIntegrationApplication.class)
class ContextTest {

    @Autowired
    private DWObtainAccessTokenConfig dwObtainAccessTokenConfig;

    @Test
    void contextLoads() {

        Assertions.assertNotNull(dwObtainAccessTokenConfig);
    }
}
