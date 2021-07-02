package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Certificate {
    private String issued;
    private OrderType orderType;

    @JsonFormat(pattern="yyyy-MM-dd")
    public String getIssued() {
        return issued;
    }

    public void setIssued(String value) {
        this.issued = value;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType value) {
        this.orderType = value;
    }
}
