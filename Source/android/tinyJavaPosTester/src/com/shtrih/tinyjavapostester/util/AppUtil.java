package com.shtrih.tinyjavapostester.util;

import com.shtrih.tinyjavapostester.network.OrderResponse;
import com.shtrih.tinyjavapostester.network.TransactionHistoryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppUtil {
    public static List<Integer> convertToListInteger(JSONArray array) throws JSONException {
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

    public static double getSumSalePaymentFromDeepLink(JSONObject deepLinkData) throws JSONException {
        JSONObject operationData = deepLinkData.getJSONObject("operation_data");

        double paymentCash = operationData.getDouble("payment_cash");
        double paymentCard = operationData.getDouble("payment_card");

        return paymentCash + paymentCard;
    }

    public static double getSumRefundTransactionFromData(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        List<Integer> transactionRefundIdList = AppUtil.convertToListInteger(deepLinkData.getJSONObject("operation_data").getJSONArray("transactions"));
        double sumRefund = 0;
        for (TransactionHistoryItem transactionHistoryItem : order.getPayHistory()) {
            if (transactionRefundIdList.contains(transactionHistoryItem.id)) {
                sumRefund += transactionHistoryItem.sum;
            }
        }

        return sumRefund;
    }

    public static double getSumRefundServiceFromData(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        List<Integer> refundServiceIdList = AppUtil.convertToListInteger(deepLinkData.getJSONObject("operation_data").getJSONArray("servs"));
        List<OrderResponse.Serv> refundServiceList = new ArrayList<>();
        for (OrderResponse.Serv serv : order.getServs()) {
            if (refundServiceIdList.contains(serv.getServId())) {
                refundServiceList.add(serv);
            }
        }

        double sumRefund = refundServiceList.stream().map(serv -> Double.parseDouble(serv.getServCostD())).reduce(0d, Double::sum);
        return sumRefund;
    }



    public static double getSumRefundByReasonFromData(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        return deepLinkData.getJSONObject("operation_data").getDouble("sum");
    }


    public static boolean isZeroPaymentFromDeepLink(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        if (deepLinkData == null || order == null)
            return false;

        if (isSale(deepLinkData) && getSumSalePaymentFromDeepLink(deepLinkData) == 0) {
            return true;
        }

        if (isRefundService(deepLinkData) && getSumRefundServiceFromData(deepLinkData, order) == 0) {
            return true;
        }

        if (isRefundTransaction(deepLinkData) && getSumRefundTransactionFromData(deepLinkData, order) == 0) {
            return true;
        }

        if (isRefundByReason(deepLinkData) && getSumRefundByReasonFromData(deepLinkData, order) == 0) {
            return true;
        }

        return false;
    }

    public static List<Double> getListServicePaymentByPartition(List<OrderResponse.Serv> services, double fullSum, double partSum) {
        double rate = partSum / fullSum;
        Integer lastIndexNotNullSum = getLastIndexNotZeroPrice(services);

        List<Double> listResult = services.stream()
                .map(serv -> Double.parseDouble(serv.getServCostD()))
                .map(servCost -> servCost != 0 ? servCost * rate : 0d)
                .map(servCost -> new BigDecimal(servCost).setScale(2, RoundingMode.FLOOR).doubleValue())
                .collect(Collectors.toList());


        if (lastIndexNotNullSum != null) {
            double prepareResultCost = listResult.stream().reduce(0d, Double::sum);
            double remain = partSum - prepareResultCost;

            listResult.set(lastIndexNotNullSum, listResult.get(lastIndexNotNullSum) + remain);
        }

        return listResult;
    }

    public static Integer getLastIndexNotZeroPrice(List<OrderResponse.Serv> services) {
        for (int i = services.size() - 1; i >= 0; i--) {
            OrderResponse.Serv serv = services.get(i);
            if (Double.parseDouble(serv.getServCostD()) != 0) {
                return i;
            }
        }

        return null;
    }

    public static boolean isFullSale(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        return isSale(deepLinkData)
                && AppUtil.getSumSalePaymentFromDeepLink(deepLinkData) == getOrderSum(order);

    }

    public static boolean isPartitionSale(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        return isSale(deepLinkData)
                && AppUtil.getSumSalePaymentFromDeepLink(deepLinkData) != getOrderSum(order);
    }

    public static double getOrderSum(OrderResponse.Order order) {
        return order.getOrderAmountWithBenefits();
    }

    public static boolean isRefundService(JSONObject deepLinkData) {
        return isRefund(deepLinkData)
                && deepLinkData.optJSONObject("operation_data") != null
                && deepLinkData.optJSONObject("operation_data").opt("servs") != null;
    }

    public static boolean isRefundTransaction(JSONObject deepLinkData) {
        return isRefund(deepLinkData)
                && deepLinkData.optJSONObject("operation_data") != null
                && deepLinkData.optJSONObject("operation_data").opt("transactions") != null;
    }

    public static boolean isRefundByReason(JSONObject deepLinkData) {
        return isRefund(deepLinkData)
                && deepLinkData.optJSONObject("operation_data") != null
                && deepLinkData.optJSONObject("operation_data").opt("claim") != null;
    }

    public static boolean isSale(JSONObject deepLinkData) {
        return deepLinkData.optInt("operation_type") == 1;
    }

    public static boolean isRefund(JSONObject deepLinkData) {
        return deepLinkData.optInt("operation_type") == 2;
    }
}
