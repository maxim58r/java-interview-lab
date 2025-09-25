package ru.max.test.test3;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

// Работа прокси
@SpringBootApplication
public class App implements ApplicationRunner {
    @Autowired private OrderService order;
    @Autowired private ApplicationContext ctx;

    public static void main(String[] args) { SpringApplication.run(App.class, args); }

    @Override public void run(ApplicationArguments args) {
        System.out.println("== BEFORE first use ==");
        order.inspect();                 // смотрим на proxy, НО методов не вызываем

        System.out.println("\n== FIRST call ==");
        order.place();                   // первый вызов → создастся target ленивой зависимости

        System.out.println("\n== AFTER first use ==");
        order.inspect();                 // снова смотрим: прокси тот же, target уже создан
    }
}

@Service
class OrderService {
    // ленивое внедрение: сюда придёт ПРОКСИ, а не реальный PaymentService
    @Autowired @Lazy private PaymentService payment;

    public void place() {
        System.out.println("OrderService.place()");
        payment.charge();                // <-- на ЭТОМ вызове создастся target
    }

    public void inspect() {
        Object proxy = payment;
        System.out.println("payment ref           = " + System.identityHashCode(proxy));
        System.out.println("payment class         = " + proxy.getClass().getName());
        System.out.println("isAopProxy            = " + AopUtils.isAopProxy(proxy));
        System.out.println("isJdkProxy            = " + AopUtils.isJdkDynamicProxy(proxy));
        System.out.println("isCglibProxy          = " + AopUtils.isCglibProxy(proxy));
        System.out.println("targetClass (user)    = " + AopUtils.getTargetClass(proxy).getName());
    }
}

@Service
class PaymentService {
    public PaymentService() {
        System.out.println(">> PaymentService ctor (TARGET CREATED)");
    }
    public void charge() {
        System.out.println("PaymentService.charge()");
    }
}
