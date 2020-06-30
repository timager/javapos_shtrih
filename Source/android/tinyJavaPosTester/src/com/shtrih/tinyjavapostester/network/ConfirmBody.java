package com.shtrih.tinyjavapostester.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfirmBody {

    @SerializedName("pack_uuid")
    @Expose
    private String packUuid;
    @SerializedName("message")
    @Expose
    private String message;

    public String getPackUuid() {
        return packUuid;
    }

    public void setPackUuid(String packUuid) {
        this.packUuid = packUuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}