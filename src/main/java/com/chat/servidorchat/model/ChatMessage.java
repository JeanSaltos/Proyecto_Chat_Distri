package com.chat.servidorchat.model;

/**
 * Representa el mensaje de chat que se envía.
 * RF5: Incluye nombre de usuario, contenido y hora.
 */
public class ChatMessage {

    private String content;
    private String username;
    private String timestamp;
    private MessageType type; // Para saber si es un JOIN, LEAVE o CHAT

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    // --- Getters y Setters ---
    // (Puedes generarlos automáticamente con tu IDE)

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}