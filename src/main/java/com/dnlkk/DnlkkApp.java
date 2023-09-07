package com.dnlkk;

import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.annotations.lifecycle.Prototype;
import com.dnlkk.dependency_injector.annotations.lifecycle.Singleton;
import com.dnlkk.dependency_injector.DependencyInjector;
import com.dnlkk.dependency_injector.config.ComponentFactory;

public class DnlkkApp {
    @AutoInject
    @Prototype
    private Component myComponent;

    @AutoInject
    @Prototype
    private Component defaultComponent;

    @AutoInject
    @Prototype
    private Component defaultComponent1;

    @AutoInject
    @Prototype
    private Component defaultComponent2;

    @AutoInject
    @Singleton
    private MyComponent defaultComponentSingleton;

    @AutoInject
    private MyComponent defaultComponentSingleton2;

    @ConcreteInject(injectName = "myComponent")
    private Component component;

    @Prototype
    @ConcreteInject(injectName = "myComponent")
    private Component componentProto;

    @AutoInject
    private DnlkkComponent dnlkkComponent;
    
    @AutoInject
    private TestComponent testComponent;

    public void runApp() {
        ComponentFactory сomponentFactory = new ComponentFactory();
        сomponentFactory.scan(this.getClass().getPackageName());
        DependencyInjector dependencyInjector = new DependencyInjector(сomponentFactory);
        dependencyInjector.inject(this);

        System.out.println(myComponent);
        myComponent.doSomething();

        System.out.println(defaultComponent);
        defaultComponent.doSomething();

        System.out.println(defaultComponent1);
        defaultComponent1.doSomething();

        System.out.println(defaultComponent2);
        defaultComponent2.doSomething();

        System.out.println(defaultComponentSingleton);
        defaultComponentSingleton.doSomething();

        System.out.println(defaultComponentSingleton2);
        defaultComponentSingleton2.doSomething();

        System.out.println(component);
        component.doSomething();

        System.out.println(componentProto);
        componentProto.doSomething();

        System.out.println(dnlkkComponent);
        dnlkkComponent.doSomething();
    }
}