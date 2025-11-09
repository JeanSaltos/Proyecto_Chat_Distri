package com.chat.servidorchat.controller;

import com.chat.servidorchat.model.ChatMessage;
import com.chat.servidorchat.service.ChatRoomService; // (1) IMPORTAR
import org.springframework.beans.factory.annotation.Autowired; // (2) IMPORTAR
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations; // (3) IMPORTAR
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
public class ChatController {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    // (4) INYECTAR EL SERVICIO Y EL TEMPLATE
    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;


    /**
     * Maneja los mensajes de chat (ESTE MÉTODO NO CAMBIA)
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(TIME_FORMATTER.format(Instant.now()));
        return chatMessage;
    }

    /**
     * Maneja la unión de un nuevo usuario (ESTE MÉTODO SÍ CAMBIA)
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public") // (A) Todavía envía el mensaje "JOIN" a /topic/public
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {

        String username = chatMessage.getUsername();
        if (username != null) {
            // Guardamos el usuario en la sesión de WebSocket
            headerAccessor.getSessionAttributes().put("username", username);

            // (B) Añadimos al usuario a nuestro servicio de estado
            chatRoomService.addUser(username);

            // (C) Difundimos LA LISTA COMPLETA al nuevo topic /topic/users
            messagingTemplate.convertAndSend("/topic/users", chatRoomService.getOnlineUsers());
        }

        // Asignamos el timestamp y tipo (esto no cambia)
        chatMessage.setTimestamp(TIME_FORMATTER.format(Instant.now()));
        chatMessage.setType(ChatMessage.MessageType.JOIN);

        // (D) Retornamos el mensaje JOIN para /topic/public
        return chatMessage;
    }
}