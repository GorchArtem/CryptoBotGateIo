package com.adamgenesis.gateio.client;

import io.gate.gateapi.ApiException;
import io.gate.gateapi.models.Order;
import io.gate.gateapi.models.OrderBook;
import io.gate.gateapi.models.Trade;
import com.adamgenesis.gateio.client.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class Main {


    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        GateIoWebSocketClient webSocketClient = new GateIoWebSocketClient(
                "d1c51a9a1fd4b8db74f43cbc83d1b370", "cd83c514abfc3c3655c287849bd9006ba1b7b0ca71cee749763d5e4877440a21");

        GateIoService gateIoService = new GateIoService(
                "d1c51a9a1fd4b8db74f43cbc83d1b370", "cd83c514abfc3c3655c287849bd9006ba1b7b0ca71cee749763d5e4877440a21");

        try {
            gateIoService.placeMarketOrder("GT_USDT", Order.SideEnum.BUY, "3");
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
//
//        // Создаём пул потоков для WebSocket
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//
//        // Запускаем WebSocket в отдельном потоке
//        executorService.submit(webSocketClient::connect);
//
//        // Мониторинг состояния WebSocket (каждые 5 секунд)
//        scheduler.scheduleAtFixedRate(() -> {
//            ConnectionState state = webSocketClient.getConnectionState();
//            log.info("WebSocket state: {}", state);
//
//            if (state == ConnectionState.DISCONNECTED || state == ConnectionState.FAILED) {
//                log.warn("WebSocket отключён, пробуем переподключиться...");
//                executorService.submit(webSocketClient::connect);
//            }
//        }, 5, 5, TimeUnit.SECONDS);
//
//        // Добавляем shutdown hook для корректного завершения
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            log.info("Остановка WebSocket-клиента...");
//            scheduler.shutdown();
//            executorService.shutdown();
//            try {
//                if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
//                    executorService.shutdownNow();
//                }
//                if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
//                    scheduler.shutdownNow();
//                }
//            } catch (InterruptedException e) {
//                log.error("Ошибка при завершении потоков", e);
//                executorService.shutdownNow();
//                scheduler.shutdownNow();
//            }
//            log.info("WebSocket клиент успешно остановлен.");
//        }));






    }

}
