package com.dnlkk;

import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.DependencyInjector;
import com.dnlkk.dependency_injector.config.ComponentFactory;

public class DnlkkApp {
    @AutoInject
    private Component myComponent;
    @AutoInject
    private Component defaultComponent;
    @ConcreteInject(className = "MyComponent")
    private Component component;

    @AutoInject
    private DnlkkComponent dnlkkComponent;
    
    @AutoInject
    private TestComponent testComponent;

    public void runApp() {
        ComponentFactory сomponentFactory = new ComponentFactory();
        сomponentFactory.scanAndInject(this.getClass().getPackageName());
        DependencyInjector dependencyInjector = new DependencyInjector(сomponentFactory);
        dependencyInjector.inject(this);

        System.out.println(myComponent);
        myComponent.doSomething();

        System.out.println(defaultComponent);
        defaultComponent.doSomething();

        System.out.println(component);
        component.doSomething();

        System.out.println(dnlkkComponent);
        dnlkkComponent.doSomething();

        System.out.println(testComponent);
        System.out.println(testComponent.getText());
    }
}