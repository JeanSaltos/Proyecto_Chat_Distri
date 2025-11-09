package com.chat.servidorchat.listener;

import com.chat.servidorchat.model.ChatMessage;
import com.chat.servidorchat.service.ChatRoomService; // (1) IMPORTAR
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    // (2) INYECTAR EL SERVICIO
    @Autowired
    private ChatRoomService chatRoomService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            logger.info("Usuario desconectado: {}", username);

            // (3) ELIMINAR AL USUARIO DEL SERVICIO DE ESTADO
            chatRoomService.removeUser(username);

            // (4) DIFUNDIR LA NUEVA LISTA DE USUARIOS
            messagingTemplate.convertAndSend("/topic/users", chatRoomService.getOnlineUsers());

            // (5) DIFUNDIR EL MENSAJE "LEAVE" (esto no cambia)
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setUsername(username);
            chatMessage.setTimestamp(TIME_FORMATTER.format(Instant.now()));

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}