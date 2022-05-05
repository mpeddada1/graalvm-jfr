package com.example;

import jdk.jfr.Event;
import jdk.jfr.Description;
import jdk.jfr.Label;

public class JfrExample {

    // @Label("Hello World")
    // @Description("Helps programmer getting started")
    // static class HelloWorldEvent extends Event {
    //     @Label("Message")
    //     String message;
    // }
    //
    // public static void main(String... args) {
    //     System.out.println(JfrFeature.class);
    //     HelloWorldEvent event = new HelloWorldEvent();
    //     event.message = "hello, world!";
    //     event.commit();
    // }
    public static void main(String... args) {
      System.out.println("HELLLOOO");
      MyFeature feat = new MyFeature();
    }
}