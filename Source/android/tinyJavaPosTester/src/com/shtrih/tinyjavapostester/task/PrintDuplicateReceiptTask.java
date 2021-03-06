package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.activity.MainActivity;
import com.shtrih.tinyjavapostester.task.message.Message;

import jpos.JposException;

public class PrintDuplicateReceiptTask extends AbstractTask {


    public PrintDuplicateReceiptTask(AbstractActivity parent, MainViewModel model) {
        super(parent, model);
    }

    @Override
    Message makeMessage() {
        return new Message("Печать копии чека");
    }

    protected void exec(ShtrihFiscalPrinter printer) throws JposException {
        printer.resetPrinter();
        printer.printDuplicateReceipt();
    }

    @Override
    protected void postExec() {

    }
}