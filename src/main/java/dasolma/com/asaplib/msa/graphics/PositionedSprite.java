package dasolma.com.asaplib.msa.graphics;

/**
 * Created by dasolma on 5/04/15.
 */
public class PositionedSprite implements ISpriteGraphic {

    private double x_offset;
    private double y_offset;
    private double w_scale;
    private double h_scale;
    protected PatchSprite sprite;

    public PositionedSprite(PatchSprite sprite, double x_offset, double y_offset, double w_scale, double h_scale) {
        this.x_offset = x_offset;
        this.y_offset = y_offset;
        this.w_scale = w_scale;
        this.h_scale = h_scale;
        this.sprite = sprite;
    }

    public int getResourceId() { return sprite.getResourceId(); }

    public int getWidth(int tick) { return sprite.getWidth(); }

    public int getHeight(int tick) { return sprite.getHeight(); }

    public double getX_offset(int tick) { return x_offset; }

    public double getY_offset(int tick) { return y_offset; }

    public double getW_scale(int tick) { return w_scale; }

    public double getH_scale(int tick) { return h_scale; }

    public int getX(int tick) { return sprite.getX(); }

    public int getY(int tick) { return sprite.getY(); }
}
