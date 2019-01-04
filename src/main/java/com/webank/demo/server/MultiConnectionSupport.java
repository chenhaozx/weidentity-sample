package com.webank.demo.server;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支持http和htts
 * 
 * @author v_wbgyang
 *
 */
@Configuration
public class MultiConnectionSupport {
     
    /**
     * 获取http端口
     */
    @Value("${http.port:20191}")
    private int port;

    /**
     * 同时支持 http和https
     * 
     */
    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                if (container instanceof TomcatEmbeddedServletContainerFactory) {
                    TomcatEmbeddedServletContainerFactory containerFactory =
                        (TomcatEmbeddedServletContainerFactory) container;
                    Connector connector =
                        new Connector(TomcatEmbeddedServletContainerFactory.DEFAULT_PROTOCOL);
                    connector.setPort(port);
                    containerFactory.addAdditionalTomcatConnectors(connector);
                }
            }
        };
    }
}
