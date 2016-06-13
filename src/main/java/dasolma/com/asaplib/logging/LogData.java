package dasolma.com.asaplib.logging;

/**
 * Created by dasolma on 14/05/15.
 */
public class LogData {

    private static final String DEBUG_TAG = "Debug CLG" ;
    private final IMonitor monitor;
    String type;
    Object data;

    public LogData(String type, Object data, IMonitor monitor) {
        this.type = type;
        this.data = data;
        this.monitor = monitor;

        //Log.i(DEBUG_TAG, data.toString());


    }

    public String getType() {return type;}

    public Object getData() {return data;}

    public IMonitor getMonitor() { return monitor; }
}
