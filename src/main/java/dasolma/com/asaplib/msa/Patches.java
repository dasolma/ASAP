package dasolma.com.asaplib.msa;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dasolma on 22/12/14.
 */
public class Patches extends ArrayList<Patch> {

    private int w;              //cols of the grid
    private int h;              //rows of the grid
    private int grid_size;      // w*h
    private int patch_size;     //cells size (w/h) of the grid
    private boolean isTorus = true;
    private Patch[][] grid;
    //properties
    private HashMap<String, Object[][]> properties = new HashMap<String, Object[][]>();


    public Patches(int w, int h, int patch_size) {
        this.w = w;
        this.h = h;
        this.grid_size = w * h;
        this.patch_size = patch_size;


        grid = new Patch[w][h];

        populate();

    }

    public Patches(int w, int h, int patch_size, boolean isTorus) {
        this.isTorus = isTorus;
        this.w = w;
        this.h = h;
        this.grid_size = w * h;
        this.patch_size = patch_size;


        grid = new Patch[w][h];

        populate();
    }


    /*
        Setup patch world from world parameters.
     */
    private void populate() {
        for(int x = 0; x < w; x++) {
            for(int y = 0; y < h; y++) {
                Patch np  = new Patch(y, x, x* getPatch_size(), y* getPatch_size(), getPatch_size(), Factory.getDefaultPatchColor(), this);
                this.add(np);
                grid[x][y] = np;

            }
        }

        set_neighbors();
    }

    private void set_neighbors() {
        for(Patch p: this) {
            int r = p.getRow();
            int c = p.getCol();

            // 1 2 3
            // 4   6
            // 7 8 9
            Patch n1 = getPatchSave(r-1,c-1);
            Patch n2 = getPatchSave(r-1,c);
            Patch n3 = getPatchSave(r-1,c+1);

            Patch n4 = getPatchSave(r,c-1);
            //Patch n5 = p
            Patch n6 = getPatchSave(r,c+1);
            Patch n7 = getPatchSave(r+1,c-1);
            Patch n8 = getPatchSave(r+1,c);
            Patch n9 = getPatchSave(r+1,c+1);

            p.setN4(new Patch[]{n2, n4, n6, n8});
            p.setN8(new Patch[]{n1, n2, n3, n4, n6, n7, n8, n9});

        }
    }

    private Patch getPatchSave(int r, int c) {
        if( !isTorus ) {
            if( r < 0 || r > h || c < 0 || c > w)
                return null;
        }

        if( r < 0 ) r = h - (r + 2);
        if( c < 0 ) c = w - (c + 2);
        r = r % h;
        c = c % w;

        return grid[c][r];
    }

    public Patch getPatch(int x, int y) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();

        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return getPatch(x, y, width, height);
    }

    public Patch getPatch(int x, int y, int width, int height) {

        int mwh = Math.min(Math.min(width, height), Math.max(width, height) / 2);

        mwh = height;

        double patch_size_w = (double)mwh / getWidth();
        double patch_size_h = (double)mwh / getHeight();

        //int col = x / (getPatch_size() +1);
        //int row = y / (getPatch_size() +1);

        int col = (int)(x / (patch_size_w));
        int row = (int)(y / (patch_size_h));


        if (col >= 0 && row >= 0 && col < w && row < h)
            return grid[col][row];

        return null;
    }

    public Patch getPatchByPos(int c, int r) {

        if (c >= 0 && r >= 0 && c < w && r < h)
            return grid[c][r];

        return null;

    }


    @Override
    public boolean add(Patch p) {
        if( this.size() >= grid_size) return false;

        return super.add(p);
    }

    @Override
    public void add(int index, Patch p) {
        if( this.size() >= grid_size) return;

        super.add(index, p);
    }


    @Override
    public boolean addAll(java.util.Collection<? extends Patch> collection) {
        if( this.size() + collection.size() >= grid_size) return false;

        return super.addAll(collection);
    }

    @Override
    public boolean addAll(int index, java.util.Collection<? extends Patch> collection) {
        if( this.size() + collection.size() >= grid_size) return false;

        return super.addAll(index, collection);
    }


    public int getPatch_size() {
        return patch_size;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }


    protected Object getProperty(Patch patch, String name, Object def) {
        if ( !properties.containsKey(name) ) return def;

        Object[][] pt =  properties.get(name);
        if ( pt != null )
            return pt[patch.getCol()][patch.getRow()];

        return def;
    }

    public void setProperty(Patch patch, String name, Object value) {
        if ( !properties.containsKey(name) )
            properties.put(name, new Object[w][h]);

        properties.get(name)[patch.getCol()][patch.getRow()] = value;

    }

    public void swapProperties(String p1, String p2) {
        Object[][] properties1 = properties.get(p1);
        Object[][] properties2 = properties.get(p2);

        properties.put(p1, properties2);
        properties.put(p2, properties1);

    }

    public void removeProperty(String name) {
        properties.remove(name);
    }

    @Override
    public Object clone() {
        return (Patches) super.clone();
    }


    public Object deepClone() {
        Patches clon = (Patches)super.clone();
        clon.grid = new Patch[w][h];
        clon.clear();

        for(int x = 0; x < w; x++) {
            for(int y = 0; y < h; y++) {
                Patch np  = (Patch)grid[x][y].clone();
                clon.add(np);

                clon.grid[x][y] = np;
                np.patches = clon;

            }
        }

        clon.set_neighbors();

        return clon;
    }
}
