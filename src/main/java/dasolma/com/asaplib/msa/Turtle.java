package dasolma.com.asaplib.msa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import dasolma.com.asaplib.msa.graphics.ISpriteGraphic;

/**
 * Created by dasolma on 27/11/15.
 */
public class Turtle {

    private UUID id = UUID.randomUUID();
    private float x;
    private float y;
    private float heading;
    protected Patches patches;

    //properties
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    //image
    private ISpriteGraphic shape;

    //
    private boolean changed = true;

    private boolean hiddend = false;

    public void commit() {
        this.changed = false;
    }

    public boolean hasChanged() {
        return this.changed;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getHeading() {
        return heading;
    }

    public Object getProperty(String name, Object def) {

        if ( !properties.containsKey(name) ) return def;

        return properties.get(name);


        //return patches.getProperty(this, name, def);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
        //patches.setProperty(this, name, value);
    }

    public boolean getBoolean(String name) {

        if( properties.containsKey(name)) {
            Object obj = properties.get(name);

            if (obj != null) return (Boolean) obj;

        }
        return false;


    }

    public Turtle(float x, float y, int patch_size, float[] color, Patches patches, ISpriteGraphic shape) {
        this.x = this.x;
        this.y = y;

        this.setX(x);
        this.setY(y);
        this.patches  = patches;
        this.shape = shape;
    }

    public ISpriteGraphic getShape() {
        return shape;
    }
}
