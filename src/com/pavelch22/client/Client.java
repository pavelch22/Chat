package com.pavelch22.client;

import com.pavelch22.Connection;
import com.pavelch22.ConsoleHelper;
import com.pavelch22.Message;
import com.pavelch22.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Console client implementation.
 */
public class Client {
    private volatile boolean clientConnected = false;
    protected Connection connection;

    /**
     * Returns server address.
     *
     * @return server address.
     */
    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Please, enter a server address.");
        return ConsoleHelper.readString();
    }

    /**
     * Returns server port.
     *
     * @return server port.
     */
    protected int getServerPort() {
        ConsoleHelper.writeMessage("Please, enter a port number.");
        return ConsoleHelper.readInt();
    }

    /**
     * Returns server name.
     *
     * @return server name.
     */
    protected String getUserName() {
        ConsoleHelper.writeMessage("Please, enter a user name.");
        return ConsoleHelper.readString();
    }

    /**
     * Returns true if the client should send text from the console.
     *
     * @return true if the client should send text from the console.
     */
    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    /**
     * Returns a new SocketThread object.
     *
     * @return a new SocketThread object.
     */
    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    /**
     * Sends a text message.
     *
     * @param text text to send.
     */
    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Error. Communications problem.");
            clientConnected = false;
        }
    }

    /**
     * Starts the client.
     */
    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Error.");
                return;
            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Connection is established. Type 'exit' for exit");
        } else {
            ConsoleHelper.writeMessage("An error with the client has occurred.");
        }
        while (clientConnected) {
            String text = ConsoleHelper.readString();
            if (text.equals("exit")) {
                break;
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(text);
            }
        }
    }

    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("User " + userName + " is connected.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("User " + userName + " is disconnected.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message receivedMessage = connection.receive();
                if (receivedMessage.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if (receivedMessage.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else {
                    throw new IOException("Unexpected MessageType.");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message receivedMessage = connection.receive();
                if (receivedMessage.getType() == MessageType.TEXT) {
                    processIncomingMessage(receivedMessage.getData());
                } else if (receivedMessage.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(receivedMessage.getData());
                } else if (receivedMessage.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(receivedMessage.getData());
                } else {
                    throw new IOException("Unexpected MessageType.");
                }
            }
        }

        @Override
        public void run() {
            try {
                String serverAddress = getServerAddress();
                int serverPort = getServerPort();
                Socket socket = new Socket(serverAddress, serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
