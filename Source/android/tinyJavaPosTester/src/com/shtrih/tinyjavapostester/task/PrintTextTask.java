package com.shtrih.tinyjavapostester.task;

import com.shtrih.fiscalprinter.ShtrihFiscalPrinter;
import com.shtrih.fiscalprinter.command.FSReFiscalization;
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
        FSReFiscalization command = new FSReFiscalization(printer.getSysPassword(), "784105015925", "", 2, 1, 2);
        printer.executeCommand(command);
//        Fiscalizer fiscalizer = new Fiscalizer();
//        ParametersFiscal parameters = new ParametersFiscal();
//        parameters.CashierName = "Гераймович Николебн Максимович Погромизд";
//        parameters.CashierVATIN = "784105015925";
//        parameters.KKTNumber = "0000000001018733";
//        parameters.OrganizationName = "STb";
//        parameters.VATIN = "7842033384";
//        parameters.AddressSettle = "Улицы Сычова 25";
//        parameters.TaxVariant = "0,1,2,3";
//        parameters.OfflineMode = true;
//        parameters.DataEncryption = false;
//        parameters.SaleExcisableGoods = false;
//        parameters.SignOfGambling = false;
//        parameters.SignOfLottery = false;
//        parameters.BSOSing = false;
//        parameters.CalcOnlineSign = false;
//        parameters.PrinterAutomatic = false;
//        parameters.AutomaticMode = false;
//        parameters.ReasonCode = 2;
//        parameters.SignOfAgent = "0";
//        parameters.OFDOrganizationName = "ООО «ПЕТЕР-СЕРВИС Спецтехнологии»";
//        parameters.OFDVATIN = "7841465198";
//
//
//        try {
//            fiscalizer.refiscalizeFS(printer, parameters);
//        } catch (Exception e) {
//            parent.showMessage(e.getMessage());
//        }
        printer.printText(text);
        parent.showMessage(String.valueOf(printer.fsReadStatus().getStatus().getCode()));
    }

    @Override
    protected void postExec() {

    }
}