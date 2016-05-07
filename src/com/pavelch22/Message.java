package com.pavelch22;

import java.io.Serializable;

/**
 * This class represents a message.
 */
public class Message implements Serializable {
    private final MessageType type;
    private final String data;

    /**
     * Creates a new Message object with no data.
     */
    public Message(MessageType type) {
        this.type = type;
        this.data = null;
    }

    /**
     * Creates a new Message object.
     */
    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Returns message type.
     *
     * @return message type.
     */
    public MessageType getType() {
        return type;
    }


    /**
     * Returns message data.
     *
     * @return message data.
     */
    public String getData() {
        return data;
    }
}
