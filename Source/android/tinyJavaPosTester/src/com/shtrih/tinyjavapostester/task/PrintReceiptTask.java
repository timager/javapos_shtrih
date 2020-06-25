package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.command.TextDocumentFilter;
import com.shtrih.jpos.fiscalprinter.SmFptrConst;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.activity.MainActivity;
import com.shtrih.tinyjavapostester.task.message.Message;

import jpos.FiscalPrinterConst;

public class PrintReceiptTask extends AbstractTask {


    public PrintReceiptTask(MainActivity parent, MainViewModel model) {
        super(parent, model);
    }

    private void printSalesReceipt(ShtrihFiscalPrinter printer) throws Exception {

        final int fiscalReceiptType = FiscalPrinterConst.FPTR_RT_SALES;

        printer.setFiscalReceiptType(fiscalReceiptType);

        printer.beginFiscalReceipt(true);

//        writePaymentTags(receipt.getTags());
        //Развернул сожержимое метода, чтобы было понятно, какие теги передаём
        printer.fsWriteTag(1016, "2225031594  ");
        printer.fsWriteTag(1073, "+78001000000");
        //printer.fsWriteTag(1057, "1");
        printer.fsWriteTag(1005, "НОВОСИБИРСК,КИРОВА,86");
        printer.fsWriteTag(1075, "+73833358088");
        printer.fsWriteTag(1171, "+73833399242");
        printer.fsWriteTag(1044, "Прием денежных средств");
        printer.fsWriteTag(1026, "РНКО \"ПЛАТЕЖНЫЙ ЦЕНТР\"");

        //receipt.getReceipt() - текст чека типа String
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

//    private void printReceipt2(ShtrihFiscalPrinter printer) throws JposException, InterruptedException {
//        printer.resetPrinter();
//        int numHeaderLines = printer.getNumHeaderLines();
//        for (int i = 1; i <= numHeaderLines; i++) {
//            printer.setHeaderLine(i, "Header line " + i, false);
//        }
//        int numTrailerLines = printer.getNumTrailerLines();
//        for (int i = 1; i <= numTrailerLines; i++) {
//            printer.setTrailerLine(i, "Trailer line " + i, false);
//        }
//        printer.setAdditionalHeader("AdditionalHeader line 1\nAdditionalHeader line 2");
//        printer.setFiscalReceiptType(FiscalPrinterConst.FPTR_RT_SALES);
//        printer.beginFiscalReceipt(true);
//        printer.printRecItem("За газ\nТел.: 123456\nЛ/сч: 789456", 100, 12, 0, 0, "");
//        printer.printRecTotal(70, 70, "0");
//        printer.printRecTotal(10, 10, "1");
//        printer.printRecTotal(10, 10, "2");
//        printer.printRecTotal(10, 10, "3");
//
//        printer.endFiscalReceipt(false);
//    }

    @Override
    Message makeMessage() {
        return new Message("Печать чека");
    }

    @Override
    protected void exec(ShtrihFiscalPrinter printer) throws Exception {
        printSalesReceipt(printer);
    }
}