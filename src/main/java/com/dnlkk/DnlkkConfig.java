package com.dnlkk;

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
    public TestComponent testComponent() {
        return new TestComponent("hi!");
    }
}