package com.dnlkk.WebTest;

import com.dnlkk.DnlkkApplication;
import com.dnlkk.boot.annotations.DnlkkWeb;

/**
 * Hello world!
 *
 */
@DnlkkWeb
public class App {
    public static void main(String[] args) {
        DnlkkApplication.run(App.class, args);
        System.out.println("hello!");
    }
}
