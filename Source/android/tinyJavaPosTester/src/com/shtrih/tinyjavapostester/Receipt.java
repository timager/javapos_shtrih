package com.shtrih.tinyjavapostester;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.SmFiscalPrinterException;
import com.shtrih.fiscalprinter.command.FSDocType;
import com.shtrih.fiscalprinter.command.FSStatusInfo;
import com.shtrih.fiscalprinter.command.LongPrinterStatus;
import com.shtrih.jpos.fiscalprinter.JposExceptionHandler;
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
        prepare(printer);
        final int fiscalReceiptType = FiscalPrinterConst.FPTR_RT_SALES;

        printer.setFiscalReceiptType(fiscalReceiptType);

        printer.beginFiscalReceipt(true);

        printer.fsWriteTag(1021, deepLinkData.getString("username"));

        final String unitName = "Оплата";


        for (OrderResponse.Serv serv : order.getServs()) {
            long price = Long.parseLong(serv.getServCost().replace(".", "")) / 100;
            printer.printRecItem(serv.getServCode() + " " + serv.getServName(), price, 0, 0, 0, unitName);
        }

        JSONObject operationData = deepLinkData.getJSONObject("operation_data");
        long paymentCash = operationData.getInt("payment_cash") * 100;
        if (paymentCash != 0) {
            printer.printRecTotal(paymentCash, paymentCash, "0");
        }
        long paymentCard = operationData.getInt("payment_card") * 100;
        if (paymentCard != 0) {
            printer.printRecTotal(paymentCard, paymentCard, "");
        }
        printer.endFiscalReceipt(true);
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
