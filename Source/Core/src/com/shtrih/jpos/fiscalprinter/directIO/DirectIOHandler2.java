/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shtrih.jpos.fiscalprinter.directIO;

import jpos.JposConst;

import java.util.Arrays;

import jpos.JposException;

import com.shtrih.jpos.fiscalprinter.FirmwareUpdateObserver;
import com.shtrih.util.Localizer;
import com.shtrih.barcode.PrinterBarcode;
import com.shtrih.fiscalprinter.FontNumber;
import com.shtrih.jpos.fiscalprinter.SmFptrConst;
import com.shtrih.jpos.fiscalprinter.PrinterImage;
import com.shtrih.jpos.fiscalprinter.ReceiptImage;
import com.shtrih.jpos.fiscalprinter.DioCsvZReport;
import com.shtrih.fiscalprinter.command.PrinterStatus;
import com.shtrih.fiscalprinter.command.PrinterCommand;
import com.shtrih.jpos.fiscalprinter.FiscalPrinterImpl;
import com.shtrih.fiscalprinter.SMFiscalPrinter;
import com.shtrih.fiscalprinter.command.CashRegister;
import com.shtrih.fiscalprinter.command.OperationRegister;

import static com.shtrih.fiscalprinter.command.PrinterConst.SMFP_STATION_REC;

import com.shtrih.jpos.DIOUtils;
import com.shtrih.jpos.fiscalprinter.FptrParameters;

/**
 * @author V.Kravtsov
 */
public class DirectIOHandler2 {

    private final FiscalPrinterImpl service;

    public DirectIOHandler2(FiscalPrinterImpl service) {
        this.service = service;
    }

    public SMFiscalPrinter getPrinter() {
        return service.getPrinter();
    }

    public FptrParameters getParams() {
        return service.getParams();
    }

    public void directIO(int command, int[] data, Object object)
            throws Exception {
        PrinterImage image;
        switch (command) {

            case SmFptrConst.SMFPTR_DIO_COMMAND:
                new DIOExecuteCommand(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_BARCODE_OBJECT:
                service.printBarcode((PrinterBarcode) object);
                break;

            case SmFptrConst.SMFPTR_DIO_SET_DEPARTMENT:
                getParams().department = ((int[]) object)[0];
                break;

            case SmFptrConst.SMFPTR_DIO_GET_DEPARTMENT:
                ((int[]) object)[0] = getParams().department;
                break;

            case SmFptrConst.SMFPTR_DIO_STRCOMMAND:
                new DIOExecuteCommandStr2(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READTABLE:
                new DIOReadTable2(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_WRITETABLE:
                new DIOWriteTable2(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_PAYMENT_NAME:
                new DIOReadPaymentName(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_WRITE_PAYMENT_NAME:
                new DIOWritePaymentName(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_DAY_END:
                PrinterStatus status = getPrinter().readLongPrinterStatus();
                if (status.getPrinterMode().isDayEndRequired()) {
                    data[0] = 1;
                } else {
                    data[0] = 0;
                }
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_BARCODE:
                new DIOPrintBarcode2(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_LOAD_IMAGE:
                dioLoadImage(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_IMAGE:
                image = service.getPrinterImages().get(data[0]);
                getPrinter().printImage(image);
                getPrinter().waitForPrinting();
                break;

            case SmFptrConst.SMFPTR_DIO_CLEAR_IMAGES:
                service.getPrinterImages().clear();
                service.saveProperties();
                break;

            case SmFptrConst.SMFPTR_DIO_ADD_LOGO:
                dioAddLogo(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_LOAD_LOGO:
                dioLoadLogo(data, object);
                break;

            // clear logo
            case SmFptrConst.SMFPTR_DIO_CLEAR_LOGO:
                service.getReceiptImages().clear();
                service.saveProperties();
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_LINE:
                dioPrintLine(data, object);
                break;

            // get driver parameter
            case SmFptrConst.SMFPTR_DIO_GET_DRIVER_PARAMETER:
                dioGetDriverParameter(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_SET_DRIVER_PARAMETER:
                dioSetDriverParameter(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_TEXT:
                new DIOPrintText(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_WRITE_TABLES:
                service.writeTables((String) object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_TABLES:
                service.readTables((String) object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_SERIAL:
                new DIOReadSerial(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_EJ_SERIAL:
                new DIOReadEJSerial(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_OPEN_DRAWER:
                new DIOOpenDrawer(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_DRAWER_STATE:
                new DIOReadDrawerState(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_PRINTER_STATUS:
                dioReadPrinterStatus(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_CASH_REG:
                dioReadCashReg(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_OPER_REG:
                dioReadOperReg(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_COMMAND_OBJECT:
                PrinterCommand printerCommand = (PrinterCommand) object;
                getPrinter().execute(printerCommand);
                break;

            case SmFptrConst.SMFPTR_DIO_XML_ZREPORT:
                new DIOXMLZReport(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_CSV_ZREPORT:
                new DioCsvZReport(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_WRITE_DEVICE_PARAMETER:
                new DIOWriteParameter(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_DEVICE_PARAMETER:
                new DIOReadParameter(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_DAY_STATUS:
                new DIOReadDayStatus(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_LICENSE:
                new DIOReadLicense(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_IS_READY_FISCAL:
                new DIOIsReadyFiscal(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_IS_READY_NONFISCAL:
                new DIOIsReadyNonFiscal(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_MAX_GRAPHICS:
                new DIOReadMaxGraphics(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_GET_HEADER_LINE:
                new DIOReadHeaderLine(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_GET_TRAILER_LINE:
                new DIOReadTrailerLine(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_GET_TEXT_LENGTH:
                dioReadTextLength(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_CASHIER_NAME:
                new DIOReadCashierName(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_WRITE_CASHIER_NAME:
                new DIOWriteCashierName(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_CUT_PAPER:
                new DIOCutPaper(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_WAIT_PRINT:
                new DIOWaitPrint(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_GET_RECEIPT_STATE:
                new DIOGetReceiptState(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_OPEN_DAY:
                new DIOOpenFiscalDay(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_SHORT_STATUS:
                new DIOReadShortStatus(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_LONG_STATUS:
                new DIOReadLongStatus(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_CANCELIO:
                getParams().cancelIO = true;
                break;

            case SmFptrConst.SMFPTR_DIO_FS_WRITE_TAG:
                new DIOFSWriteTag(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_WRITE_TLV:
                new DIOFSWriteTLV(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_WRITE_OPERATION_TLV:
                new DIOFSWriteOperationTLV(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_DOC_END:
                new DIOPrintDocEnd(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_DISABLE_PRINT:
                new DIODisablePrint(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_NON_FISCAL:
                new DIOPrintNonFiscal(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_WRITE_CUSTOMER_EMAIL:
                new DIOFSWriteCustomerEmail(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_WRITE_CUSTOMER_PHONE:
                new DIOFSWriteCustomerPhone(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_PRINT_CALC_REPORT:
                new DIOFSPrintCalcReport(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_JOURNAL:
                new DIOPrintJournal(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_SET_DISCOUNT_AMOUNT:
                new DIOSetDiscountAmount(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_FS_PARAMS:
                new DIOReadFSParams(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_FS_PARAMS2:
                new DIOReadFSParams2(service).execute(data, object);
                break;
                
            case SmFptrConst.SMFPTR_DIO_READ_FS_TICKETS:
                new DIOReadFSTickets(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_FS_TICKETS2:
                new DIOReadFSTickets2(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_FS_TICKETS3:
                new DIOReadFSTickets3(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_FS_TICKETS4:
                new DIOReadFSTickets4(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_CORRECTION2:
                new DIOPrintCorrectionReceipt2(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_CORRECTION:
                new DIOPrintCorrectionReceipt(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_TOTALS:
                new DIOReadTotals(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_PRINT_RAW_GRAPHICS:
                new DIOPrintRawGraphics(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_MAX_GRAPHICS_WIDTH:
                new DIOReadMaxGraphicsWidth(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FEED_PAPER:
                new DIOFeedPaper(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_GET_FS_SERVICE_STATE:
                new DIOGetFSServiceState(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_SET_FS_SERVICE_STATE:
                new DIOSetFSServiceState(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_CONTINUE_PRINT:
                new DIOContinuePrint(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_READ_DOCUMENT_TLV:
                new DIOReadDocumentTLV(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_READ_DAY_OPEN:
                new DIOReadDayOpen(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_READ_DAY_CLOSE:
                new DIOReadDayClose(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_READ_RECEIPT:
                new DIOReadReceipt(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_READ_STATUS:
                new DIOFSReadStatus(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_FIND_DOCUMENT:
                new DIOFSFindDocument(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_DISABLE_DOCEND:
                new DIODisableDocEnd(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_READ_FISCALIZATION_TAG:
                new DIOReadFiscalizationTag(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_READ_FISCALIZATION_TLV:
                new DIOReadFiscalizationTLV(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_DEVICE_METRICS:
                new DIOReadDeviceMetrics(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_TOTALIZER:
                new DIOReadTotalizers(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_FS_READ_DOCUMENT_TLV_TEXT:
                new DIOReadDocumentTLVText(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_EJ_DOCUMENT:
                new DIOReadEJDocument(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_START_DAY_CLOSE:
                new DIOStartDayClose(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_START_DAY_OPEN:
                new DIOStartDayOpen(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_START_FISCALIZATION:
                new DIOStartFiscalization(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_START_CALC_REPORT:
                new DIOStartCalcReport(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_START_FISCAL_CLOSE:
                new DIOStartFiscalClose(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_SET_ITEM_CODE:
                new DIOSetItemCode(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_SEND_ITEM_CODE:
                new DIOSendItemCode(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_CHECK_ITEM_CODE:
                new DIOCheckItemCode(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_ACCEPT_ITEM_CODE:
                new DIOAcceptItemCode(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_BIND_ITEM_CODE:
                new DIOBindItemCode(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_KM_SERVER_STATUS:
                new DIOReadKMServerStatus(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_READ_FFD_VERSION:
                new DIOGetFFDVersion(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_WRITE_FFD_VERSION:
                new DIOSetFFDVersion(service).execute(data, object);
                break;

            case SmFptrConst.SMFPTR_DIO_GET_RECEIPT_FIELD:
                new DIOGetReceiptField(service).execute(data, object);
                break;
                
            case SmFptrConst.SMFPTR_DIO_SET_RECEIPT_FIELD:
                new DIOSetReceiptField(service).execute(data, object);
                break;
                
            default:
                throw new JposException(JposConst.JPOS_E_ILLEGAL, Localizer.getString(Localizer.invalidParameterValue) + ", command");
        }
    }

    public void dioAddLogo(int[] data, Object object) throws Exception {
        int[] params = (int[]) object;
        int imageIndex = params[0];
        int position = params[1];
        service.getPrinterImages().checkIndex(imageIndex);
        ReceiptImage logo = new ReceiptImage(imageIndex, position);
        service.getReceiptImages().add(logo);
        service.saveProperties();
    }

    public void dioLoadLogo(int[] data, Object object) throws Exception {
        String fileName = (String) object;
        int logoPosition = data[0];
        int imageIndex = service.loadLogo(fileName, logoPosition);
        data[0] = imageIndex;
    }

    public void dioPrintLine(int[] data, Object object) throws Exception {
        int[] params = (int[]) object;
        byte[] lineData = new byte[service.getPrinter().getMaxGraphicsLineWidth() / 8];
        int lineHeight = params[0];
        int lineType = SmFptrConst.SMFPTR_LINE_TYPE_BLACK;
        if (params.length > 1) {
            lineType = params[1];
        }
        if (lineType == SmFptrConst.SMFPTR_LINE_TYPE_WHITE) {
            Arrays.fill(lineData, (byte) 0x00);
        } else {
            Arrays.fill(lineData, (byte) 0xFF);
        }
        getPrinter().printGraphicLine(SMFP_STATION_REC, lineHeight, lineData);
        getPrinter().waitForPrinting();
    }

    public void dioReadPrinterStatus(int[] data, Object object) throws Exception {
        Object[] params = (Object[]) object;
        PrinterStatus status = service.readPrinterStatus();
        params[0] = status;
    }

    public void dioReadTextLength(int[] data, Object object) throws Exception {
        DIOUtils.checkDataMinLength(data, 1);
        int fontNumber = data[0];
        int textLength = service.getPrinter().getModel()
                .getTextLength(new FontNumber(fontNumber));
        data[0] = textLength;
    }

    public void dioGetDriverParameter(int[] data, Object object) throws Exception {
        DIOUtils.checkDataMinLength(data, 1);
        int paramID = data[0];
        String paramValue = "";
        switch (paramID) {
            case SmFptrConst.SMFPTR_DIO_PARAM_REPORT_DEVICE:
                paramValue = String.valueOf(getParams().reportDevice);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_REPORT_TYPE:
                paramValue = String.valueOf(getParams().reportType);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_NUMHEADERLINES:
                paramValue = String.valueOf(getParams().numHeaderLines);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_NUMTRAILERLINES:
                paramValue = String.valueOf(getParams().numTrailerLines);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_POLL_ENABLED:
                paramValue = String.valueOf(getParams().pollEnabled);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_CUT_MODE:
                paramValue = String.valueOf(service.getParams().cutMode);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_FONT_NUMBER:
                paramValue = String.valueOf(service.getFontNumber());
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_SYS_PASSWORD:
                paramValue = String.valueOf(service.getPrinter().getSysPassword());
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_USR_PASSWORD:
                paramValue = String.valueOf(service.getPrinter().getUsrPassword());
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_PASSWORD:
                paramValue = String.valueOf(service.getPrinter().getTaxPassword());
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_0:
                paramValue = String.valueOf(service.getParams().taxAmount[0]);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_1:
                paramValue = String.valueOf(service.getParams().taxAmount[1]);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_2:
                paramValue = String.valueOf(service.getParams().taxAmount[2]);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_3:
                paramValue = String.valueOf(service.getParams().taxAmount[3]);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_4:
                paramValue = String.valueOf(service.getParams().taxAmount[4]);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_5:
                paramValue = String.valueOf(service.getParams().taxAmount[5]);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_SYSTEM:
                paramValue = String.valueOf(service.getParams().taxSystem);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_TOTAL_AMOUNT:
                paramValue = String.valueOf(service.getParams().itemTotalAmount);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_PAYMENT_TYPE:
                paramValue = String.valueOf(service.getParams().paymentType);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_SUBJECT_TYPE:
                paramValue = String.valueOf(service.getParams().subjectType);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_NEW_ITEM_STATUS:
                paramValue = String.valueOf(service.getParams().newItemStatus);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_CHECK_MODE:
                paramValue = String.valueOf(service.getParams().itemCheckMode);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_MARK_TYPE:
                paramValue = String.valueOf(service.getParams().itemMarkType);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_TAX_AMOUNT:
                paramValue = String.valueOf(service.getParams().itemTaxAmount);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_DOC_NUM:
                paramValue = String.valueOf(service.getPrinter().getLastDocNumber());
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_DOC_MAC:
                paramValue = String.valueOf(service.getPrinter().getLastMacValue());
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_PROTOCOL_TYPE:
                paramValue = String.valueOf(service.getParams().protocolType);
                break;
        }
        ((String[]) object)[0] = paramValue;
    }

    public void dioSetDriverParameter(int[] data, Object object) throws Exception {
        DIOUtils.checkDataMinLength(data, 1);
        int paramID = data[0];

        if (paramID == SmFptrConst.SMFPTR_DIO_PARAM_FIRMWARE_UPDATE_OBSERVER) {
            service.setFirmwareUpdateObserver((FirmwareUpdateObserver) object);
            return;
        }

        long value = Long.parseLong(((String[]) object)[0]);
        switch (paramID) {
            case SmFptrConst.SMFPTR_DIO_PARAM_REPORT_DEVICE:
                service.getParams().reportDevice = (int) value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_REPORT_TYPE:
                service.getParams().reportType = (int) value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_NUMHEADERLINES:
                service.setNumHeaderLines((int) value);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_NUMTRAILERLINES:
                service.setNumTrailerLines((int) value);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_POLL_ENABLED:
                service.setPollEnabled(value != 0);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_CUT_MODE:
                getParams().cutMode = ((int) value);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_FONT_NUMBER:
                service.setFontNumber((int) value);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_SYS_PASSWORD:
                service.getPrinter().setSysPassword((int) value);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_USR_PASSWORD:
                service.getPrinter().setUsrPassword((int) value);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_PASSWORD:
                service.getPrinter().setTaxPassword((int) value);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_0:
                service.getParams().taxAmount[0] = value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_1:
                service.getParams().taxAmount[1] = value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_2:
                service.getParams().taxAmount[2] = value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_3:
                service.getParams().taxAmount[3] = value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_4:
                service.getParams().taxAmount[4] = value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_VALUE_5:
                service.getParams().taxAmount[5] = value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_TAX_SYSTEM:
                service.getParams().taxSystem = (int) value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_TOTAL_AMOUNT:
                service.getParams().itemTotalAmount = (long) value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_PAYMENT_TYPE:
                service.getParams().paymentType = (byte) ((int) value);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_SUBJECT_TYPE:
                service.getParams().subjectType = (byte) ((int) value);
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_MARK_TYPE:
                service.getParams().itemMarkType = (int) value;
                break;

            case SmFptrConst.SMFPTR_DIO_PARAM_ITEM_TAX_AMOUNT:
                service.getParams().itemTaxAmount = (long) value;
                break;
                
        }
    }

    public void dioReadCashReg(int[] data, Object object) throws Exception {
        DIOUtils.checkDataMinLength(data, 1);
        int number = data[0];
        Object[] params = (Object[]) object;
        CashRegister register = new CashRegister(number);
        long amount = service.printer.readCashRegisterCorrection(number);
        register.setValue(amount);
        params[0] = register;
    }

    public void dioReadOperReg(int[] data, Object object) throws Exception {
        DIOUtils.checkDataMinLength(data, 1);
        int number = data[0];
        Object[] params = (Object[]) object;
        OperationRegister register = new OperationRegister(number);
        service.printer.check(service.printer.readOperationRegister(register));
        params[0] = register;
    }

    public void dioLoadImage(int[] data, Object object) throws Exception {
        String fileName = ((String[]) (object))[0];
        PrinterImage printerImage = new PrinterImage(fileName);
        getPrinter().loadImage(printerImage, true);
        int imageIndex = service.getPrinterImages().getIndex(printerImage);
        data[0] = imageIndex;
    }
}
