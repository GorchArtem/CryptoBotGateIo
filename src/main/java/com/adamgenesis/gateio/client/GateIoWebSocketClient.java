package com.adamgenesis.gateio.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@ClientEndpoint
public class GateIoWebSocketClient {


    private static final Logger log = LoggerFactory.getLogger(GateIoWebSocketClient.class);
    private static final String SERVER_URI = "wss://api.gateio.ws/ws/v4/";
    private static final int RECONNECT_SECONDS = 5;
    private final String apiKey;
    private final String secretKey = "cd83c514abfc3c3655c287849bd9006ba1b7b0ca71cee749763d5e4877440a21";
    private static final String SERVER_TIME_URL = "https://api.gateio.ws/api/v4/spot/time";
    private final SignatureService signatureService;

    private WebSocketContainer container;
    private Session session;

    private final AtomicReference<ConnectionState> connectionState = new AtomicReference<>(ConnectionState.DISCONNECTED);

    public GateIoWebSocketClient(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.signatureService = new SignatureService(secretKey);
    }
    private void subscribeToChannels() {

        long timestamp = getServerTime() / 1000;
        System.out.println(timestamp);
        String signature = signatureService.generateSignature("spot.balances", "subscribe", timestamp);
        System.out.println(signature);

        String spotBalanceSubscription = """
            {
              "time": %d,
              "channel": "spot.balances",
              "event": "subscribe",
              "auth": {
                "method": "api_key",
                "KEY": "%s",
                "SIGN": "%s"
              }
            }
            """.formatted(timestamp, apiKey, signature);

        String spotOrderBook = """
                {
                  "channel": "spot.order_book",
                  "event": "subscribe",
                  "payload": ["SOL_USDT", "100", "100ms"]
                }
                """;

        String authMessage = """
            {
              "channel": "spot.balances",
              "event": "auth",
              "payload": ["%s", "%s"]
            }
            """.formatted(apiKey, signature);

        sendMessage(spotBalanceSubscription);
    }

    public void connect() {
        try {
            log.info("Подключение к WebSocket...");
            connectionState.set(ConnectionState.CONNECTING);

            container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, new URI(SERVER_URI));
            session.setMaxIdleTimeout(TimeUnit.MINUTES.toMillis(60));

            if (session.isOpen()) {
                connectionState.set(ConnectionState.CONNECTED);
                log.info("WebSocket успешно подключен");
                subscribeToChannels();
            } else {
                connectionState.set(ConnectionState.FAILED);
                log.error("Ошибка подключения");
            }
        } catch (URISyntaxException | IOException | DeploymentException e) {
            connectionState.set(ConnectionState.FAILED);
            log.error("Ошибка WebSocket соединения", e);
            reconnect();
        }
    }

    private void reconnect() {
        if (connectionState.get() == ConnectionState.CONNECTED) {
            connectionState.set(ConnectionState.DISCONNECTED);
            log.warn("Соединение потеряно. Переподключение через {} секунд...", RECONNECT_SECONDS);
        }

        try {
            TimeUnit.SECONDS.sleep(RECONNECT_SECONDS);
            connect();
        } catch (InterruptedException e) {
            log.error("Ошибка при переподключении", e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        connectionState.set(ConnectionState.CONNECTED);
        log.info("Соединение с WebSocket установлено");
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Получено сообщение: {}", message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        connectionState.set(ConnectionState.DISCONNECTED);
        log.warn("WebSocket закрыт: {}", reason);
        reconnect();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        connectionState.set(ConnectionState.FAILED);
        log.error("Ошибка в WebSocket", throwable);
        reconnect();
    }

    public void sendMessage(String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            } else {
                log.warn("Невозможно отправить сообщение, WebSocket не подключен.");
            }
        } catch (IOException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    public ConnectionState getConnectionState() {
        return connectionState.get();
    }

    private long getServerTime() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.gateio.ws/api/v4/spot/time"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = new ObjectMapper().readTree(response.body());
            return jsonNode.get("server_time").asLong();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении серверного времени", e);
        }
    }

}
