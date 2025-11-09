'use strict';

// --- (1) Seleccionar elementos del DOM ---
const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#messageArea');
const logoutButton = document.querySelector('#logoutButton');
const userListElement = document.querySelector('#userList');

// --- (2) Variables Globales ---
let stompClient = null;
let username = null;

// Colores para los avatares (solo cosmético)
const colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

// --- (3) Funciones Principales ---

/**
 * Función CONNECT (Paso 1)
 * Se llama cuando el usuario envía el formulario de login.
 */
function connect(event) {
    // Evita que el formulario recargue la página
    event.preventDefault();

    // Obtiene el nombre de usuario y lo guarda (RNF6: validación simple)
    username = document.querySelector('#username').value.trim();

    if (username) {
        // Oculta la página de login y muestra la de chat
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        // (A) Crea la conexión WebSocket usando SockJS
        // Usamos la URL que definimos en WebSocketConfig.java
        const socket = new SockJS('/ws');

        // (B) Crea una instancia del cliente STOMP sobre el socket
        stompClient = Stomp.over(socket);

        // (C) Conecta el cliente STOMP al servidor
        // Llama a onConnected si tiene éxito, o a onError si falla
        stompClient.connect({}, onConnected, onError);
    }
}

/**
 * Función ON_CONNECTED (Paso 2)
 * Se llama cuando la conexión STOMP es exitosa.
 */
function onConnected() {
    // (A) Suscribirse al Topic Público (para mensajes)
    stompClient.subscribe('/topic/public', onMessageReceived);

    // (B) --- NUEVO: Suscribirse al Topic de Usuarios ---
    // Recibiremos la lista completa de usuarios aquí
    stompClient.subscribe('/topic/users', onUserListReceived);

    // (C) Enviar un mensaje de "JOIN" al servidor (sin cambios)
    const joinMessage = {
        username: username,
        type: 'JOIN'
    };
    stompClient.send("/app/chat.addUser", {}, JSON.stringify(joinMessage));

    console.log("Conectado y suscrito a /topic/public y /topic/users!");
}

/**
 * Función ON_ERROR
 * Se llama si la conexión STOMP falla.
 */
function onError(error) {
    console.error('No se pudo conectar a WebSocket. Error: ' + error);
    // (Opcional: mostrar un mensaje de error al usuario)
}

/**
 * Función SEND_MESSAGE (Paso 3)
 * Se llama cuando el usuario envía el formulario de chat.
 */
function sendMessage(event) {
    event.preventDefault(); // Evita recargar la página

    const messageContent = messageInput.value.trim();

    // Si el mensaje no está vacío y el cliente STOMP está conectado
    if (messageContent && stompClient) {
        // (A) Crea el objeto de mensaje (RF5)
        const chatMessage = {
            username: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        // (B) Envía el mensaje al servidor (RF2)
        // El servidor lo recibirá en @MessageMapping("/chat.sendMessage")
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));

        // (C) Limpia el campo de texto
        messageInput.value = '';
    }
}

function onUserListReceived(payload) {
    // Parsea la lista de usuarios (viene como un array de strings)
    const users = JSON.parse(payload.body);

    // Limpia la lista actual en el HTML
    while (userListElement.firstChild) {
        userListElement.removeChild(userListElement.firstChild);
    }

    // Vuelve a llenar la lista con los usuarios recibidos
    users.forEach(user => {
        const userElement = document.createElement('li');
        userElement.textContent = user;
        userListElement.appendChild(userElement);
    });
}

/**
 * Función ON_MESSAGE_RECEIVED (Paso 4)
 * Se llama cada vez que recibimos un mensaje del topic "/topic/public".
 */
function onMessageReceived(payload) {
    // Parsea el mensaje JSON que viene del servidor
    const message = JSON.parse(payload.body);

    // Crea el elemento <li> que se mostrará en el chat
    const messageElement = document.createElement('li');

    // (A) Si es un mensaje de evento (JOIN o LEAVE) (RF4)
    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        messageElement.classList.add('event-message');

        let eventText = message.username;
        if (message.type === 'JOIN') {
            eventText += ' se ha unido al chat!';
        } else {
            eventText += ' se ha desconectado.';
        }
        messageElement.textContent = eventText;

        // (B) Si es un mensaje de CHAT normal (RF3, RF5)
    } else {
        messageElement.classList.add('chat-message');

        // --- Avatar (cosmético) ---
        const avatarElement = document.createElement('span');
        const avatarText = document.createTextNode(message.username[0]); // Primera letra
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.username);
        messageElement.appendChild(avatarElement);
        // --- Fin Avatar ---

        // --- Contenedor del Mensaje ---
        const bubbleElement = document.createElement('div');

        // Cabecera: Usuario y Hora
        const headerElement = document.createElement('span');
        headerElement.className = 'sender';
        headerElement.textContent = message.username;

        const timestampElement = document.createElement('span');
        timestampElement.className = 'timestamp';
        timestampElement.textContent = message.timestamp;

        headerElement.appendChild(timestampElement);
        bubbleElement.appendChild(headerElement);

        // Contenido del mensaje
        const contentElement = document.createElement('p');
        contentElement.textContent = message.content;
        bubbleElement.appendChild(contentElement);

        messageElement.appendChild(bubbleElement);
        // --- Fin Contenedor ---
    }

    // (C) Añadir el mensaje al área de chat
    messageArea.appendChild(messageElement);

    // (D) Hacer scroll hasta el fondo (RNF4 Usabilidad)
    messageArea.scrollTop = messageArea.scrollHeight;
}

/**
 * Función DISCONNECT (RF6)
 * Se llama al pulsar el botón "Salir".
 */
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    // Vuelve a la página de login
    chatPage.classList.add('hidden');
    usernamePage.classList.remove('hidden');

    // Limpia el área de chat
    while (messageArea.firstChild) {
        messageArea.removeChild(messageArea.firstChild);
    }
    console.log("Desconectado.");
}

/**
 * Función auxiliar para obtener un color basado en el hash del nombre.
 */
function getAvatarColor(messageSender) {
    let hash = 0;
    for (let i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    const index = Math.abs(hash % colors.length);
    return colors[index];
}


// --- (4) Event Listeners ---
// Asigna las funciones a los eventos de los formularios
usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
logoutButton.addEventListener('click', disconnect, true);