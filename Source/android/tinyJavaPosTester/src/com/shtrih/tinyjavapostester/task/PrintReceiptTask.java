package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.command.FSDocType;
import com.shtrih.fiscalprinter.command.FSDocumentReceipt;
import com.shtrih.fiscalprinter.command.FSStatusInfo;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.Receipt;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.task.listener.Listener;
import com.shtrih.tinyjavapostester.task.message.Message;
import com.shtrih.tinyjavapostester.util.AppUtil;

import jpos.JposException;

public class PrintReceiptTask extends AbstractTask {

    private Receipt receipt;
    private Listener<Exception> exceptionListener;
    private Boolean repeatPrint = false;


    public PrintReceiptTask(AbstractActivity parent, MainViewModel model, Receipt receipt, Listener<Exception> exceptionListener) {
        super(parent, model);
        this.receipt = receipt;
        this.exceptionListener = exceptionListener;
    }


    @Override
    Message makeMessage() {
        return new Message("Печать чека и отправка результата");
    }

    @Override
    protected void exec(ShtrihFiscalPrinter printer) {
        long expectedDocNumber = -1;

        try {
            expectedDocNumber = printer.fsReadStatus().getDocNumber() + 1;

            receipt.print(printer);

            repeatPrint = false;

            exceptionListener.handle(null);
        } catch (Exception e) {
            resetReceipt(printer, expectedDocNumber);

            exceptionListener.handle(e);
        }
    }

    private void repeatPrint(ShtrihFiscalPrinter printer, long expectedDocNumber) {
        if (AppUtil.getFSDocumentReceipt(printer, expectedDocNumber) != null) {
            try {
                printer.printDuplicateReceipt();
            } catch (JposException e1) {
                exceptionListener.handle(e1);
            }
        } else {
            if (!repeatPrint) {
                repeatPrint = true;
                exec(printer);
            }
        }
    }

    private void resetReceipt(ShtrihFiscalPrinter printer, long expectedDocNumber) {
        try {
            FSDocumentReceipt fsReceipt = AppUtil.getFSDocumentReceipt(printer, expectedDocNumber);
            if (fsReceipt == null) {
                printer.resetPrinter();
            }  else {
                FSStatusInfo fsStatus = printer.fsReadStatus();

                if (fsStatus.getDocType().getValue() != FSDocType.FS_DOCTYPE_NONE) {
                    printer.fsCancelDocument();
                }
            }

        } catch (Exception ignored) { }
    }

    @Override
    protected void postExec() {
    }
}