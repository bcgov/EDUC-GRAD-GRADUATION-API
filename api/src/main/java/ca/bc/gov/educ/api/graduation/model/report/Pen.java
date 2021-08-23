package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class Pen {
    private String pen;
    private Object entityID;

    public String getPen() {
        return pen;
    }

    public void setPen(String value) {
        this.pen = value;
    }

    public Object getEntityID() {
        return entityID;
    }

    public void setEntityID(Object value) {
        this.entityID = value;
    }
}
