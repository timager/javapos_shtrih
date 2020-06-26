package com.shtrih.tinyjavapostester.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class OrderResponse implements Serializable {

    @SerializedName("header")
    @Expose
    private Meta meta;
    @SerializedName("body")
    @Expose
    private Order order;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public class Order implements Serializable {

        @SerializedName("order_id")
        @Expose
        private Integer orderId;
        @SerializedName("order_number")
        @Expose
        private String orderNumber;
        @SerializedName("operation_type")
        @Expose
        private String operationType;
        @SerializedName("order_amount")
        @Expose
        private Integer orderAmount;
        @SerializedName("order_discount")
        @Expose
        private Integer orderDiscount;
        @SerializedName("order_discount_percent")
        @Expose
        private Integer orderDiscountPercent;
        @SerializedName("order_discount_ballance")
        @Expose
        private Integer orderDiscountBallance;
        @SerializedName("order_amount_with_benefits")
        @Expose
        private Integer orderAmountWithBenefits;
        @SerializedName("order_advanced")
        @Expose
        private Integer orderAdvanced;
        @SerializedName("order_dbt")
        @Expose
        private Integer orderDbt;
        @SerializedName("order_currency")
        @Expose
        private String orderCurrency;
        @SerializedName("order_main_amount")
        @Expose
        private Integer orderMainAmount;
        @SerializedName("order_dop_amount")
        @Expose
        private String orderDopAmount;
        @SerializedName("order_rec_advance")
        @Expose
        private String orderRecAdvance;
        @SerializedName("order_ready_date")
        @Expose
        private Object orderReadyDate;
        @SerializedName("doc_name")
        @Expose
        private String docName;
        @SerializedName("doc_number")
        @Expose
        private String docNumber;
        @SerializedName("patient_email")
        @Expose
        private String patientEmail;
        @SerializedName("patient_phone")
        @Expose
        private String patientPhone;
        @SerializedName("servs")
        @Expose
        private List<Serv> servs = null;
        @SerializedName("Pay_History")
        @Expose
        private List<Object> payHistory = null;

        public Integer getOrderId() {
            return orderId;
        }

        public void setOrderId(Integer orderId) {
            this.orderId = orderId;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public String getOperationType() {
            return operationType;
        }

        public void setOperationType(String operationType) {
            this.operationType = operationType;
        }

        public Integer getOrderAmount() {
            return orderAmount;
        }

        public void setOrderAmount(Integer orderAmount) {
            this.orderAmount = orderAmount;
        }

        public Integer getOrderDiscount() {
            return orderDiscount;
        }

        public void setOrderDiscount(Integer orderDiscount) {
            this.orderDiscount = orderDiscount;
        }

        public Integer getOrderDiscountPercent() {
            return orderDiscountPercent;
        }

        public void setOrderDiscountPercent(Integer orderDiscountPercent) {
            this.orderDiscountPercent = orderDiscountPercent;
        }

        public Integer getOrderDiscountBallance() {
            return orderDiscountBallance;
        }

        public void setOrderDiscountBallance(Integer orderDiscountBallance) {
            this.orderDiscountBallance = orderDiscountBallance;
        }

        public Integer getOrderAmountWithBenefits() {
            return orderAmountWithBenefits;
        }

        public void setOrderAmountWithBenefits(Integer orderAmountWithBenefits) {
            this.orderAmountWithBenefits = orderAmountWithBenefits;
        }

        public Integer getOrderAdvanced() {
            return orderAdvanced;
        }

        public void setOrderAdvanced(Integer orderAdvanced) {
            this.orderAdvanced = orderAdvanced;
        }

        public Integer getOrderDbt() {
            return orderDbt;
        }

        public void setOrderDbt(Integer orderDbt) {
            this.orderDbt = orderDbt;
        }

        public String getOrderCurrency() {
            return orderCurrency;
        }

        public void setOrderCurrency(String orderCurrency) {
            this.orderCurrency = orderCurrency;
        }

        public Integer getOrderMainAmount() {
            return orderMainAmount;
        }

        public void setOrderMainAmount(Integer orderMainAmount) {
            this.orderMainAmount = orderMainAmount;
        }

        public String getOrderDopAmount() {
            return orderDopAmount;
        }

        public void setOrderDopAmount(String orderDopAmount) {
            this.orderDopAmount = orderDopAmount;
        }

        public String getOrderRecAdvance() {
            return orderRecAdvance;
        }

        public void setOrderRecAdvance(String orderRecAdvance) {
            this.orderRecAdvance = orderRecAdvance;
        }

        public Object getOrderReadyDate() {
            return orderReadyDate;
        }

        public void setOrderReadyDate(Object orderReadyDate) {
            this.orderReadyDate = orderReadyDate;
        }

        public String getDocName() {
            return docName;
        }

        public void setDocName(String docName) {
            this.docName = docName;
        }

        public String getDocNumber() {
            return docNumber;
        }

        public void setDocNumber(String docNumber) {
            this.docNumber = docNumber;
        }

        public String getPatientEmail() {
            return patientEmail;
        }

        public void setPatientEmail(String patientEmail) {
            this.patientEmail = patientEmail;
        }

        public String getPatientPhone() {
            return patientPhone;
        }

        public void setPatientPhone(String patientPhone) {
            this.patientPhone = patientPhone;
        }

        public List<Serv> getServs() {
            return servs;
        }

        public void setServs(List<Serv> servs) {
            this.servs = servs;
        }

        public List<Object> getPayHistory() {
            return payHistory;
        }

        public void setPayHistory(List<Object> payHistory) {
            this.payHistory = payHistory;
        }

    }

    private class Meta implements Serializable {

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

    public class Serv implements Serializable {

        @SerializedName("serv_id")
        @Expose
        private Integer servId;
        @SerializedName("depart")
        @Expose
        private Integer depart;
        @SerializedName("serv_tax")
        @Expose
        private Integer servTax;
        @SerializedName("serv_quantity")
        @Expose
        private Integer servQuantity;
        @SerializedName("serv_code")
        @Expose
        private String servCode;
        @SerializedName("serv_name")
        @Expose
        private String servName;
        @SerializedName("serv_cost")
        @Expose
        private String servCost;
        @SerializedName("serv_cost_d")
        @Expose
        private String servCostD;
        @SerializedName("benefits_amount")
        @Expose
        private Integer benefitsAmount;
        @SerializedName("serv_payed")
        @Expose
        private Integer servPayed;
        @SerializedName("prev_tran")
        @Expose
        private List<Object> prevTran = null;
        @SerializedName("priority_id")
        @Expose
        private Integer priorityId;

        public Integer getServId() {
            return servId;
        }

        public void setServId(Integer servId) {
            this.servId = servId;
        }

        public Integer getDepart() {
            return depart;
        }

        public void setDepart(Integer depart) {
            this.depart = depart;
        }

        public Integer getServTax() {
            return servTax;
        }

        public void setServTax(Integer servTax) {
            this.servTax = servTax;
        }

        public Integer getServQuantity() {
            return servQuantity;
        }

        public void setServQuantity(Integer servQuantity) {
            this.servQuantity = servQuantity;
        }

        public String getServCode() {
            return servCode;
        }

        public void setServCode(String servCode) {
            this.servCode = servCode;
        }

        public String getServName() {
            return servName;
        }

        public void setServName(String servName) {
            this.servName = servName;
        }

        public String getServCost() {
            return servCost;
        }

        public void setServCost(String servCost) {
            this.servCost = servCost;
        }

        public String getServCostD() {
            return servCostD;
        }

        public void setServCostD(String servCostD) {
            this.servCostD = servCostD;
        }

        public Integer getBenefitsAmount() {
            return benefitsAmount;
        }

        public void setBenefitsAmount(Integer benefitsAmount) {
            this.benefitsAmount = benefitsAmount;
        }

        public Integer getServPayed() {
            return servPayed;
        }

        public void setServPayed(Integer servPayed) {
            this.servPayed = servPayed;
        }

        public List<Object> getPrevTran() {
            return prevTran;
        }

        public void setPrevTran(List<Object> prevTran) {
            this.prevTran = prevTran;
        }

        public Integer getPriorityId() {
            return priorityId;
        }

        public void setPriorityId(Integer priorityId) {
            this.priorityId = priorityId;
        }

    }
}