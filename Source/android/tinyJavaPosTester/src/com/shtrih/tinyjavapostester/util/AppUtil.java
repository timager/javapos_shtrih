package com.shtrih.tinyjavapostester.util;

import com.shtrih.tinyjavapostester.network.OrderResponse;

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

    public static double getSumPaymentFromDeepLink(JSONObject deepLinkData) throws JSONException {
        JSONObject operationData = deepLinkData.getJSONObject("operation_data");

        double paymentCash = operationData.getDouble("payment_cash");
        double paymentCard = operationData.getDouble("payment_card");

        return paymentCash + paymentCard;
    }

    public static List<Double> getListServicePaymentByPartition(List<OrderResponse.Serv> services, double fullSum, double partSum) {
        Double rate = partSum / fullSum;
        Integer lastIndexNotNullSum = getLastIndexNotZeroPrice(services);

        List<Double> listResult = services.stream()
                .map(serv -> Double.parseDouble(serv.getServCostD()))
                .map(servCost -> servCost != 0 ? servCost * rate : 0d)
                .map(servCost -> new BigDecimal(servCost).setScale(2, RoundingMode.FLOOR).doubleValue())
                .collect(Collectors.toList());


        if (lastIndexNotNullSum != null) {
            Double prepareResultCost = listResult.stream().reduce(0d, Double::sum);
            Double remain = partSum - prepareResultCost;

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
}
