package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Certificate {
    private Date issued;
    private String certStyle;
    private OrderType orderType;
    private boolean isOrigin;

    @JsonFormat(pattern="yyyy-MM-dd")
    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date value) {
        this.issued = value;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType value) {
        this.orderType = value;
    }

    public boolean getIsOrigin() {
        return isOrigin;
    }

    public void setIsOrigin(boolean origin) {
        isOrigin = origin;
    }

    public String getCertStyle() {
        return certStyle;
    }

    public void setCertStyle(String certStyle) {
        this.certStyle = certStyle;
    }
}
