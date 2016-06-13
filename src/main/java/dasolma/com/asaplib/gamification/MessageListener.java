package dasolma.com.asaplib.gamification;

/**
 * Created by dasolma on 4/01/15.
 */
public abstract class MessageListener {



    public enum MessageTypeEnum {
        DEBUG,
        INFO,
        ACTION
    }

    public abstract void info(MessageTypeEnum type, String message);
}
