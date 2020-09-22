package com.chatserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
    private String fileRequested;
    private Map<String, String> headerMap = new HashMap<>();

    public HTTPRequest(Socket socket) throws Exception {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = bufferedReader.readLine();

            System.out.println(line);

            String[] strArray = line.split(" ");
            if (strArray[1].equals("/")) {
                fileRequested = "index.html";
            } else {
                fileRequested = strArray[1].substring(1);
            }

            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }
                System.out.println(line);
                String key = line.split(": ")[0];
                String value = line.split(": ")[1];
                headerMap.put(key, value);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new Exception("Bad request!");
        }
    }


    public boolean fileExists(String workingDir) {
        File tmpFile = new File(workingDir + "/" + fileRequested);
        return tmpFile.exists();
    }

    public String getFileName() {
        return fileRequested;
    }

    public boolean isWebSocket() {
        return headerMap.get("Connection").equals("Upgrade");
    }

    public String getHeaderValue(String key) {
        return headerMap.get(key);
    }
}
