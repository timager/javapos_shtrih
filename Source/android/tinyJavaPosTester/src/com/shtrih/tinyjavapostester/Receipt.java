package com.shtrih.tinyjavapostester;

import android.util.Pair;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.SmFiscalPrinterException;
import com.shtrih.fiscalprinter.command.FSDocType;
import com.shtrih.fiscalprinter.command.FSStatusInfo;
import com.shtrih.fiscalprinter.command.LongPrinterStatus;
import com.shtrih.jpos.fiscalprinter.JposExceptionHandler;
import com.shtrih.jpos.fiscalprinter.SmFptrConst;
import com.shtrih.tinyjavapostester.network.OrderResponse;
import com.shtrih.tinyjavapostester.util.AppUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jpos.FiscalPrinterConst;
import jpos.JposException;

public class Receipt {
    private final String UNIT_NAME_SALE = "Оплата";
    private final String UNIT_NAME_REFUND = "Возврат";
    private final String CONST_WORD_PAYMENT = "ПЛАТЕЖ";

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

        if (AppUtil.isFullSale(deepLinkData, order)) {
            printFullSales(printer);
        } else if (AppUtil.isPartitionSale(deepLinkData, order)) {
            printPartitionSales(printer);
        } else if (AppUtil.isRefundService(deepLinkData)) {
            printRefundService(printer);
        } else if (AppUtil.isRefundTransaction(deepLinkData)) {
            printRefundTransaction(printer);
        } else if (AppUtil.isRefundByReason(deepLinkData)) {
            printRefundByReason(printer);
        }
    }

    private void printFullSales(ShtrihFiscalPrinter printer) throws Exception {
        printer.setFiscalReceiptType(FiscalPrinterConst.FPTR_RT_SALES);
        printer.beginFiscalReceipt(true);
        writeTags(printer);
        printFullSaleItems(printer, order.getServs(), UNIT_NAME_SALE);
        printDiscount(printer);
        printFullSubTotal(printer);
        printFullSaleTotal(printer);
        printer.endFiscalReceipt(false);
    }

    private void printPartitionSales(ShtrihFiscalPrinter printer) throws Exception {
        double deepLinkSum = AppUtil.getSumSalePaymentFromDeepLink(deepLinkData);

        printer.setFiscalReceiptType(FiscalPrinterConst.FPTR_RT_SALES);
        printer.beginFiscalReceipt(true);
        writeTags(printer);
        printPartitionSaleItems(printer, deepLinkSum, order.getServs(), UNIT_NAME_SALE);
        printDiscount(printer);
        printPartitionSaleSubTotal(printer);
        printPartitionSaleTotal(printer);
        printer.endFiscalReceipt(false);
    }

    private void printRefundService(ShtrihFiscalPrinter printer) throws Exception {
        int fiscalReceiptType = deepLinkData.getJSONObject("operation_data").getInt("fiscal");

        List<Integer> refundServiceIdList = AppUtil.convertToListInteger(deepLinkData.getJSONObject("operation_data").getJSONArray("servs"));
        List<OrderResponse.Serv> refundServiceList = new ArrayList<>();
        for (OrderResponse.Serv serv : order.getServs()) {
            if (refundServiceIdList.contains(serv.getServId())) {
                refundServiceList.add(serv);
            }
        }

        printer.setParameter(SmFptrConst.SMFPTR_DIO_PARAM_ITEM_PAYMENT_TYPE, fiscalReceiptType);
        printer.setFiscalReceiptType(FiscalPrinterConst.FPTR_RT_REFUND);
        printer.beginFiscalReceipt(true);
        writeTags(printer);
        printRefundItems(printer, refundServiceList, UNIT_NAME_REFUND, fiscalReceiptType);
        printRefundSubTotal(printer, fiscalReceiptType);
        printRefundTotal(printer, refundServiceList);
        printer.endFiscalReceipt(false);
    }

    private void printRefundTransaction(ShtrihFiscalPrinter printer) throws Exception {
        int fiscalReceiptType = deepLinkData.getJSONObject("operation_data").getInt("fiscal");

        double sumRefund = AppUtil.getSumRefundTransactionFromData(deepLinkData, order);

        printer.setParameter(SmFptrConst.SMFPTR_DIO_PARAM_ITEM_PAYMENT_TYPE, fiscalReceiptType);
        printer.setFiscalReceiptType(FiscalPrinterConst.FPTR_RT_REFUND);
        printer.beginFiscalReceipt(true);
        writeTags(printer);
        printRefundItemsByTransaction(printer, sumRefund, UNIT_NAME_REFUND, fiscalReceiptType);
        printRefundSubTotal(printer, fiscalReceiptType);
        printRefundTotalByTransaction(printer, sumRefund);
        printer.endFiscalReceipt(false);
    }

    private void printRefundByReason(ShtrihFiscalPrinter printer) throws Exception {
        int fiscalReceiptType = deepLinkData.getJSONObject("operation_data").getInt("fiscal");

        printer.setParameter(SmFptrConst.SMFPTR_DIO_PARAM_ITEM_PAYMENT_TYPE, fiscalReceiptType);
        printer.setFiscalReceiptType(FiscalPrinterConst.FPTR_RT_REFUND);
        printer.beginFiscalReceipt(true);
        writeTags(printer);
        printRefundTotalByReason(printer);
        printer.endFiscalReceipt(false);
    }

    private void writeTags(ShtrihFiscalPrinter printer) throws Exception {
        printer.fsWriteTag(1021, deepLinkData.getString("username"));
    }

    private void printFullSaleItems(ShtrihFiscalPrinter printer, List<OrderResponse.Serv> items, String unitName) throws JposException {
        for (OrderResponse.Serv serv : items) {
            long price = Long.parseLong(serv.getServCost().replace(".", "")) / 100;
            long priceDiscount = Long.parseLong(serv.getServCostD().replace(".", "")) / 100;
            long discount = price - priceDiscount;
            int taxType = serv.getServTax();
            printer.printRecItem(serv.getServCode() + " " + serv.getServName(), price, 0, taxType, 0, unitName);
            printer.printRecItemAdjustment(FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT, "", discount, taxType);
            printer.printRecMessage(makeSpacesFormatString(getTextPaymentType(1), CONST_WORD_PAYMENT));
            printer.printRecMessage("------------");
        }
    }

    private void printPartitionSaleItems(ShtrihFiscalPrinter printer, double partitionSum, List<OrderResponse.Serv> items, String unitName) throws JposException {
        // Коэфициент между суммой возврата и общей суммой
        double sumRate = partitionSum / order.getOrderAmount();

        List<Double> printPriceList = AppUtil.getListServicePaymentByPartition(items, order.getOrderAmountWithBenefits(), partitionSum);
        for (int i = 0; i < items.size(); i++) {
            OrderResponse.Serv serv = items.get(i);
            Double printPrice = printPriceList.get(i);
            long printPricePenny = (long) (printPrice * 100);

            long pricePenny = (long) (Double.parseDouble(serv.getServCost()) * 100);
            long priceWithDiscountPenny = (long) (Double.parseDouble(serv.getServCostD()) * 100);
            long discountPenny = pricePenny - priceWithDiscountPenny;
            int taxType = serv.getServTax();

            printer.printRecItem(serv.getServCode() + " " + serv.getServName(), printPricePenny, 0, taxType, 0, unitName);
            printer.printRecItemAdjustment(FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT, "", discountPenny, taxType);
            printer.printRecMessage(makeSpacesFormatString(getTextPaymentType(2), CONST_WORD_PAYMENT));
            printer.printRecMessage("------------");
        }
    }

    private void printRefundItems(ShtrihFiscalPrinter printer, List<OrderResponse.Serv> items, String unitName, int fiscalReceiptType) throws Exception {
        for (OrderResponse.Serv serv : items) {
            long price = Long.parseLong(serv.getServCost().replace(".", "")) / 100;
            long priceDiscount = Long.parseLong(serv.getServCostD().replace(".", "")) / 100;
            long discount = price - priceDiscount;
            int taxType = serv.getServTax();
            printer.printRecItemRefund(serv.getServCode() + " " + serv.getServName(), price, 0, taxType, 0, unitName);
            printer.printRecItemAdjustment(FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT, "", discount, taxType);
            printPaymentType(printer, fiscalReceiptType);
            printer.printRecMessage("------------");
        }
    }

    private void printRefundItemsByTransaction(ShtrihFiscalPrinter printer, double sumRefund, String unitName, int fiscalReceiptType) throws Exception {
        List<OrderResponse.Serv> servs = order.getServs();
        List<Pair<Long, Long>> printPriceList = AppUtil.getListPricePennyWithDiscountPennyByPartition( order, ((long)sumRefund * 100));

        for (int i = 0; i < servs.size(); i++) {
            OrderResponse.Serv serv = servs.get(i);
            Pair<Long, Long> printPricePennyWithDiscount = printPriceList.get(i);
            int taxType = serv.getServTax();

            printer.printRecItemRefund(serv.getServCode() + " " + serv.getServName(), printPricePennyWithDiscount.first, 0, taxType, 0, unitName);
            printer.printRecItemAdjustment(FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT, "", printPricePennyWithDiscount.second, taxType);
            printPaymentType(printer, fiscalReceiptType);
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

    private void printFullSubTotal(ShtrihFiscalPrinter printer) throws JposException {
        long orderSum = order.getOrderAmount(); //не уверен
        long orderSumDiscount = order.getOrderAmountWithBenefits();
        printer.printRecMessage(makeSpacesFormatString("СУММА ЗАКАЗА", "=" + orderSum));
        printer.printRecMessage(makeSpacesFormatString("СУММА С УЧЕТОМ СКИДКИ", "=" + orderSumDiscount));
        printer.printRecMessage(makeSpacesFormatString(getTextPaymentType(1), "=" + orderSumDiscount));
    }

    private void printPartitionSaleSubTotal(ShtrihFiscalPrinter printer) throws JposException, JSONException {
        double sendOrderSum = AppUtil.getSumSalePaymentFromDeepLink(deepLinkData);
        long orderSum = order.getOrderAmount(); //не уверен
        long orderSumDiscount = order.getOrderAmountWithBenefits();
        printer.printRecMessage(makeSpacesFormatString("СУММА ЗАКАЗА", "=" + orderSum));
        printer.printRecMessage(makeSpacesFormatString("СУММА С УЧЕТОМ СКИДКИ", "=" + orderSumDiscount));
        printer.printRecMessage(makeSpacesFormatString(getTextPaymentType(2), "=" + sendOrderSum));
    }

    private void printRefundSubTotal(ShtrihFiscalPrinter printer, Integer paymentType) throws JposException {
        long orderSum = order.getOrderAmount(); //не уверен
        long orderSumDiscount = order.getOrderAmountWithBenefits();
        printer.printRecMessage(makeSpacesFormatString("СУММА ЗАКАЗА", "=" + orderSum));
        printer.printRecMessage(makeSpacesFormatString("СУММА С УЧЕТОМ СКИДКИ", "=" + orderSumDiscount));
        printer.printRecMessage(makeSpacesFormatString(getTextPaymentType(paymentType), "=" + orderSum));
    }

    private void printFullSaleTotal(ShtrihFiscalPrinter printer) throws Exception {
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

    private void printPartitionSaleTotal(ShtrihFiscalPrinter printer) throws Exception {
        JSONObject operationData = deepLinkData.getJSONObject("operation_data");
        long orderSum = order.getOrderAmountWithBenefits() * 100;

        long paymentCash = operationData.getInt("payment_cash") * 100;
        if (paymentCash != 0) {
            printer.printRecTotal(orderSum, paymentCash, "0");
        }
        long paymentCard = operationData.getInt("payment_card") * 100;
        if (paymentCard != 0) {
            printer.printRecTotal(orderSum, paymentCard, "2");
        }
    }

    private void printRefundTotal(ShtrihFiscalPrinter printer, List<OrderResponse.Serv> refundServiceList) throws Exception {
        int paymentType = deepLinkData.getJSONObject("operation_data").getInt("pay_type");
        double sumPayment = 0;

        for (OrderResponse.Serv serv : refundServiceList) {
            sumPayment += Double.parseDouble(serv.getServCostD());
        }

        // Терминал принимает сумму в копейках
        long sumPennyPayment = (long) (sumPayment * 100);

        if (paymentType == 1) {
            printer.printRecTotal(sumPennyPayment, sumPennyPayment, "0");
        } else if (paymentType == 2) {
            printer.printRecTotal(sumPennyPayment, sumPennyPayment, "2");
        }
    }

    private void printRefundTotalByTransaction(ShtrihFiscalPrinter printer, double sumRefund) throws Exception {
        int paymentType = 1;

        // Терминал принимает сумму в копейках
        long sumPennyPayment = (long) (sumRefund * 100);

        if (paymentType == 1) {
            printer.printRecTotal(sumPennyPayment, sumPennyPayment, "0");
        } else if (paymentType == 2) {
            printer.printRecTotal(sumPennyPayment, sumPennyPayment, "2");
        }
    }

    private void printRefundTotalByReason(ShtrihFiscalPrinter printer) throws JSONException, JposException {
        String reason = deepLinkData.getJSONObject("operation_data").getString("claim");
        long sumPennyPayment = (long) (deepLinkData.getJSONObject("operation_data").getDouble("sum") * 100);

        printer.printRecRefund(reason, sumPennyPayment, 0);
        printer.printRecTotal(sumPennyPayment, sumPennyPayment, "0");
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

    private void printPaymentType(ShtrihFiscalPrinter printer, Integer paymentType) throws JposException {
        String text = getTextPaymentType(paymentType);
        if (text != null) {
            printer.printRecMessage(text);
        }
    }

    private String getTextPaymentType(Integer paymentType) {
        switch (paymentType) {
            case 1:
                return "ПРЕДОПЛАТА 100%";
            case 2:
                return "ЧАСТИЧНАЯ ПРЕДОПЛАТА";
            case 4:
                return "ПОЛНЫЙ РАСЧЕТ";
            case 5:
                return "ЧАСТИЧНЫЙ РАСЧЕТ";
        }

        return null;
    }


}
