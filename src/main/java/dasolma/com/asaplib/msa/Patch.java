package dasolma.com.asaplib.msa;

import dasolma.com.asaplib.msa.graphics.ISpriteGraphic;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dasolma on 22/12/14.
 *
 * Represent a rectangle on a grid. The set of all patches is the world on which the agents live
 * and the model runs.
 */

public class Patch implements Cloneable {

    private int row;
    private int col;
    protected Patches patches;

    //patch position on the grid
    private int x;
    private int y;

    private int patch_size;

    //neighbors list
    private Patch[] n4;
    private Patch[] n8;

    // The patch color
    private float[] color;


    //properties
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    //image
    private ArrayList<ISpriteGraphic> sprites = new ArrayList<ISpriteGraphic>();

    //
    private boolean changed = true;

    Patch(int row, int col, int x, int y, int patch_size, float[] color, Patches patches) {
        this.row = row;
        this.col = col;

        this.setX(x);
        this.setY(y);
        this.patch_size = patch_size;
        this.patches  = patches;


        this.color = color;
    }

    public Patches getPatches() { return patches; }

    public int getX() {
        return x;
    }

    void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    void setY(int y) {
        this.y = y;
    }

    public Patch[] getN4() {
        return n4;
    }

    void setN4(Patch[] n4) {
        this.n4 = n4;
    }

    public Patch[] getN8() {
        return n8.clone();
    }

    void setN8(Patch[] n8) {
        this.n8 = n8;
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {

        this.color = color;

        this.changed = true;
    }

    public void commit() {
        this.changed = false;
    }

    public boolean hasChanged() {
        return this.changed;
    }


    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }




    public void removeProperty(String name) {
        properties.remove(name);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public boolean hasProperty(String name) { return properties.containsKey(name); }

    public ArrayList<ISpriteGraphic> getSprites() { return sprites; }

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

    public int getManhattanDistance(Patch p) {
        return Math.abs(p.col - col) + Math.abs(p.row - row);
    }

    @Override
    public Object clone() {
        try {
            Patch p =  (Patch)super.clone();
            p.properties = (HashMap<String, Object> ) properties.clone();
            for( String key: p.properties.keySet())
            {
                Cloneable obj = null;

                if ( p.properties.get(key) instanceof Cloneable)
                    obj = (Cloneable)p.properties.get(key);

                if ( obj != null ) {
                    try {
                        p.properties.put(key, obj.getClass().getMethod("clone").invoke(obj));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            p.sprites = (ArrayList<ISpriteGraphic>)sprites.clone();

            return p;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Patch getNeighbor(DirectionEnum direction) {
        switch (direction) {
            case LEFT:
                return n4[1];
            case RIGH:
                return n4[2];
            case UP:
                return n4[0];
            case DOWN:
                return n4[3];
        }

        return null;
    }


    public enum DirectionEnum {
        LEFT,
        RIGH,
        UP,
        NONE, DOWN
    }
}
