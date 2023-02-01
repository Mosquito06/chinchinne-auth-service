package com.chinchinne.authservice.config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CustomizationPort implements WebServerFactoryCustomizer<ConfigurableWebServerFactory>
{
    @Override
    public void customize(ConfigurableWebServerFactory server)
    {
        Random random = new Random();

        int port = random.ints(30020, 30029).findFirst().getAsInt();
        server.setPort(port);
    }
}
