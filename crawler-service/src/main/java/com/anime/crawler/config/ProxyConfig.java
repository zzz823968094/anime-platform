package com.anime.crawler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * HTTP代理配置
 * 用于解决国内服务器访问受限API的问题
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {

    private boolean enabled = false;
    private String host = "127.0.0.1";
    private int port = 7890;
    private String type = "http";

    /**
     * 创建Java系统代理
     */
    @Bean
    public Proxy httpProxy() {
        if (!enabled) {
            return Proxy.NO_PROXY;
        }

        // 设置JVM系统属性，使所有HTTP请求使用代理
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", String.valueOf(port));
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", String.valueOf(port));

        // 设置不使用代理的主机（本地服务、Nacos等）
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|::1");

        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }
}
