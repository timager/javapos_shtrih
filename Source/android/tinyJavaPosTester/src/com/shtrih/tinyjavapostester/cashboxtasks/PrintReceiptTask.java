package com.shtrih.tinyjavapostester.cashboxtasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.shtrih.fiscalprinter.command.FSCancelDoc;
import com.shtrih.fiscalprinter.command.FSDayClose;
import com.shtrih.fiscalprinter.command.FSFiscalization;
import com.shtrih.fiscalprinter.command.FSOpenDay;
import com.shtrih.fiscalprinter.command.FSReFiscalization;
import com.shtrih.fiscalprinter.command.FSReadFiscalization;
import com.shtrih.fiscalprinter.command.FSReadStatus;
import com.shtrih.fiscalprinter.command.FSResetState;
import com.shtrih.fiscalprinter.command.FSStartFiscalization;
import com.shtrih.jpos.fiscalprinter.SmFptrConst;
import com.shtrih.jpos1c.xml.fnoperation.ParametersFiscal;
import com.shtrih.tinyjavapostester.Fiscalizer;
import com.shtrih.tinyjavapostester.MainActivity;
import com.shtrih.tinyjavapostester.MainViewModel;

import jpos.FiscalPrinterConst;
import jpos.JposException;

public class PrintReceiptTask extends AsyncTask<Void, Void, String> {

    private final MainActivity parent;
    private final int positions;
    private final int strings;

    private long startedAt;
    private long doneAt;
    private ProgressDialog dialog;

    private final MainViewModel model;

    public PrintReceiptTask(MainActivity parent, int positions, int strings, MainViewModel model) {
        this.parent = parent;
        this.positions = positions;
        this.strings = strings;
        this.model = model;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog = ProgressDialog.show(parent, "Printing receipt", "Please wait...", true);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            model.getPrinter().resetPrinter();
            startedAt = System.currentTimeMillis();
//            printReceipt2();
            reFiscalization();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            doneAt = System.currentTimeMillis();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        dialog.dismiss();

        if (result == null)
            parent.showMessage("Success " + (doneAt - startedAt) + " ms");
        else
            parent.showMessage(result);
    }


    private void openDay() throws JposException {
        FSOpenDay command = new FSOpenDay();
        command.setSysPassword(model.getPrinter().getSysPassword());
        model.getPrinter().fsOpenDay(command);
    }

    private void openDay2() throws JposException {

        model.getPrinter().openFiscalDay();
    }

    private void reFiscalization() throws Exception {
        model.getPrinter().resetPrinter();
        Fiscalizer fiscalizer = new Fiscalizer();
        ParametersFiscal parameters = new ParametersFiscal();
        parameters.CashierName = "Гераймович Николебн Максимович Погромизд";
        parameters.CashierVATIN = "784105015925";
        parameters.KKTNumber = "0000000001018733";
        parameters.OrganizationName = "STb";
        parameters.VATIN = "7842033384";
        parameters.AddressSettle = "Улицы Сычова 25";
        parameters.TaxVariant = "0,1,2,3";
        parameters.OfflineMode = true;
        parameters.DataEncryption = false;
        parameters.SaleExcisableGoods = false;
        parameters.SignOfGambling = false;
        parameters.SignOfLottery = false;
        parameters.BSOSing = false;
        parameters.CalcOnlineSign = false;
        parameters.PrinterAutomatic = false;
        parameters.AutomaticMode = false;
        parameters.ReasonCode = 2;
        parameters.SignOfAgent = "0";
        parameters.OFDOrganizationName = "ООО «ПЕТЕР-СЕРВИС Спецтехнологии»";
        parameters.OFDVATIN = "7841465198";


        fiscalizer.refiscalizeFS(model.getPrinter(), parameters);
//        FSReFiscalization command = new FSReFiscalization(model.getPrinter().getSysPassword(), "784105015925", "", 2, 1, 2);
//        model.getPrinter().executeCommand(command);
    }

    private void printReceipt2() throws JposException, InterruptedException {
//        model.getPrinter().resetPrinter();
//        model.getPrinter().endNonFiscal();
//        FSReFiscalization command = new FSReFiscalization(model.getPrinter().getUsrPassword(), "784105015925", "0000000001018733", 2, 1, 2);
//        model.getPrinter().executeCommand(command);
//        FSResetState command = new FSResetState();
//        command.setSysPassword(model.getPrinter().getUsrPassword());
//        model.getPrinter().fsResetState(command);
//        int numHeaderLines = model.getPrinter().getNumHeaderLines();
//        for (int i = 1; i <= numHeaderLines; i++) {
//            model.getPrinter().setHeaderLine(i, "Header line " + i, false);
//        }
//        int numTrailerLines = model.getPrinter().getNumTrailerLines();
//        for (int i = 1; i <= numTrailerLines; i++) {
//            model.getPrinter().setTrailerLine(i, "Trailer line " + i, false);
//        }
//        model.getPrinter().setAdditionalHeader("AdditionalHeader line 1\nAdditionalHeader line 2");
//
//
//        model.getPrinter().setFiscalReceiptType(FiscalPrinterConst.FPTR_GD_NONFISCAL_DOC_VOID);
//        model.getPrinter().beginFiscalReceipt(true);
//        model.getPrinter().printRecItem("test: 789456", 100, 12, 0, 0, "");
//        model.getPrinter().printRecTotal(70, 70, "0");
//        model.getPrinter().printRecTotal(10, 10, "1");
//        model.getPrinter().printRecTotal(10, 10, "2");
//        model.getPrinter().printRecTotal(10, 10, "3");

//        model.getPrinter().endFiscalReceipt(false);
//        model.getPrinter().printDuplicateReceipt();
    }
}