package com.shtrih.tinyjavapostester.util;

import android.util.Pair;

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


    public static double getSumRefundFromData(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        if (isRefundService(deepLinkData)) {
            return getSumRefundServiceFromData(deepLinkData, order);
        } else if (isRefundTransaction(deepLinkData)) {
            return getSumRefundTransactionFromData(deepLinkData, order);
        } else if (isRefundByReason(deepLinkData)) {
            return getSumRefundByReasonFromData(deepLinkData, order);
        }

        return 0;
    }

    public static double getSumRefundTransactionFromData(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        return getRefundTransactionListFromData(deepLinkData, order).stream().mapToInt(item -> item.getSumWithoutChange()).sum();
    }

    public static List<TransactionHistoryItem> getRefundTransactionListFromData(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        List<Integer> transactionRefundIdList = AppUtil.convertToListInteger(deepLinkData.getJSONObject("operation_data").getJSONArray("transactions"));
        List<TransactionHistoryItem> resultList = new ArrayList<>();

        for (TransactionHistoryItem transactionHistoryItem : order.getPayHistory()) {
            if (transactionRefundIdList.contains(transactionHistoryItem.id)) {
                resultList.add(transactionHistoryItem);
            }
        }

        return resultList;
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

//    public static List<Double> getListServicePaymentByPartition(List<OrderResponse.Serv> services, double fullSum, double partSum) {
//        double rate = partSum / fullSum;
//        Integer lastIndexNotNullSum = getLastIndexNotZeroPrice(services);
//
//        List<Double> listResult = services.stream()
//                .map(serv -> Double.parseDouble(serv.getServCostD()))
//                .map(servCost -> servCost != 0 ? servCost * rate : 0d)
//                .map(servCost -> new BigDecimal(servCost).setScale(2, RoundingMode.FLOOR).doubleValue())
//                .collect(Collectors.toList());
//
//
//        if (lastIndexNotNullSum != null) {
//            double prepareResultCost = listResult.stream().reduce(0d, Double::sum);
//            double remain = partSum - prepareResultCost;
//
//            listResult.set(lastIndexNotNullSum, listResult.get(lastIndexNotNullSum) + remain);
//        }
//
//        return listResult;
//    }

    public static List<Pair<Long, Long>> getListPricePennyWithDiscountPennyByPartition(OrderResponse.Order order, long partSumPenny) {
        long fullSumPenny = order.getOrderAmountWithBenefits() * 100;

        double rate = ((double) partSumPenny) / ((double) fullSumPenny);
        Integer lastIndexNotNullPrice = getLastIndexNotZeroPrice(order.getServs());

        List<Pair<Long, Long>> listResult = order.getServs().stream()
                .map(serv -> {
                    double price = Double.parseDouble(serv.getServCost());
                    double discount = price - Double.parseDouble(serv.getServCostD());

                    return new Pair<Double, Double>(price, discount);
                })

                .map(pair -> {
                    double newPrice = 0d;
                    double newDiscount = 0d;

                    if (pair.first != 0) {
                        newPrice = pair.first * rate;
                    }

                    if (pair.second != 0) {
                        newDiscount = pair.second * rate;
                    }

                    return new Pair<Double, Double>(newPrice, newDiscount);
                })
                .map(pair -> new Pair<Double, Double>(floor(pair.first), floor(pair.second)))
                .map(pair -> new Pair<Long, Long>((long) (pair.first * 100), (long) (pair.second * 100)))
                .collect(Collectors.toList());

        if (lastIndexNotNullPrice != null) {
            long prepareResultPricePenny = listResult.stream().map(pair -> pair.first - pair.second).reduce(0L, Long::sum);

            long remainPenny = partSumPenny - prepareResultPricePenny;
            Pair<Long, Long> lastNotNullPricePair = listResult.get(lastIndexNotNullPrice);

            listResult.set(lastIndexNotNullPrice, new Pair<Long, Long>(lastNotNullPricePair.first + remainPenny, lastNotNullPricePair.second));
        }

        return listResult;
    }

    public static Double floor(double number) {
        return new BigDecimal(number).setScale(2, RoundingMode.FLOOR).doubleValue();
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
                && AppUtil.getSumSalePaymentFromDeepLink(deepLinkData) >= getOrderSum(order);

    }

    public static boolean isPartitionSale(JSONObject deepLinkData, OrderResponse.Order order) throws JSONException {
        return isSale(deepLinkData)
                && AppUtil.getSumSalePaymentFromDeepLink(deepLinkData) < getOrderSum(order);
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

    public static String getSubjectTypeName(Integer id) {
        switch (id) {
            case 1:
                return "Товар";
            case 2:
                return "Подакцизный товар";
            case 3:
                return "Работа";
            case 4:
                return "Услуга";
            case 5:
                return "Ставка азартной игры";
            case 6:
                return "Выигрыш азартной игры";
            case 7:
                return "Лотерейный билет";
            case 8:
                return "Выигрыш лотереи";
            case 9:
                return "Предоставление РИД";
            case 10:
                return "Платеж";
            case 11:
                return "Составной предмет расчета";
            case 12:
                return "Иной предмет расчета";
        }

        return null;
    }

    public static String getPaymentTypeName(Integer id) {
        switch (id) {
            case 1:
                return "Предоплата 100%";
            case 2:
                return "Частичная предоплата";
            case 3:
                return "Аванс";
            case 4:
                return "Полный расчет";
            case 5:
                //return "Частичный расчет и кредит";
                return "Частичный расчет";
            case 6:
                return "Передача в кредит";
            case 7:
                return "Оплата кредита";
        }

        return null;
    }
}
