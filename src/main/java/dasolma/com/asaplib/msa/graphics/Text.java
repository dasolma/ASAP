package dasolma.com.asaplib.msa.graphics;

/**
 * Created by dasolma on 11/04/15.
 */
public class Text implements ISpriteGraphic {

    private String text;
    private float y;
    private float x;
    private int resource_id;
    private float scale = 2f;
    private int color = 0xffffffff;

    public Text(int resource_id, String text, float x, float y, float scale, int color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.resource_id = resource_id;
        this.scale = scale;
        this.color = color;
    }

    public String getText() {return text; }

    public float getX() { return x; }

    public float getY() { return y; }

    public int getResource_id() { return resource_id; }

    public float getScale() {
        return scale;
    }

    public int getColor() {
        return color;
    }
}
