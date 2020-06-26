package com.shtrih.tinyjavapostester;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.network.OrderResponse;

public class Receipt {
    private OrderResponse.Order order;

    public Receipt(OrderResponse.Order order) {
        this.order = order;
    }

    public void print(ShtrihFiscalPrinter printer){

    }
}
