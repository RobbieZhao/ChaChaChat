package com.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

public class WebSocket {

    public WebSocket(HTTPRequest httpRequest, Socket socket) throws IOException, NoSuchAlgorithmException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeBytes("HTTP/1.1 101 Switching Protocols" + "\r\n");
        dos.writeBytes("Upgrade: websocket" + "\r\n");
        dos.writeBytes("Connection: Upgrade" + "\r\n");
        dos.writeBytes("Sec-WebSocket-Accept:" + getSecWebSocketAccept(httpRequest) + "\r\n\r\n");
    }

    public String getSecWebSocketAccept(HTTPRequest httpRequest) throws NoSuchAlgorithmException {
        String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String joinedString = httpRequest.getHeaderValue("Sec-WebSocket-Key") + magicString;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return Base64.getEncoder().encodeToString(md.digest(joinedString.getBytes()));
    }

    public static String[] readMessage(Socket socket) throws IOException {
        String clientMessage = "";

        DataInputStream in = new DataInputStream(socket.getInputStream());

        byte firstByte = in.readByte();
        int finCode = (firstByte & 0xff) >> 7;
        int opCode = firstByte & 0xf;

        byte secondByte = in.readByte();
        int mask = (secondByte & 0xff) >> 7;
        int payloadLength = secondByte & 0x7f;

        if (payloadLength <= 125) {
            byte[] maskKey = new byte[4];
            in.read(maskKey);

            byte[] clientMessageBytes = new byte[payloadLength];
            in.read(clientMessageBytes);

            for (int i = 0; i < payloadLength; i++) {
                int realNumber = clientMessageBytes[i] ^ maskKey[i % 4];
                char c = (char) (realNumber & 0xFF);
                clientMessage += c;
            }
        }

        System.out.println("message received " + clientMessage);

        String firstPart = clientMessage.substring(0, clientMessage.indexOf(' '));
        String secondPart = clientMessage.substring(clientMessage.indexOf(' ') + 1);

        return new String[]{firstPart, secondPart};
    }

    public void handleMessage(SocketChannel socketChannel, Map<String, Room> roomMap) throws IOException {
        String[] messageArr = readMessage(socketChannel.socket());

        String roomName = messageArr[1];
        if (roomMap.containsKey(roomName)) {
            roomMap.get(roomName).addClient(socketChannel);
            System.out.println("joined a existing room");
        } else {
            Room room = new Room(socketChannel);
            roomMap.put(roomName, room);
            System.out.println("Created a new room: " + roomName);
        }
    }


    public static byte[] encodeMessage(String message) {
        byte[] messageBytes = new byte[2 + message.length()];
        messageBytes[0] = (byte)(-127);
        messageBytes[1] = (byte) message.length();
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            messageBytes[i + 2] = (byte) c;
        }
        return messageBytes;
    }

    public static String getJSONMessage(String username, String message) {
        return "{ \"user\" : \"" + username + "\", \"message\" : \"" + message + "\" }";
    }

    public static void sendMessage(Socket socket, String username, String message) throws IOException {
        byte[] messageBytes;
        messageBytes = encodeMessage(getJSONMessage(username, message));
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.write(messageBytes);
        System.out.println("message sent " + message);
    }

}
