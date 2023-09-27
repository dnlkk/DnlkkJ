package com.dnlkk.DITest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.dnlkk.DITest.logic.Component;
import com.dnlkk.DITest.logic.MyComponent;
import com.dnlkk.DITest.logic.TestComponent;
import com.dnlkk.DITest.model.User;
import com.dnlkk.DITest.model.UserOperation;
import com.dnlkk.DITest.repository.TestRepository;
import com.dnlkk.DITest.service.MyService;
import com.dnlkk.boot.annotations.ConfigValue;
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
    private TestRepository repo;

    @ConfigValue("app.version")
    private String version;

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

        System.out.println(repo);
        System.out.println(repo.findAll());
        // User user = repo.findById(1);
        // System.out.println(user);
        // user.setName("Ruslan");
        // System.out.println(repo.save(user));

        User userNew = new User();
        userNew.setId(8);
        userNew.setName("Ruslan");
        userNew.setSurname("tuki");
        userNew.setAge(3);
        UserOperation userOperation1 = new UserOperation();
        userOperation1.setAmount(new BigDecimal(4742));
        userOperation1.setOperationId(4);
        userOperation1.setFrom(userNew);
        userOperation1.setTo(repo.findById(2));

        UserOperation userOperation2 = new UserOperation();
        userOperation2.setAmount(BigDecimal.valueOf(33.47));
        userOperation2.setOperationId(5);
        userOperation2.setFrom(userNew);
        userOperation2.setTo(repo.findById(9));
        userNew.setUserFromOperations(List.of(userOperation1, userOperation2));

        UserOperation userOperation3 = new UserOperation();
        userOperation3.setAmount(BigDecimal.valueOf(1.22));
        userOperation3.setOperationId(20);
        userOperation3.setFrom(userNew);
        userOperation3.setTo(repo.findById(1));
        userNew.setUserFromOperations(List.of(userOperation1, userOperation2, userOperation3));
        System.out.println("entity");
        System.out.println(userNew);
        System.out.println(repo.save(userNew));
        System.out.println(repo.findByNameAndSurnameOrId("toki", "tuki", 3));
        System.out.println(repo.findByName("toki"));

        System.out.println(repo.countByName("Ruslan"));

        System.out.println(version);

        System.out.println(repo.testSelect(2));
        System.out.println(repo.testSelect(2));
        System.out.println(repo.testSelect(2));
        System.out.println(repo.testSelect(2));
        System.out.println(repo.testSelect(2));
        System.out.println(repo.testSelect(2));
        System.out.println(repo.sumAgeByName("Ruslan"));
        toPring(repo.findAll());
    }

    public static <T> void toPring(List<T> s) {
        System.out.println("start findAll:");
        s.stream().forEach(str -> System.out.println(str));
        System.out.println("end findAll:");
    }
}