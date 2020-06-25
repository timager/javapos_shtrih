package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.activity.MainActivity;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.task.message.Message;

import jpos.JposException;

public class OpenDayTask extends AbstractTask {

    public OpenDayTask(MainActivity parent, MainViewModel model) {
        super(parent, model);
    }

    @Override
    Message makeMessage() {
        return new Message("Открытие смены");
    }

    protected void exec(ShtrihFiscalPrinter printer) throws JposException {
        printer.openFiscalDay();
    }
}