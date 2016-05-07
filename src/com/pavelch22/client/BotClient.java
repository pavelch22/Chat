package com.pavelch22.client;

import com.pavelch22.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Bot that answers simple questions.
 */
public class BotClient extends Client {

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        ConsoleHelper.writeMessage("Please, enter a bot name.");
        return ConsoleHelper.readString();
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hello chat. I am not a human but I am smart enough to understand next commands: /date; /joke");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            try {
                ConsoleHelper.writeMessage(message);
                String name = message.split(": ")[0];
                String text = message.split(": ")[1];
                switch (text) {
                    case "/date":
                        SimpleDateFormat dateFormat = new SimpleDateFormat("d.MM.YYYY");
                        sendTextMessage("Information for " + name + ": " + dateFormat.format(Calendar.getInstance().getTime()));
                        break;
                    case "/joke":
                        sendTextMessage("Funny joke...");
                        break;
                }
            } catch (Exception e) {

            }
        }
    }

    public static void main(String[] args) {
        BotClient bot = new BotClient();
        bot.run();
    }
}
