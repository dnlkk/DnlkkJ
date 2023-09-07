package com.dnlkk;

import com.dnlkk.dependency_injector.annotations.Pea;
import com.dnlkk.dependency_injector.config.Config;

@Config
public class DnlkkConfig2 {
    
    @Pea
    public MyComponent myComponentTest2() {
        return new MyComponent();
    }
}