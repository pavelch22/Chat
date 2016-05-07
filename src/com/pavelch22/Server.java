package com.pavelch22;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server implementation.
 */
public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    /**
     * Sends a broadcast message.
     *
     * @param message a message to send.
     */
    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("An error has occurred. Message to " + pair.getKey() + " has not been sent.");
            }
        }
    }

    /**
     * Starts the server.
     */
    public void run() {
        ServerSocket serverSocket = null;
        try {
            ConsoleHelper.writeMessage("Enter a port number.");
            int port = ConsoleHelper.readInt();
            serverSocket = new ServerSocket(port);
            ConsoleHelper.writeMessage("Server is online.");
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage(e.toString());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message receivedMessage = connection.receive();
                if (receivedMessage.getType() == MessageType.USER_NAME) {
                    String userName = receivedMessage.getData();
                    if (userName != null && !userName.isEmpty() && !connectionMap.containsKey(userName)) {
                        connectionMap.put(userName, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return userName;
                    }
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                if (!pair.getKey().equals(userName)) {
                    Message message = new Message(MessageType.USER_ADDED, pair.getKey());
                    connection.send(message);
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message receivedMessage = connection.receive();
                if (receivedMessage.getType() == MessageType.TEXT) {
                    Message message = new Message(MessageType.TEXT, userName + ": " + receivedMessage.getData());
                    sendBroadcastMessage(message);
                } else {
                    ConsoleHelper.writeMessage("Error. It's not a text message.");
                }
            }
        }

        @Override
        public void run() {
            Connection connection = null;
            String userName = null;
            try {
                ConsoleHelper.writeMessage("New connection with " + socket.getRemoteSocketAddress() + " is established.");
                connection = new Connection(socket);
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                sendListOfUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Error. Problem with communication with the remote address.");
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
                ConsoleHelper.writeMessage("Connection closed.");
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
