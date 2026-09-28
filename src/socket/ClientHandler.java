package socket;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String roomId;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void send(String message) {
        if (out != null) out.println(message);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            String line;
            while ((line = in.readLine()) != null) {
                handleMessage(line);
            }
        } catch (IOException e) {
            // client disconnected
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String json) {
        String type = SocketProtocol.getField(json, "type");
        if (type == null) return;

        if ("USER_JOINED".equals(type)) {
            roomId = SocketProtocol.getField(json, "roomId");
            SocketServer.addClient(roomId, this);
        }

        if (roomId != null) {
            SocketServer.broadcastToRoom(roomId, json, this);
        }

        if ("USER_LEFT".equals(type)) {
            cleanup();
        }
    }

    private void cleanup() {
        if (roomId != null) {
            SocketServer.removeClient(roomId, this);
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}