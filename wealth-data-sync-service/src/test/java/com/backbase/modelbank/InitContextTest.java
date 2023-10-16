package com.backbase.modelbank;

import static com.backbase.modelbank.WealthDataSyncApplication.main;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = WealthDataSyncApplication.class)
@Testcontainers
@DirtiesContext
@ActiveProfiles("it")
@Slf4j
class InitContextTest {

    private static final String QUEUE_NAME = "order-event-test-queue";

    static {
        System.setProperty("SIG_SECRET_KEY", "JWTSecretKeyDontUseInProduction!");
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    DriveWealthConfigurationProperties wealthConfigurationProperties;

    @Test
    void shouldLoadContext() {
        assertNotNull(applicationContext);
    }


    @Test
    void shouldLoadContextWithArgs() {
        main(new String[]{"--spring.profiles.active=it"});
        assertNotNull(applicationContext);
    }

    @Test
    void testConfig() {
        Assertions.assertEquals("testClientAppKey", wealthConfigurationProperties.getDwClientAppKey());
        Assertions.assertEquals("testClientId", wealthConfigurationProperties.getClientID());
        Assertions.assertEquals("testClientSecret", wealthConfigurationProperties.getClientSecret());
        Assertions.assertEquals(365,
            wealthConfigurationProperties.getTransactions().getDaysRefreshWindow().get(0).period());
        Assertions.assertEquals(1,
            wealthConfigurationProperties.getTransactions().getDaysRefreshWindow().get(1).period());
        Assertions.assertEquals(12, wealthConfigurationProperties.getOrders().getMonthsRefreshWindow().get(0).period());
        Assertions.assertEquals(1, wealthConfigurationProperties.getOrders().getMonthsRefreshWindow().get(1).period());
    }

}
