package com.deliverytech.delivery_api.config;

import com.deliverytech.delivery_api.filters.CorrelationIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {
    private final CorrelationIdFilter correlationIdFilter;

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistrationBean() {
        FilterRegistrationBean<CorrelationIdFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(correlationIdFilter);
        registrationBean.addUrlPatterns("/*");

        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registrationBean;
    }
}
