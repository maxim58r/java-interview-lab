package ru.max.test.test4;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Configuration
@ComponentScan
public class AppAware {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppAware.class);
        ctx.close();
    }
}

@Component
class DemoAware implements BeanNameAware, BeanClassLoaderAware, BeanFactoryAware,
        EnvironmentAware, ResourceLoaderAware, ApplicationContextAware,
        InitializingBean {

    @Autowired(required = false)
    private Environment injectedEnv;

    @Override
    public void setBeanName(String name) {
        System.out.println("1 BeanNameAware: " + name);
    }

    @Override
    public void setBeanClassLoader(ClassLoader cl) {
        System.out.println("2 BeanClassLoaderAware: " + cl);
    }

    @Override
    public void setBeanFactory(BeanFactory bf) {
        System.out.println("3 BeanFactoryAware: " + bf.getClass().getSimpleName());
    }

    @Override
    public void setEnvironment(Environment env) {
        System.out.println("4 EnvironmentAware: " + env.getProperty("spring.application.name"));
    }

    @Override
    public void setResourceLoader(ResourceLoader rl) {
        System.out.println("5 ResourceLoaderAware: " + rl.getClass().getSimpleName());
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) {
        System.out.println("6 ApplicationContextAware: " + ac.getId());
    }

    @PostConstruct
    public void initAnno() {
        System.out.println("7 @PostConstruct, envInjected? " + (injectedEnv != null));
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("8 InitializingBean#afterPropertiesSet");
    }
}
