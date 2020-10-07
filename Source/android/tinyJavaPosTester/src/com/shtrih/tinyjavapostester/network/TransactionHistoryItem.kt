package com.shtrih.tinyjavapostester.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TransactionHistoryItem(
    @SerializedName("depart") @Expose val depart: Int,
    @SerializedName("DateTime") @Expose  val dateTime: String,
    @SerializedName("operation_type") @Expose val operationType: Int,
    @SerializedName("Pay_Type") @Expose val payType: Int,
    @SerializedName("Summ") @Expose val sum: Int,
    @SerializedName("Check_Num") @Expose val checkNum: String,
    @SerializedName("Ftran_Id") @Expose val id: Int,
    @SerializedName("KKM") @Expose val kkm: String,
    @SerializedName("BT_TerrmNum") @Expose val bt_terrmNum: String,
    @SerializedName("BT_ClientCard") @Expose val bt_clientCard: String,
    @SerializedName("BT_ClientExpiryDate") @Expose val bt_clientExpiryDate: String,
    @SerializedName("BT_CardName") @Expose val bt_cardName: String,
    @SerializedName("HST_Name") @Expose val hst_Name: String
)