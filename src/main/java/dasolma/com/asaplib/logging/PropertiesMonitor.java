package dasolma.com.asaplib.logging;

import dasolma.com.asaplib.msa.Factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by dasolma on 14/05/15.
 */
public class PropertiesMonitor implements IMonitor {

    private String cls;
    private String[] properties;
    private Object instance = null;
    private String type;
    private EnumWhen when;
    private String event;
    private ICondition condition;

    public PropertiesMonitor(String type, String cls, String property, EnumWhen when) {
        this.cls = cls;
        this.properties = property.split(",");
        this.type = type;
        this.when = when;
    }

    public PropertiesMonitor(String type, String cls, String property, EnumWhen when, ICondition condition) {
        this(type, cls, property, when);
        this.condition = condition;
    }


    public PropertiesMonitor(String type, String cls, String property, EnumWhen when, String event) {
        this(type, cls, property, when);
        this.event = event;
    }


    public PropertiesMonitor(String type, String cls, String property, EnumWhen when, String event, ICondition condition) {
        this(type, cls, property, when);
        this.event = event;
        this.condition = condition;
    }

    public ICondition getCondition() { return condition; }

    public Object get() {

        //if( instance == null) {
        instance = Factory.getObject(cls);
        //}

        if( instance == null) {
            return null;
        }
        else {
            KeyValueList result = new KeyValueList();
            for( String property: properties) {
                String encoder = getEncoer(property);
                property = getProperty(property);


                Field field = null;
                try {
                    try {
                        field = instance.getClass().getDeclaredField(property);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                    Object value = null;
                    if ( field != null ) {

                        field.setAccessible(true);

                        value = field.get(instance);
                    }
                    else {
                        Method method = instance.getClass().getMethod("get"+capitalizeFirst(property), null);

                        value = method.invoke(instance, null);
                    }

                    if ( !encoder.isEmpty() ) {
                        IEncoder enconder = createEnconder(encoder);
                        if (enconder == null) {
                            value = "error: not found encoder constructor";
                        }
                        value = enconder.encode(value);
                    }

                    result.add(new KeyValue(property, value));

                } catch (Exception e) {
                    e.printStackTrace();
                    result.add(new KeyValue(property, "error: " + e.getMessage()));
                }

            }

            return result;


        }

    }

    private String getEncoer(String str) {
        String[] strSplit = str.split("\\|");
        if( strSplit.length > 1) return strSplit[1].trim();
        return "";
    }

    private String getProperty(String str) {
        String[] strSplit = str.split("\\|");
        if( strSplit.length > 1) return strSplit[0].trim();
        return str;
    }


    private String capitalizeFirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public String getType() {
        return type;
    }

    @Override
    public EnumWhen getWhen() {
        return when;
    }

    @Override
    public String getEvent() {
        return event;
    }


    public IEncoder createEnconder(String encoder) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class classToInvestigate = Class.forName(encoder);
        Constructor[] aClassConstructors = classToInvestigate.getDeclaredConstructors();
        for(Constructor c : aClassConstructors){
            return (IEncoder)c.newInstance();
        }

        return null;

    }
}
