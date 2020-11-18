package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.tinyjavapostester.R;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.application.App;
import com.shtrih.tinyjavapostester.shar_pref.AppConst;
import com.shtrih.tinyjavapostester.task.message.Message;
import com.shtrih.tinyjavapostester.util.ToastUtil;

import jpos.JposException;

public class OpenDayTask extends AbstractTask {

    public OpenDayTask(AbstractActivity parent, MainViewModel model) {
        super(parent, model);
    }

    @Override
    Message makeMessage() {
        return new Message("Открытие смены");
    }

    protected void exec(ShtrihFiscalPrinter printer) throws JposException {
        String cashierName = App.getCashierName();

        if (AppConst.DEFAULT_CASHIER_NAME.equals(cashierName)) {
            ToastUtil.showMessage(R.string.message_operation_stop_please_login);
        } else {
            ToastUtil.showMessage("Имя пользователя:\n" + cashierName);

            printer.writeCashierName(App.getCashierName());

            printer.openFiscalDay();
        }
    }

    @Override
    protected void postExec() {
        parent.useDayOpened();
    }
}