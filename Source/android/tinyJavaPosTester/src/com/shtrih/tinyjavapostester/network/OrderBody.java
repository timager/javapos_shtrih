package com.shtrih.tinyjavapostester.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

public class OrderBody {
    @SerializedName("order_number")
    @Expose
    private String orderNumber;
    @SerializedName("depart_number")
    @Expose
    private String departNumber;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("operation_type")
    @Expose
    private String operationType;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getDepartNumber() {
        return departNumber;
    }

    public void setDepartNumber(String departNumber) {
        this.departNumber = departNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }


    public OrderBody(JSONObject data) throws JSONException {
        orderNumber = data.getString("order_number");
        departNumber = data.getString("depart_number");
        token = data.getString("token");
        operationType = data.getString("operation_type");
    }
}
