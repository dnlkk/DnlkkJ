package com.dnlkk.DITest.config;

import com.dnlkk.DITest.logic.Component;
import com.dnlkk.DITest.logic.DnlkkComponent;
import com.dnlkk.DITest.logic.Dummy;
import com.dnlkk.DITest.logic.MyComponent;
import com.dnlkk.DITest.logic.TestComponent;
import com.dnlkk.dependency_injector.annotations.Pea;
import com.dnlkk.dependency_injector.config.Config;

@Config
public class DnlkkConfig {
    
    @Pea
    public Component myComponent() {
        return new MyComponent();
    }
        
    @Pea
    public Component dnlkkComponent2() {
        return new DnlkkComponent();
    }

    @Pea
    public MyComponent myComponentTest() {
        return new MyComponent();
    }
        
    @Pea
    public TestComponent testComponent() {
        return new TestComponent("hi!");
    }

    @Pea
    public Dummy dummy() {
        return new Dummy("silly!");
    } 
}