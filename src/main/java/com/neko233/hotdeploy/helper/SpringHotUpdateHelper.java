package com.neko233.hotdeploy.helper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.neko233.hotdeploy.scanner.SimplePackageScanner;
import com.neko233.hotdeploy.util.SplitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestController;
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
    }


    public void refreshBeanByClassName(String className) throws ClassNotFoundException {
        unregisterBean(SplitUtil.getBeanNameByClassName(className));
        registerBean(SplitUtil.getBeanNameByClassName(className));
    }

    public void refreshControllerByClassName(String className)
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        unregisterController(SplitUtil.getBeanNameByClassName(className));
        registerController(SplitUtil.getBeanNameByClassName(className));
    }


    /**
     * 注册bean
     */
    public void registerBean(String className) throws ClassNotFoundException {
        Class<?> toLoadClass = Class.forName(className);

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(toLoadClass);
        BeanDefinition newBeanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        newBeanDefinition.setScope("singleton");
        beanFactory.registerBeanDefinition(SplitUtil.getBeanNameByClassName(className), newBeanDefinition);
    }


    /**
     * 卸载bean
     *
     * @param className 作为 beanName
     */
    public void unregisterBean(String className) {
        beanFactory.removeBeanDefinition(SplitUtil.getBeanNameByClassName(className));
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

        registerBean(SplitUtil.getBeanNameByClassName(className));

        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean(
                RequestMappingHandlerMapping.class);

        // reflect get RequestMapping Class & get detect Handler to @RequestMapping Method
        Method detectHandlerMethod = requestMappingHandlerMapping.getClass()
                .getSuperclass()
                .getSuperclass()
                .getDeclaredMethod("detectHandlerMethods", Object.class);
        detectHandlerMethod.setAccessible(true);
        detectHandlerMethod.invoke(requestMappingHandlerMapping, SplitUtil.getBeanNameByClassName(className));
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
        Object controller = applicationContext.getBean(SplitUtil.getBeanNameByClassName(className));

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

        unregisterBean(SplitUtil.getBeanNameByClassName(className));
    }

    /**
     * refresh all user define class BeanDefinition
     * @return ok ?
     */

    public boolean refreshAllUserBeanDefinition() {
        boolean isOk = true;
        List<Class> allClassList = null;
        try {
            allClassList = SimplePackageScanner.getAllClasses();
        } catch (IOException e) {
            e.printStackTrace();
            isOk = false;
        }

        if (allClassList == null || allClassList.size() == 0) {
            return false;
        }

        List<String> allClassPathList = allClassList.stream()
                .filter(aClass -> {
                    if (aClass.getAnnotation(Component.class) != null) {
                        return true;
                    }
                    if (aClass.getAnnotation(Configuration.class) != null) {
                        return true;
                    }
                    if (aClass.getAnnotation(Repository.class) != null) {
                        return true;
                    }
                    if (aClass.getAnnotation(Service.class) != null) {
                        return true;
                    }
                    if (aClass.getAnnotation(Controller.class) != null) {
                        return true;
                    }
                    if (aClass.getAnnotation(RestController.class) != null) {
                        return true;
                    }
                    return false;
                })
                .map(Class::getName)
                .collect(Collectors.toList());

        if (allClassPathList.size() == 0) {
            return false;
        }

        for (String classPath : allClassPathList) {
            unregisterBean(classPath);
            try {
                registerBean(classPath);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                isOk = false;
            }
        }
        return isOk;
    }


}
