package cognitionmodel.models.inverted;

public class Point{
    String field;
    Object value;
    BitAgent agent;

    public Point(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    public String toString(){
        return field+":"+value.toString();
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public BitAgent getAgent() {
        return agent;
    }
}