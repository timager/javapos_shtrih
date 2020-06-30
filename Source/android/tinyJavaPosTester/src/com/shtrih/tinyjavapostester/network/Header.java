package com.shtrih.tinyjavapostester.network;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Header implements Serializable {

    @SerializedName("api")
    @Expose
    private String api;
    @SerializedName("dt")
    @Expose
    private String dt;
    @SerializedName("latency")
    @Expose
    private Integer latency;
    @SerializedName("route")
    @Expose
    private String route;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("errors")
    @Expose
    private List<Object> errors = null;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public Integer getLatency() {
        return latency;
    }

    public void setLatency(Integer latency) {
        this.latency = latency;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Object> getErrors() {
        return errors;
    }

    public void setErrors(List<Object> errors) {
        this.errors = errors;
    }
}