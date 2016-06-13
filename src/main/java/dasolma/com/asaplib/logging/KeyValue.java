package dasolma.com.asaplib.logging;

/**
 * Created by dasolma on 14/05/15.
 */
public class KeyValue {

    String key;
    Object value;

    public KeyValue(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {return key;}

    public Object getValue() {return value;}
}

