package com.dnlkk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dnlkk.DITest.DnlkkTestApp;
import com.dnlkk.DITest.logic.Dummy;
import com.dnlkk.DITest.logic.TestComponent;

@DisplayName("Dependency Injector DnlkkApp Tests")
public class DependencyInjectorTest {
    
    private DnlkkTestApp dnlkkApp;

    public void setUp() {
        dnlkkApp = (DnlkkTestApp) DnlkkApplication.run(DnlkkTestApp.class, new String[]{});
    }

    @BeforeEach
    void beforeEach() {
        this.setUp();
    }

    @Nested
    @DisplayName("Peas prototypes test")
    class PeasPrototypesTest {

        @Test
        @DisplayName("Prototype peas not null test")
        public void prototypePeasNotNullTest() {
            // MyComponent
            assertNotNull(dnlkkApp.getMyComponent());
            assertNotNull(dnlkkApp.getDefaultComponent());
            assertNotNull(dnlkkApp.getDefaultComponent1());
            assertNotNull(dnlkkApp.getComponentProto());

            // DnlkkComponent
            assertNotNull(dnlkkApp.getDnlkkComponent2());
            assertNotNull(dnlkkApp.getDnlkkComponent22());
        }

        @Test
        @DisplayName("Prototype peas MyComponent not equals test")
        public void myComponentPrototypeTest() {
            assertNotEquals(dnlkkApp.getMyComponent(), dnlkkApp.getDefaultComponent());
            assertNotEquals(dnlkkApp.getMyComponent(), dnlkkApp.getDefaultComponent1());
            assertNotEquals(dnlkkApp.getDefaultComponent(), dnlkkApp.getDefaultComponent1());
            assertNotEquals(dnlkkApp.getMyComponent(), dnlkkApp.getComponentProto());
        }

        @Test
        @DisplayName("Prototype peas DnlkkComponent not equals test")
        public void dnlkkComponentPrototypeTest() {
            assertNotEquals(dnlkkApp.getDnlkkComponent2(), dnlkkApp.getDnlkkComponent22());
        }
    }

    @Nested
    @DisplayName("Peas singleton test")
    class PeasSingletonTest {
        @Test
        @DisplayName("Singleton peas not null test")
        public void singletonPeasNotNullTest() {
            // MyComponent
            assertNotNull(dnlkkApp.getDnlkkComponent2weqwrq());
            assertNotNull(dnlkkApp.getDnlkkComponent2weqwrqq());
            assertNotNull(dnlkkApp.getDefaultComponentSingleton());
            assertNotNull(dnlkkApp.getDefaultComponentSingleton2());

            // DnlkkComponent
            assertNotNull(dnlkkApp.getMyComponentTest2());
            assertNotNull(dnlkkApp.getComponent());

            // TestComponent
            assertNotNull(dnlkkApp.getTestComponent());
        }

        @Test
        @DisplayName("Singleton peas MyComponent equals test")
        public void myComponentSingletonTest() {
            assertEquals(dnlkkApp.getDefaultComponentSingleton2(), dnlkkApp.getDnlkkComponent2weqwrqq());
            assertEquals(dnlkkApp.getDefaultComponentSingleton2(), dnlkkApp.getDefaultComponentSingleton());
            assertEquals(dnlkkApp.getDnlkkComponent2weqwrq(), dnlkkApp.getDefaultComponentSingleton2());
        }
        
        @Test
        @DisplayName("Singleton peas DnlkkComponent equals test")
        public void dnlkkComponentSingletonTest() {
            assertEquals(dnlkkApp.getMyComponentTest2(), dnlkkApp.getComponent());
        }

        @Test
        @DisplayName("Singleton pea TestComponent have correct @AutoInject fields test")
        public void testComponentSingletonRecursiveDITest() {
            TestComponent testComponent = dnlkkApp.getTestComponent();

            assertNotNull(testComponent);
            assertEquals("hi!", testComponent.getText());

            Dummy dummy = testComponent.getDummy();

            assertNotNull(dummy);
            assertEquals("silly!", dummy.getText());
            assertEquals("Bobby", dummy.getBobby().getName());
        }
    }
}