package com.chat.servidorchat.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio Singleton para mantener el estado de la sala de chat (usuarios conectados).
 * Es 'synchronized' para ser seguro en concurrencia (varios usuarios
 * uniéndose y saliendo al mismo tiempo).
 */
@Service
public class ChatRoomService {

    // Usamos un Set para evitar duplicados.
    // Lo envolvemos en un Set sincronizado para seguridad en hilos (concurrencia).
    private final Set<String> onlineUsers = Collections.synchronizedSet(new HashSet<>());

    public void addUser(String username) {
        onlineUsers.add(username);
    }

    public void removeUser(String username) {
        onlineUsers.remove(username);
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}