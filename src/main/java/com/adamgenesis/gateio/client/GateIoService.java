package com.adamgenesis.gateio.client;

import io.gate.gateapi.ApiClient;
import io.gate.gateapi.ApiException;
import io.gate.gateapi.api.SpotApi;
import io.gate.gateapi.models.Order;
import io.gate.gateapi.models.OrderBook;

import io.gate.gateapi.models.Trade;

import java.util.List;

public class GateIoService {

    private final SpotApi spotApi;
    private final SignatureService signatureService;


    public GateIoService(String apiKey, String secretKey) {
        ApiClient client = new ApiClient();
        client.setApiKeySecret(apiKey, secretKey);
        this.signatureService = new SignatureService(secretKey);
        this.spotApi = new SpotApi(client);
    }

    public List<Trade> getRecentTrades(String currencyPair) throws ApiException {
        return spotApi.listTrades(currencyPair).execute();
    }

    public OrderBook getOrderBook(String currencyPair) throws ApiException {
        return spotApi.listOrderBook(currencyPair).execute();
    }

    public Order placeMarketOrder(String currencyPair, Order.SideEnum side, String amountUsdt) throws ApiException {
        Order order = new Order()
                .currencyPair(currencyPair)
                .side(side)
                .type(Order.TypeEnum.MARKET)
                .amount(amountUsdt);

        order.setTimeInForce(Order.TimeInForceEnum.IOC);

        return spotApi.createOrder(order);
    }




}
