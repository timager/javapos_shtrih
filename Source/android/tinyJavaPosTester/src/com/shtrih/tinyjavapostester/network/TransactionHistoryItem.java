package com.shtrih.tinyjavapostester.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TransactionHistoryItem implements Serializable {
    @SerializedName("depart")
    @Expose
    public Integer depart;
    @SerializedName("DateTime")
    @Expose
    public String dateTime;
    @SerializedName("operation_type")
    @Expose
    public Integer operationType;
    @SerializedName("Pay_Type")
    @Expose
    public Integer payType;
    // Сумма транзакции со сдачей (в рублях)
    @SerializedName("Summ")
    @Expose
    public Integer sum;
    @SerializedName("Check_Num")
    @Expose
    public String checkNum;
    @SerializedName("Ftran_Id")
    @Expose
    public Integer id;
    @SerializedName("KKM")
    @Expose
    public String kkm;
    @SerializedName("BT_TerrmNum")
    @Expose
    public String bt_terrmNum;
    @SerializedName("BT_ClientCard")
    @Expose
    public String bt_clientCard;
    @SerializedName("BT_ClientExpiryDate")
    @Expose
    public String bt_clientExpiryDate;
    @SerializedName("BT_CardName")
    @Expose
    public String bt_cardName;
    @SerializedName("HST_Name")
    @Expose
    public String hst_Name;
    // Сдача (в рублях)
    @SerializedName("Change")
    @Expose
    public Integer change;

    public boolean isCashPayment() {
        return payType == 1;
    }

    public boolean isCardPayment() {
        return payType == 2;
    }

    // Сумма транзакции без сдачи
    public Integer getSumWithoutChange() {
        if (change != null) {
            return sum - change;
        } else {
            return sum;
        }
    }
}
