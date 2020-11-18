package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.R;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.application.App;
import com.shtrih.tinyjavapostester.shar_pref.AppConst;
import com.shtrih.tinyjavapostester.task.message.Message;
import com.shtrih.tinyjavapostester.util.ToastUtil;

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
        String cashierName = App.getCashierName();

        if (AppConst.DEFAULT_CASHIER_NAME.equals(cashierName)) {
            ToastUtil.showMessage(R.string.message_operation_stop_please_login);
        } else {
            ToastUtil.showMessage("Имя пользователя:\n" + cashierName);

            printer.resetPrinter();
            printer.writeCashierName(App.getCashierName());

            printer.printZReport();
        }
    }

    @Override
    protected void postExec() {
        parent.useDayOpened();
    }
}