package com.dnlkk.DITest;

import com.dnlkk.DnlkkApplication;
import com.dnlkk.boot.annotations.DnlkkApp;
import com.dnlkk.dependency_injector.annotations.AutoInject;
import com.dnlkk.dependency_injector.annotations.ConcreteInject;
import com.dnlkk.dependency_injector.annotations.lifecycle.Prototype;

/**
 * Hello world!
 *
 */
@DnlkkApp
public class App {
    @Prototype
    @ConcreteInject("dnlkkComponent2")
    private DnlkkComponent dnlkkComponent;
    @AutoInject
    private DnlkkComponent dnlkkComponent2;
    @ConcreteInject("dnlkkComponent2")
    private DnlkkComponent dnlkkComponent25;
    @AutoInject
    private TestComponent testComponent;
    @AutoInject
    private TestRepository testRepository;

    public static void main(String[] args) {
        DnlkkApplication.run(DnlkkTestApp.class, args);
        System.out.println("hello!");
    }

    public void runApp() {
        System.out.println(dnlkkComponent);
        System.out.println(dnlkkComponent2);
        System.out.println(dnlkkComponent25);
        System.out.println(testComponent);
        System.out.println(testRepository.findByName("Ruslan"));
    }
}
