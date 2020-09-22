package com.chatserver;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.*;
import java.util.*;

public class Room {
    private Selector selector;
    private ArrayList<SocketChannel> clientsInTheRoom = new ArrayList<>();
    private ArrayList<SocketChannel> clientsToBeAdded = new ArrayList<>();
    private ArrayList<String[]> allMessages = new ArrayList<>();

    /**
     * The constructor take a socket channel as a parameter
     * Because we call the constructor when a client wants to join a room that doesn't exist
     * And we create a new room for the client, and add the client to the room
    */
    public Room(SocketChannel socketChannel) throws IOException {
        // This is a constructor call
        this.selector = Selector.open();

        // A Channel has to be in non-blocking mode before it can be registered with the selector
        socketChannel.configureBlocking(false);
        this.selector.selectNow(); // Ben's slides say that this is to make the selector happy
        socketChannel.register(selector, SelectionKey.OP_READ);

        clientsInTheRoom.add(socketChannel);

        // Create a new thread for the new room
        // In this project, one thread handles all for one room
        Thread thread = new Thread(() -> {
            try {
                serverRoom();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    /**
     * The synchronized keyword makes sure that
     * only one client channel is being added to the room at one time
     */
    public synchronized void addClient(SocketChannel socketChannel) {
        clientsToBeAdded.add(socketChannel);
        // This will wake up the selector even it doesn't have a readable key
        selector.wakeup();
    }

    /**
     * We need the synchonized key word since the selector, clientsInTheRoom,
     * and the clientsToBeAdded variables are being shared
     */
    public synchronized void addAllClients() throws IOException {
        for (SocketChannel socketChannel : clientsToBeAdded) {
            socketChannel.configureBlocking(false);
            selector.selectNow();
            socketChannel.register(selector, SelectionKey.OP_READ);

            sendMessages(socketChannel, allMessages);

            clientsInTheRoom.add(socketChannel);
        }
        clientsToBeAdded.clear();
    }


    public void sendMessagesToAllClients(ArrayList<String[]> newMessages) throws IOException {
        for (SocketChannel socketChannel : clientsInTheRoom) {
            sendMessages(socketChannel, newMessages);
        }
    }

    public void sendMessages(SocketChannel socketChannel, ArrayList<String[]> messages) throws IOException {
        for (var message : messages) {
            SelectionKey key = socketChannel.keyFor(selector);
            key.cancel();

            socketChannel.configureBlocking(true);
            WebSocket.sendMessage(socketChannel.socket(), message[0], message[1]);

            socketChannel.configureBlocking(false);
            selector.selectNow();
            socketChannel.register(selector, SelectionKey.OP_READ);
        }
    }


    public void serverRoom() throws IOException {
        while (true) {
            selector.select();

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            ArrayList<String[]> newMessages = new ArrayList<>();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    iterator.remove();
                    key.cancel();
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    socketChannel.configureBlocking(true);

                    Socket socket = socketChannel.socket();
                    String[] tmpArr = WebSocket.readMessage(socket);
                    newMessages.add(new String[]{tmpArr[0], tmpArr[1]});
                    allMessages.add(new String[]{tmpArr[0], tmpArr[1]});

                    socketChannel.configureBlocking(false);
                    selector.selectNow();
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }
            addAllClients();
            sendMessagesToAllClients(newMessages);
        }
    }
}
