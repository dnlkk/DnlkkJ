package com.dnlkk.DITest.config;

import com.dnlkk.DITest.logic.Bobby;
import com.dnlkk.DITest.logic.DnlkkComponent;
import com.dnlkk.dependency_injector.annotations.Pea;
import com.dnlkk.dependency_injector.config.Config;

@Config
public class DnlkkConfig2 {
    
    @Pea
    public DnlkkComponent myComponentTest2() {
        return new DnlkkComponent();
    }

    @Pea
    public Bobby bobby() {
        return new Bobby("Bobby", "Dummiev", 13);
    }
}