package com.shtrih.tinyjavapostester;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.SmFiscalPrinterException;
import com.shtrih.fiscalprinter.command.FSDocType;
import com.shtrih.fiscalprinter.command.FSStatusInfo;
import com.shtrih.fiscalprinter.command.LongPrinterStatus;
import com.shtrih.jpos.fiscalprinter.JposExceptionHandler;
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

    private void printReceiptHeader(ShtrihFiscalPrinter printer) throws JposException {
        printer.setNumHeaderLines(5);
        printer.setHeaderLine(1, "* " + "ОАО Тестовая клиника", false);
        printer.setHeaderLine(2, "* " + "\"Колебн и Тим\"", false);
        printer.setHeaderLine(3, "* " + "г. Тестов ул. Тестов д.228", false);
        printer.setHeaderLine(4, "Заказ № " + order.getOrderNumber(), false);
        printer.setHeaderLine(5, "----------------------------", false);
    }

    public void print(ShtrihFiscalPrinter printer) throws Exception {
        prepare(printer);
        printer.setFontNumber(1);
        printReceiptHeader(printer);
        printer.setFiscalReceiptType(FiscalPrinterConst.FPTR_RT_SALES);
        printer.beginFiscalReceipt(true);
        writeTags(printer);
        printItems(printer);
        printDiscount(printer);
        printSubTotal(printer);
        printTotal(printer);
        printer.endFiscalReceipt(false);
    }

    private void writeTags(ShtrihFiscalPrinter printer) throws Exception {
        printer.fsWriteTag(1021, deepLinkData.getString("username"));
    }

    private void printItems(ShtrihFiscalPrinter printer) throws JposException {
        final String unitName = "Оплата";

        for (OrderResponse.Serv serv : order.getServs()) {
            long price = Long.parseLong(serv.getServCost().replace(".", "")) / 100;
            long priceDiscount = Long.parseLong(serv.getServCostD().replace(".", "")) / 100;
            long discount = price - priceDiscount;
            int taxType = serv.getServTax();
            printer.printRecItem(serv.getServCode() + " " + serv.getServName(), price, 0, taxType, 0, unitName);
            printer.printRecItemAdjustment(FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT, "", discount, taxType);
            printer.printRecMessage("------------");
        }
    }


    private void printDiscount(ShtrihFiscalPrinter printer) throws JposException {
        if (order.getOrderDiscountPercent() > 0) {
            long discount = order.getOrderDiscount();
            long discountPercent = order.getOrderDiscountPercent();
            long cardBalance = order.getOrderDiscountBallance();
            printer.printRecMessage("********************************");
            printer.printRecMessage("********************************");
            printer.printRecMessage("ОСНОВАНИЕ СКИДКИ:");
            printer.printRecMessage("   " + order.getDocName());
            printer.printRecMessage(makeSpacesFormatString("СУММА СКИДКИ", "=" + discount));
            printer.printRecMessage(makeSpacesFormatString("ПРОЦЕНТ СКИДКИ", "=" + discountPercent + "%"));
            printer.printRecMessage(makeSpacesFormatString("БАЛАНС КАРТЫ", "=" + cardBalance));
            printer.printRecMessage("********************************");
            printer.printRecMessage("********************************");
        }
    }

    private String makeSpacesFormatString(String text1, String text2) {
        int len = "********************************".length() - text1.length() - text2.length();
        StringBuilder str = new StringBuilder(text1);
        for (int i = 0; i < len; i++) {
            str.append(" ");
        }
        str.append(text2);
        return str.toString();
    }

    private void printSubTotal(ShtrihFiscalPrinter printer) throws JposException {
        long orderSum = order.getOrderAmount(); //не уверен
        long orderSumDiscount = order.getOrderAmountWithBenefits();
        printer.printRecMessage(makeSpacesFormatString("СУММА ЗАКАЗА", "=" + orderSum));
        printer.printRecMessage(makeSpacesFormatString("СУММА С УЧЕТОМ СКИДКИ", "=" + orderSumDiscount));
    }

    private void printTotal(ShtrihFiscalPrinter printer) throws Exception {
        JSONObject operationData = deepLinkData.getJSONObject("operation_data");
        long paymentCash = operationData.getInt("payment_cash") * 100;
        if (paymentCash != 0) {
            printer.printRecTotal(paymentCash, paymentCash, "0");
        }
        long paymentCard = operationData.getInt("payment_card") * 100;
        if (paymentCard != 0) {
            printer.printRecTotal(paymentCard, paymentCard, "2");
        }
    }

    private void prepare(ShtrihFiscalPrinter printer) throws JposException {
        LongPrinterStatus status = printer.readLongPrinterStatus();

        // проверяем наличие бумаги
        if (status.getSubmode() == 1 || status.getSubmode() == 2) {
            final int errorCodeNoPaper = 107;
            throw JposExceptionHandler.getJposException(
                    new SmFiscalPrinterException(errorCodeNoPaper, "Отсутствует бумага"));
        }

        // проверяем, есть ли открытый документ в ФН
        FSStatusInfo fsStatus = printer.fsReadStatus();

        if (fsStatus.getDocType().getValue() != FSDocType.FS_DOCTYPE_NONE)
            printer.fsCancelDocument(); // если есть отменяем

        printer.resetPrinter();
    }
}
