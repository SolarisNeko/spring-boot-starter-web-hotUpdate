package com.neko233.hotdeploy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class HotDeployApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(HotDeployApplication.class, args);

        for (String beanDefinitionName : applicationContext.getBeanDefinitionNames()) {
            if (beanDefinitionName.startsWith("com.neko233")) {
                System.out.println(beanDefinitionName);
            }
        }
    }

}
