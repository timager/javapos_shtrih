package com.shtrih.tinyjavapostester.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TransactionResponse {

    @SerializedName("header")
    @Expose
    private Header header;
    @SerializedName("body")
    @Expose
    private Boolean body;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Boolean getBody() {
        return body;
    }

    public void setBody(Boolean body) {
        this.body = body;
    }

}