package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.command.FSFiscalization;
import com.shtrih.fiscalprinter.command.FSReFiscalization;
import com.shtrih.fiscalprinter.command.FSRegistrationReport;
import com.shtrih.fiscalprinter.command.FSResetState;
import com.shtrih.jpos1c.xml.fnoperation.ParametersFiscal;
import com.shtrih.tinyjavapostester.Fiscalizer;
import com.shtrih.tinyjavapostester.MainViewModel;
import com.shtrih.tinyjavapostester.activity.AbstractActivity;
import com.shtrih.tinyjavapostester.task.message.Message;

import jpos.JposException;

public class PrintTextTask extends AbstractTask {

    private String text;

    public PrintTextTask(AbstractActivity parent, MainViewModel model, String text) {
        super(parent, model);
        this.text = text;
    }

    @Override
    Message makeMessage() {
        return new Message("Печать текста");
    }

    protected void exec(ShtrihFiscalPrinter printer) throws JposException {
        printer.resetPrinter();
        printer.printText(text);
    }

    @Override
    protected void postExec() {

    }
}