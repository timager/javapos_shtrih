package com.shtrih.tinyjavapostester.network;

import android.os.Build;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionBody {

    public TransactionBody(OrderResponse.Order order, JSONObject deepLinkData, String kktNumber, long receiptNumber) {
        orderId = order.getOrderId();
        packUuid = UUID.randomUUID().toString();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pack_uuid", packUuid);
            jsonObject.put("HST_Name", Build.DEVICE);
            jsonObject.put("KKM", kktNumber);
            jsonObject.put("check_num", receiptNumber);
            jsonObject.put("operation_type", deepLinkData.getInt("operation_type"));
            jsonObject.put("order_id", orderId);
            JSONArray operationPayments = new JSONArray();
            JSONObject operationData = deepLinkData.getJSONObject("operation_data");
            int paymentCash = operationData.getInt("payment_cash");
            JSONObject transactionPayCash = new JSONObject();
            transactionPayCash.put("pay_type", 1);
            transactionPayCash.put("pay_sum", paymentCash);
            operationPayments.put(transactionPayCash);
            int paymentCard = operationData.getInt("payment_card");
            JSONObject transactionPayCard = new JSONObject();
            transactionPayCard.put("pay_type", 2);
            transactionPayCard.put("pay_sum", paymentCard);
            operationPayments.put(transactionPayCard);
            jsonObject.put("operation_payments", operationPayments);
            JSONArray servs = new JSONArray();
            for (OrderResponse.Serv serv : order.getServs()) {
                JSONObject transactionServ = new JSONObject();
                transactionServ.put("serv_id", serv.getServId());
                transactionServ.put("serv_current_pay", serv.getServCostD());
                servs.put(transactionServ);
            }
            jsonObject.put("Servs", servs);
            json = jsonObject.toString();
            userId = deepLinkData.getInt("user_id");
            departNumber = deepLinkData.getString("depart_number");
            token = deepLinkData.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public TransactionBody(Integer orderId, Integer userId, String departNumber, String token, String packUuid, String json) {
        this.orderId = orderId;
        this.userId = userId;
        this.departNumber = departNumber;
        this.token = token;
        this.packUuid = packUuid;
        this.json = json;
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

    public static TransactionBody createRefundServiceTransactionBody(OrderResponse.Order order, JSONObject deepLinkData, String kktNumber, long receiptNumber) throws JSONException {
        Integer orderId = order.getOrderId();
        String packUuid = UUID.randomUUID().toString();
        int paymentType = deepLinkData.getJSONObject("operation_data").getInt("pay_type");

        List<Integer> refundServiceIdList = convertToList(deepLinkData.getJSONObject("operation_data").getJSONArray("servs"));
        List<OrderResponse.Serv> refundServiceList = new ArrayList<>();
        for (OrderResponse.Serv serv : order.getServs()) {
            if (refundServiceIdList.contains(serv.getServId())) {
                refundServiceList.add(serv);
            }
        }

        int sumPayment = 0;
        for (OrderResponse.Serv serv : refundServiceList) {
            sumPayment += Long.parseLong(serv.getServCost().replace(".", "")) / 100;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pack_uuid", packUuid);
            jsonObject.put("HST_Name", Build.DEVICE);
            jsonObject.put("KKM", kktNumber);
            jsonObject.put("check_num", receiptNumber);
            jsonObject.put("operation_type", deepLinkData.getInt("operation_type"));
            jsonObject.put("order_id", orderId);
            JSONArray operationPayments = new JSONArray();

            if (paymentType == 1) {
                JSONObject transactionPayCash = new JSONObject();
                transactionPayCash.put("pay_type", 1);
                transactionPayCash.put("pay_sum", -sumPayment);
                transactionPayCash.put("pay_id", 1);
                operationPayments.put(transactionPayCash);
            } else if (paymentType == 2) {
                JSONObject transactionPayCard = new JSONObject();
                transactionPayCard.put("pay_type", 2);
                transactionPayCard.put("pay_sum", -sumPayment);
                transactionPayCard.put("pay_id", 1);
                operationPayments.put(transactionPayCard);
            }

            jsonObject.put("operation_payments", operationPayments);

            JSONArray servs = new JSONArray();

            for (int i = 0; i < refundServiceList.size(); i++) {
                OrderResponse.Serv serv = refundServiceList.get(i);
                JSONObject service = new JSONObject();
                service.put("serv_id", serv.getServId());
                service.put("serv_current_pay", "-" + serv.getServCostD());
                service.put("pay_id", 1);
                servs.put(service);
            }
            jsonObject.put("Servs", servs);

            String json = jsonObject.toString();
            Integer userId = deepLinkData.getInt("user_id");
            String departNumber = deepLinkData.getString("depart_number");
            String token = deepLinkData.getString("token");

            return new TransactionBody(orderId, userId, departNumber, token, packUuid, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<Integer> convertToList(JSONArray array) {
        List<Integer> resultList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                Integer value = array.getInt(i);
                resultList.add(value);
            } catch (Exception ignored) {
            }
        }
        return resultList;
    }
}