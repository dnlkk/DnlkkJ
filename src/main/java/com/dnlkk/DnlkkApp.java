package com.dnlkk;

import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.annotations.lifecycle.Prototype;
import com.dnlkk.dependency_injector.annotations.lifecycle.Singleton;
import com.dnlkk.dependency_injector.DependencyInjector;
import com.dnlkk.dependency_injector.config.DependencyInjectionContainer;

public class DnlkkApp {
    @AutoInject
    @Prototype
    private Component myComponent;

    @ConcreteInject(injectName = "myComponent")
    @Prototype
    private Component defaultComponent;

    @ConcreteInject(injectName = "myComponent")
    @Prototype
    private Component defaultComponent1;

    @AutoInject
    @Prototype
    private Component dnlkkComponent2;
    
    @ConcreteInject(injectName = "dnlkkComponent2")
    @Prototype
    private Component dnlkkComponent22;

    @AutoInject
    private MyComponent dnlkkComponent2weqwrq;
    @AutoInject
    @Singleton
    private MyComponent dnlkkComponent2weqwrqq;


    @ConcreteInject(injectName = "myComponent")
    @Singleton
    private MyComponent defaultComponentSingleton;

    @ConcreteInject(injectName = "myComponent")
    private MyComponent defaultComponentSingleton2;

    @ConcreteInject(injectName = "myComponent")
    private Component component;

    @Prototype
    @ConcreteInject(injectName = "myComponent")
    private Component componentProto;

    @AutoInject
    private DnlkkComponent myComponentTest;
    
    @AutoInject
    private TestComponent testComponent;

    public void runApp() {
        DependencyInjectionContainer dependencyInjectionContainer = new DependencyInjectionContainer();
        dependencyInjectionContainer.scan(this.getClass().getPackageName());
        DependencyInjector dependencyInjector = new DependencyInjector(dependencyInjectionContainer);
        dependencyInjector.inject(this);

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

        System.out.println(myComponentTest);
        myComponentTest.doSomething();

        System.out.println(testComponent);
        System.out.println(testComponent.getText());
        System.out.println(testComponent.getDummy().getText());
    }
}