package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.activity.MainActivity;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.task.message.Message;

public class PrintXReportTaskKKM extends AbstractTask {

    public PrintXReportTaskKKM(MainActivity parent, MainViewModel model) {
        super(parent, model);
    }

    @Override
    Message makeMessage() {
        return new Message("Печать X отчета");
    }

    @Override
    protected void exec(ShtrihFiscalPrinter printer) throws Exception {
        printer.resetPrinter();
        printer.printXReport();
    }
}