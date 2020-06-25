package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.activity.MainActivity;
import com.shtrih.tinyjavapostester.task.listener.BooleanListener;
import com.shtrih.tinyjavapostester.task.message.Message;

public class CheckDayOpenedTask extends AbstractTask {

    private BooleanListener listener;

    public CheckDayOpenedTask(AbstractActivity parent, MainViewModel model, BooleanListener listener) {
        super(parent, model);
        this.listener = listener;
    }

    @Override
    Message makeMessage() {
        return new Message("Проверка открытия смены");
    }

    @Override
    protected void exec(ShtrihFiscalPrinter printer) throws Exception {
        printer.resetPrinter();
        listener.action(printer.getDayOpened());
    }

    @Override
    protected void postExec() {

    }
}