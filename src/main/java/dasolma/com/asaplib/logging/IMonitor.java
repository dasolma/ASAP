package dasolma.com.asaplib.logging;

/**
 * Created by dasolma on 14/05/15.
 */
public interface IMonitor {

    public enum EnumWhen {
        Connect,
        Start,
        Finish,
        EachTick,
        Event
    }

    Object get();
    String getType();
    EnumWhen getWhen();
    String getEvent();
    ICondition getCondition();

}
