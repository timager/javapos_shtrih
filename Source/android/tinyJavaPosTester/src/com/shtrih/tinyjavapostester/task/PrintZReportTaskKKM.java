package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.activity.MainActivity;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.task.message.Message;

public class PrintZReportTaskKKM extends AbstractTask {

    public PrintZReportTaskKKM(AbstractActivity parent, MainViewModel model) {
        super(parent, model);
    }

    @Override
    Message makeMessage() {
        return new Message("Печать Z отчета");
    }

    @Override
    protected void exec(ShtrihFiscalPrinter printer) throws Exception {
        printer.resetPrinter();
        printer.printZReport();
    }

    @Override
    protected void postExec() {
        parent.useDayOpened();
    }
}