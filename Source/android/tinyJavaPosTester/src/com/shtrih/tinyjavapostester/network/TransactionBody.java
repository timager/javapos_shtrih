package com.shtrih.tinyjavapostester.network;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class TransactionBody {

    public TransactionBody(OrderResponse.Order order, JSONObject deepLinkData) {
        orderId = order.getOrderId();
        json = new Gson().toJson(order);
        packUuid = UUID.randomUUID().toString();
        try {
            userId = deepLinkData.getInt("user_id");
            departNumber = deepLinkData.getString("depart_number");
            token = deepLinkData.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SerializedName("order_id")
    @Expose
    private Integer orderId;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("depart_number")
    @Expose
    private String departNumber;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("pack_uuid")
    @Expose
    private String packUuid;
    @SerializedName("json")
    @Expose
    private String json;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getPackUuid() {
        return packUuid;
    }

    public void setPackUuid(String packUuid) {
        this.packUuid = packUuid;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

}