package com.neko233.hotdeploy.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author SolarisNeko
 * @date 6/11/2022
 */
@Slf4j
public class SpringHotUpdateHelper implements ApplicationContextAware {

    /**
     * 项目的 Application Context
     */
    private ConfigurableApplicationContext applicationContext;

    /**
     * get BeanFactory 强转 DefaultListableBeanFactory (包扫描的 Bean definition 记录工厂)
     */
    private DefaultListableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        this.beanFactory = (DefaultListableBeanFactory) this.applicationContext.getBeanFactory();

        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        Set<String> names = Arrays.stream(beanDefinitionNames)
                .filter(name -> name.startsWith("com.neko233"))
                .collect(Collectors.toSet());
        System.out.println(names);
    }


    public void refreshBeanByClassName(String className) throws ClassNotFoundException {
        unregisterBean(className);
        registerBean(className);
    }

    public void refreshControllerByClassName(String className)
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        unregisterController(className);
        registerController(className);
    }


    /**
     * 注册bean
     */
    public void registerBean(String className) throws ClassNotFoundException {
        Class<?> toLoadClass = Class.forName(className);

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(toLoadClass);
        BeanDefinition newBeanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        newBeanDefinition.setScope("singleton");
        beanFactory.registerBeanDefinition(className, newBeanDefinition);
    }


    /**
     * 卸载bean
     *
     * @param className 作为 beanName
     */
    public void unregisterBean(String className) {
        beanFactory.removeBeanDefinition(className);
    }


    /**
     * 获取所有bean
     *
     * @return beanName List
     */
    public List<String> getBeans() {
        return Arrays.asList(applicationContext.getBeanDefinitionNames());
    }


    /**
     * get Bean
     *
     * @param className class Name
     * @return Bean instance
     */
    public Object getBeanByClassName(String className) {
        return applicationContext.getBean(className);
    }


    /**
     * 注册controller
     *
     * @param className 类名
     */
    public void registerController(String className)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        registerBean(className);

        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean(
                RequestMappingHandlerMapping.class);

        // reflect get RequestMapping Class & get detect Handler to @RequestMapping Method
        Method detectHandlerMethod = requestMappingHandlerMapping.getClass()
                .getSuperclass()
                .getSuperclass()
                .getDeclaredMethod("detectHandlerMethods", Object.class);
        detectHandlerMethod.setAccessible(true);
        detectHandlerMethod.invoke(requestMappingHandlerMapping, className);
    }


    /**
     * 卸载controller
     *
     * @param className class Name
     */
    public void unregisterController(String className) {
        // request Mapping Handler store the Route for @RequestMapping : Handler
        final RequestMappingHandlerMapping requestMappingHandlerMapping =
                (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
        Object controller = applicationContext.getBean(className);

        final Class<?> targetClass = controller.getClass();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
            try {
                Method createMappingMethod = RequestMappingHandlerMapping.class.getDeclaredMethod(
                        "getMappingForMethod", Method.class, Class.class);

                createMappingMethod.setAccessible(true);
                RequestMappingInfo requestMappingInfo = (RequestMappingInfo)
                        createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);

                if (requestMappingInfo != null) {
                    requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                }
            } catch (Exception e) {
                log.error("Request Mapping Reflect error! ", e);
                e.printStackTrace();
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        unregisterBean(className);
    }


    public void refreshAllContext(String prefixClassPath)
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Field refreshed = GenericApplicationContext.class.getDeclaredField("refreshed");
        refreshed.setAccessible(true);
        refreshed.set(applicationContext, new AtomicBoolean(false));

        Method refreshBeanFactory = AbstractApplicationContext.class.getDeclaredMethod("refreshBeanFactory");
        refreshBeanFactory.setAccessible(true);
        AbstractApplicationContext abstractContext = (AbstractApplicationContext) applicationContext;
        refreshBeanFactory.invoke(abstractContext);

        beanFactory = (DefaultListableBeanFactory) abstractContext.getBeanFactory();

        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            if (beanDefinitionName.startsWith(prefixClassPath)) {
                unregisterController(beanDefinitionName);
                registerController(beanDefinitionName);
            }
        }
    }

    @Deprecated
    public void oldRefreshAll() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        Field refreshed = GenericApplicationContext.class.getDeclaredField("refreshed");
        refreshed.setAccessible(true);
        refreshed.set(applicationContext, new AtomicBoolean(false));

        beanFactory.destroySingletons();
//        System.out.println(beanFactory.hashCode());

        Field applicationContextBeanFactory = GenericApplicationContext.class.getDeclaredField("beanFactory");
        applicationContextBeanFactory.setAccessible(true);
        DefaultListableBeanFactory newBeanFactory = new DefaultListableBeanFactory();
        applicationContextBeanFactory.set(applicationContext, newBeanFactory);

        Method onRefreshWebServer = GenericApplicationContext.class.getDeclaredMethod("onRefresh");
        onRefreshWebServer.setAccessible(true);

        beanFactory = newBeanFactory;
        applicationContext.refresh();
    }
}
