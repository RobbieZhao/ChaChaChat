package com.chatserver;

import java.io.*;
import java.net.Socket;

public class HTTPResponse {
    private String responseCode;
    private String contentType;
    private HTTPRequest request;

    private final String workingDir = "websites/";

    public HTTPResponse(HTTPRequest request) {
        String fileName = request.getFileName();
        File tmpFile = new File(workingDir + fileName);
        if (tmpFile.exists()) {
            responseCode = "HTTP/1.1 200 OK";
        } else {
            responseCode = "HTTP/1.1 404 NOT FOUND";
            contentType = "text/html";
        }

        if (fileName.endsWith(".html")) {
            contentType = "text/html";
        } else if (fileName.endsWith(".css")) {
            contentType = "text/css";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
            contentType = "image/gif";
        }

        this.request = request;
    }

    public void sendResponse(Socket socket) throws IOException {
        if (request.fileExists(workingDir)) {
            sendFile(socket, workingDir + request.getFileName());
        } else {
            send_404(socket);
        }
    }

    public void sendFile(Socket socket, String filePath) {
        try {

            File file = new File(filePath);
            byte[] data = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeBytes(responseCode + "\r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + data.length);
            dos.writeBytes("\r\n\r\n");
//            while(fis.available() > 0) {
//                dos.write(fis.read());
//                dos.flush();
////                Thread.sleep(1);
//            }
            fis.transferTo(dos);
            dos.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    public void send_404(Socket socket) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            dos.writeBytes(responseCode + "\r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("\r\n\r\n");
            dos.writeBytes("<h1> [404] The file you have requested is not found. </h1>\r\n");
            dos.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

