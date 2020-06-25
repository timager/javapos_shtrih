package com.shtrih.tinyjavapostester.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Api {
    String API_PREFIX = "/api/v1/";

    @Headers({"Accept: application/json"})
    @POST(API_PREFIX + "payment/order")
    public Call<OrderResponse> getOrder(@Body OrderBody data);
}
