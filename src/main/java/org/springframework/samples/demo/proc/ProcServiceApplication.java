package org.springframework.samples.demo.proc;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

//import brave.spring.web.TracingClientHttpRequestInterceptor;

/**
 * @author wdongyu
 */
@EnableDiscoveryClient
@EnableWebMvc
@SpringBootApplication
public class ProcServiceApplication {

    //@LoadBalanced
    //@Autowired TracingClientHttpRequestInterceptor clientInterceptor;

    private Logger logger = Logger.getLogger("App ~~~~~~~~~~~~~~");

    @Bean RestTemplate restTemplate() {
        RestTemplate r = new RestTemplate();
        //r.setInterceptors(Collections.singletonList(clientInterceptor));
        //logger.info(clientInterceptor.toString());
        return r;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProcServiceApplication.class, args);
    }
}
