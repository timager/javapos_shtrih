package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.application.App;
import com.shtrih.tinyjavapostester.task.message.Message;

public class PrintXReportTaskKKM extends AbstractTask {

    public PrintXReportTaskKKM(AbstractActivity parent, MainViewModel model) {
        super(parent, model);
    }

    @Override
    Message makeMessage() {
        return new Message("Печать X отчета");
    }

    @Override
    protected void exec(ShtrihFiscalPrinter printer) throws Exception {
        printer.resetPrinter();

        String cashierName = App.getCashierName();
        printer.writeCashierName(cashierName);

        printer.printXReport();
    }

    @Override
    protected void postExec() {

    }
}