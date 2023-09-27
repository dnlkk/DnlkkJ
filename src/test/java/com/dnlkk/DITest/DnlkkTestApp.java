package com.dnlkk.DITest;
import java.util.List;

import com.dnlkk.DITest.logic.Component;
import com.dnlkk.DITest.logic.MyComponent;
import com.dnlkk.DITest.logic.TestComponent;
import com.dnlkk.DITest.service.MyService;
import com.dnlkk.boot.annotations.DnlkkApp;
import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.annotations.lifecycle.Prototype;
import com.dnlkk.dependency_injector.annotations.lifecycle.Singleton;

import lombok.Data;

@Data
@DnlkkApp
public class DnlkkTestApp {
    @AutoInject
    @Prototype
    private Component myComponent;

    @ConcreteInject("myComponent")
    @Prototype
    private Component defaultComponent;

    @ConcreteInject("myComponent")
    @Prototype
    private Component defaultComponent1;

    @AutoInject
    @Prototype
    private Component dnlkkComponent2;
    
    @ConcreteInject("dnlkkComponent2")
    @Prototype
    private Component dnlkkComponent22;

    @ConcreteInject("myComponent")
    private MyComponent dnlkkComponent2weqwrq;
    @ConcreteInject("myComponent")
    @Singleton
    private MyComponent dnlkkComponent2weqwrqq;


    @ConcreteInject("myComponent")
    @Singleton
    private Component defaultComponentSingleton;

    @ConcreteInject("myComponent")
    private Component defaultComponentSingleton2;

    @ConcreteInject("dnlkkComponent2")
    private Component component;

    @Prototype
    @ConcreteInject("myComponent")
    private Component componentProto;

    @ConcreteInject(value = "dnlkkComponent2")
    private Component myComponentTest2;
    
    @AutoInject
    private TestComponent testComponent;

    @AutoInject
    private MyService myService;

    public void runApp() {
        System.out.println(myService);

        System.out.println(myComponent);
        myComponent.doSomething();

        System.out.println(defaultComponent);
        defaultComponent.doSomething();

        System.out.println(defaultComponent1);
        defaultComponent1.doSomething();

        System.out.println(dnlkkComponent2);
        dnlkkComponent2.doSomething();

        System.out.println(dnlkkComponent22);
        dnlkkComponent22.doSomething();

        System.out.println(dnlkkComponent2weqwrq);
        dnlkkComponent2weqwrq.doSomething();
        System.out.println(dnlkkComponent2weqwrqq);
        dnlkkComponent2weqwrqq.doSomething();

        System.out.println(defaultComponentSingleton);
        defaultComponentSingleton.doSomething();

        System.out.println(defaultComponentSingleton2);
        defaultComponentSingleton2.doSomething();

        System.out.println(component);
        component.doSomething();

        System.out.println(componentProto);
        componentProto.doSomething();

        System.out.println(myComponentTest2);
        myComponentTest2.doSomething();

        System.out.println(testComponent);
        System.out.println(testComponent.getText());
        System.out.println(testComponent.getDummy().getText());
    }

    public static <T> void toPring(List<T> s) {
        System.out.println("start findAll:");
        s.stream().forEach(str -> System.out.println(str));
        System.out.println("end findAll:");
    }
}