package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.Receipt;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.task.listener.Listener;
import com.shtrih.tinyjavapostester.task.message.Message;

public class PrintReceiptTask extends AbstractTask {

    private Receipt receipt;
    private Listener<Exception> exceptionListener;


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
        try {
            receipt.print(printer);
            exceptionListener.handle(null);
        } catch (Exception e) {
            exceptionListener.handle(e);
        }
    }

    @Override
    protected void postExec() {
    }
}