package br.com.trofo.seeder.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContainerConfig {

    @Bean
    WebServerFactoryCustomizer containerCustomizer() {
        return (containerFactory) -> {
            if (containerFactory instanceof TomcatServletWebServerFactory) {
                TomcatServletWebServerFactory tomcatContainerFactory = (TomcatServletWebServerFactory) containerFactory;
                tomcatContainerFactory.addConnectorCustomizers((connector) -> connector.setUseBodyEncodingForURI(true));
            }
        };
    }


}
