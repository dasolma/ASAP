package dasolma.com.asaplib.msa.graphics;

/**
 * Created by dasolma on 11/04/15.
 */
public class AnimatedText implements ISpriteGraphic {

    private String text;
    private float y_offset;
    private float x_offset;
    private int frame = 0;
    private int resource_id;
    private float scale = 2f;
    private int color = 0xffffffff;

    public AnimatedText(int resource_id, String text, float x_offset, float y_offset, float scale, int color) {
        this.text = text;
        this.x_offset = x_offset;
        this.y_offset = y_offset;
        this.resource_id = resource_id;
        this.scale = scale;
        this.color = color;
    }

    public String getText() {return text; }

    public float getY_offset(int tick) { frame++; return y_offset * frame; }

    public float getX_offset(int tick) { frame++; return x_offset * frame; }

    public int getResource_id() { return resource_id; }

    public float getScale() {
        return scale;
    }

    public int getColor() {
        return color;
    }
}
