package com.chatserver;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("started");
        HTTPserver httpserver = new HTTPserver();
        httpserver.initiate();
    }
}
