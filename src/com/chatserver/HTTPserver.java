package com.chatserver;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class HTTPserver {
    private static int portNumber = 8080;
    private Map<String, Room> roomMap = new HashMap<>();

    public void initiate() throws Exception {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(portNumber));

        while (true) {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();

                HTTPRequest httpRequest = new HTTPRequest(socketChannel.socket());
                if (httpRequest.isWebSocket()) {
                    // By creating a WebSocket Object, we build handshakes
                    WebSocket webSocket = new WebSocket(httpRequest, socketChannel.socket());
                    // Handle the message
                    webSocket.handleMessage(socketChannel, roomMap);
                } else {
                    HTTPResponse httpResponse = new HTTPResponse(httpRequest);
                    httpResponse.sendResponse(socketChannel.socket());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


