package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketServer implements Runnable {

    public static final int PORT = 8081;
    private static final Map<String, List<ClientHandler>> roomClients = new ConcurrentHashMap<>();

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("SocketServer listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.out.println("SocketServer not started (port busy or unavailable): " + e.getMessage());
        }
    }

    public static void addClient(String roomId, ClientHandler handler) {
        roomClients.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public static void removeClient(String roomId, ClientHandler handler) {
        List<ClientHandler> list = roomClients.get(roomId);
        if (list != null) {
            list.remove(handler);
            if (list.isEmpty()) roomClients.remove(roomId);
        }
    }

    public static void broadcastToRoom(String roomId, String message, ClientHandler sender) {
        List<ClientHandler> list = roomClients.get(roomId);
        if (list == null) return;
        for (ClientHandler h : list) {
            if (h != sender) h.send(message);
        }
    }
}