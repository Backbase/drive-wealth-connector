package com.backbase.modelbank;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("it")
class InitContextTest {

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
        Application.main(new String[]{"--spring.profiles.active=it"});
        assertNotNull(applicationContext);
    }

    @Test
    void testConfig(){
        Assertions.assertEquals("testClientAppKey", wealthConfigurationProperties.getDwClientAppKey());
        Assertions.assertEquals("testClientId", wealthConfigurationProperties.getClientID());
        Assertions.assertEquals("testClientSecret", wealthConfigurationProperties.getClientSecret());
    }

}
