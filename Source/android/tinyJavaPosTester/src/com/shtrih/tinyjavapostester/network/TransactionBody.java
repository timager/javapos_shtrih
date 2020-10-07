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

    public static TransactionBody createSaleTransactionBody(OrderResponse.Order order, JSONObject deepLinkData, String kktNumber, long receiptNumber) {
        Integer orderId = order.getOrderId();
        String packUuid = UUID.randomUUID().toString();

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

        double sumPayment = 0;
        for (OrderResponse.Serv serv : refundServiceList) {
            sumPayment += Double.parseDouble(serv.getServCost());
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

    public static TransactionBody createRefundTransactionTransactionBody(OrderResponse.Order order, JSONObject deepLinkData, String kktNumber, long receiptNumber) throws JSONException {
        Integer orderId = order.getOrderId();
        String packUuid = UUID.randomUUID().toString();
        int paymentType = 1;

        List<Integer> transactionRefundIdList = convertToList(deepLinkData.getJSONObject("operation_data").getJSONArray("transactions"));
        double sumRefund = 0;
        for (TransactionHistoryItem transactionHistoryItem : order.getPayHistory()) {
            if (transactionRefundIdList.contains(transactionHistoryItem.getId())) {
                sumRefund += transactionHistoryItem.getSum();
            }
        }

        double sumRate = sumRefund / order.getOrderAmount();

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
                transactionPayCash.put("pay_sum", -sumRefund);
                transactionPayCash.put("pay_id", 1);
                operationPayments.put(transactionPayCash);
            } else if (paymentType == 2) {
                JSONObject transactionPayCard = new JSONObject();
                transactionPayCard.put("pay_type", 2);
                transactionPayCard.put("pay_sum", -sumRefund);
                transactionPayCard.put("pay_id", 1);
                operationPayments.put(transactionPayCard);
            }

            jsonObject.put("operation_payments", operationPayments);

            JSONArray servs = new JSONArray();
            double sumHandleItem = 0;

            for (int i = 0; i < order.getServs().size(); i++) {
                OrderResponse.Serv serv = order.getServs().get(i);

                double priceService = Double.parseDouble(serv.getServCostD());

                double finalPrice;
                // Последняя цена высчитывается из суммы возврата - сумма всех позиций в чеке, кроме последнего
                if (i != order.getServs().size() - 1) {
                    finalPrice = sumRate * priceService;
                    sumHandleItem += finalPrice;
                } else {
                    finalPrice = sumRefund - sumHandleItem;
                }

                JSONObject service = new JSONObject();
                service.put("serv_id", serv.getServId());
                service.put("serv_current_pay", "-" + finalPrice);
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

    public static TransactionBody createRefundByReasonTransactionBody(OrderResponse.Order order, JSONObject deepLinkData, String kktNumber, long receiptNumber) throws JSONException {
        Integer orderId = order.getOrderId();
        String packUuid = UUID.randomUUID().toString();
        int paymentType = 1;
        int sumPayment = deepLinkData.getJSONObject("operation_data").getInt("sum");

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