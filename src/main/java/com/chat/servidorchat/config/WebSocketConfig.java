package com.chat.servidorchat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Habilita nuestro servidor como un "Message Broker"
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Registra el endpoint STOMP.
     * Esta es la "puerta de enlace" (URL) que los clientes usarán para conectarse
     * a nuestro servidor WebSocket.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para la conexión WebSocket.
        // "/ws" será la URL: http://localhost:8080/ws
        // .withSockJS() proporciona un fallback para navegadores
        // que no soporten WebSocket (RNF4 Usabilidad).
        registry.addEndpoint("/ws").withSockJS();
    }

    /**
     * Configura el "Broker de Mensajes".
     * Aquí definimos los "canales" o "topics" que usará la aplicación.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 1. Prefijo para los destinos de los mensajes (Topics)
        // El servidor enviará mensajes a los clientes suscritos a destinos
        // que empiecen con "/topic".
        // Ejemplo: "/topic/public" (El chat público)
        registry.enableSimpleBroker("/topic");

        // 2. Prefijo para los mensajes ENTRANTES
        // Los clientes enviarán mensajes a destinos que empiecen con "/app".
        // Estos mensajes se dirigirán a métodos en nuestro @Controller.
        // Ejemplo: "/app/chat.sendMessage"
        registry.setApplicationDestinationPrefixes("/app");
    }
}