package dasolma.com.asaplib.msa.graphics;

/**
 * Created by dasolma on 5/04/15.
 */
public class PatchSprite  implements ISpriteGraphic {
    private int resourceId;
    private int width;
    private int height;
    private int x;
    private int y;

    public PatchSprite(int resourceId, int x, int y, int width, int height) {
        this.resourceId = resourceId;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

    }

    public PatchSprite(int resourceId, int width, int height) {
        this.resourceId = resourceId;
        this.width = width;
        this.height = height;
        this.x = 0;
        this.y = 0;

    }

    public int getResourceId() { return resourceId; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public int getX() { return  x; }

    public int getY() { return y; }
}
