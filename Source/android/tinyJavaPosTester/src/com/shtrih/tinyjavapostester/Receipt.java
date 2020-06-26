package com.shtrih.tinyjavapostester;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.jpos.fiscalprinter.SmFptrConst;
import com.shtrih.tinyjavapostester.network.OrderResponse;

import org.json.JSONObject;

import jpos.FiscalPrinterConst;
import jpos.JposException;

public class Receipt {
    private OrderResponse.Order order;
    private JSONObject deepLinkData;

    public Receipt(OrderResponse.Order order, JSONObject deepLinkData) {
        this.order = order;
        this.deepLinkData = deepLinkData;
    }

    public void print(ShtrihFiscalPrinter printer) throws Exception {
        printer.setFiscalReceiptType(FiscalPrinterConst.FPTR_S_RECEIPT);
        printer.beginFiscalReceipt(true);

        printer.fsWriteTag(1031, deepLinkData.getJSONObject("operation_data").getString("payment_cash"));
        printer.fsWriteTag(1081, deepLinkData.getJSONObject("operation_data").getString("payment_card"));

//        printer.fsWriteTag(1073, "+78001000000");
//        printer.fsWriteTag(1005, "НОВОСИБИРСК,КИРОВА,86");
//        printer.fsWriteTag(1075, "+73833358088");
//        printer.fsWriteTag(1171, "+73833399242");
//        printer.fsWriteTag(1044, "Прием денежных средств");
//        printer.fsWriteTag(1026, "РНКО \"ПЛАТЕЖНЫЙ ЦЕНТР\"");

        printer.printNormal(FiscalPrinterConst.FPTR_S_RECEIPT, "receipt.getReceipt()");

        final String unitName = "Оплата";

        printer.setParameter(SmFptrConst.SMFPTR_DIO_PARAM_ITEM_PAYMENT_TYPE, 4);
        printer.setParameter(SmFptrConst.SMFPTR_DIO_PARAM_ITEM_SUBJECT_TYPE, 4);

        printer.printRecItem("Приём платежа", 12300, 0, 0, 0, unitName);

        printer.setParameter(SmFptrConst.SMFPTR_DIO_PARAM_ITEM_PAYMENT_TYPE, 4);
        printer.setParameter(SmFptrConst.SMFPTR_DIO_PARAM_ITEM_SUBJECT_TYPE, 4);

        printer.printRecItem("Размер вознаграждения", 123, 0, 3, 0, unitName);

        printer.printRecTotal(12423, 12423, "0");
        printer.endFiscalReceipt(false);
    }
}
