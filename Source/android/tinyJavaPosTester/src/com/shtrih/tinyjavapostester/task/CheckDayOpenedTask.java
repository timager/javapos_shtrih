package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.task.listener.Listener;
import com.shtrih.tinyjavapostester.task.message.Message;

public class CheckDayOpenedTask extends AbstractTask {

    private Listener<Boolean> listener;

    public CheckDayOpenedTask(AbstractActivity parent, MainViewModel model, Listener<Boolean> listener) {
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
        listener.handle(printer.getDayOpened());
    }

    @Override
    protected void postExec() {

    }
}