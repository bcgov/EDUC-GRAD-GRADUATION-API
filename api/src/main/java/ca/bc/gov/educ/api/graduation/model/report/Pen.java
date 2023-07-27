package ca.bc.gov.educ.api.graduation.model.report;

import java.io.Serializable;
import java.util.Objects;

public class Pen implements Comparable<Pen>, Serializable {

    private static final long serialVersionUID = 2L;

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

    @Override
    public int compareTo(Pen o) {
        return pen.compareTo(o.pen);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pen other = (Pen) o;
        return Objects.equals(pen, other.pen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pen);
    }
}
