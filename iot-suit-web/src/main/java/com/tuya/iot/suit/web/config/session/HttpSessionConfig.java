package com.tuya.iot.suit.web.config.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

@Configuration
@EnableSpringHttpSession
public class HttpSessionConfig {

    @Value("${smart.office.http.session.timeout:7200}")
    private Integer timeout;

    /**
     * session策略，这里默认会从头部，请求参数中获取内容
     * 这里的token 可以自定义，主要用于请求参数的名字
     *
     * @return
     */
    @Bean
    public HttpSessionStrategy httpSessionStrategy() {
        return new HttpSessionStrategy("token");
    }

}
