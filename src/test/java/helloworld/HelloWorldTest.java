package helloworld;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import helloworld.HelloWorld;

public class HelloWorldTest {

    @Test
    void testGetFive() {
        HelloWorld obj = new HelloWorld();
        Assertions.assertEquals(5, obj.getFive());
    }
}
