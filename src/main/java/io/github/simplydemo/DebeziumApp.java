package io.github.simplydemo;

import io.github.simplydemo.debezium.MysqChangeDataCapture;

public class DebeziumApp {

    public static void main(String[] args) {
        new MysqChangeDataCapture().run();
    }

}
