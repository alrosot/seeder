package br.com.trofo.seeder.config;

import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContainerConfig {

    @Bean
    EmbeddedServletContainerCustomizer containerCustomizer() {
        return (containerFactory) -> {
            if (containerFactory instanceof TomcatEmbeddedServletContainerFactory) {
                TomcatEmbeddedServletContainerFactory tomcatContainerFactory = (TomcatEmbeddedServletContainerFactory) containerFactory;
                tomcatContainerFactory.addConnectorCustomizers((connector) -> {
                    connector.setUseBodyEncodingForURI(true);
                });
            }
        };
    }


}
