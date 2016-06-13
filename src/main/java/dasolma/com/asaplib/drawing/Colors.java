package dasolma.com.asaplib.drawing;

/**
 * Created by dasolma on 19/12/14.
 */
public class Colors {

    public enum ColorEnum {
        BLACK,
        WHITE,
        RED,
        GREEN,
        BLUE,
        YELLOW,
        PURPLE,
        CIAN,
        ORANGE,
        DARK_GREEN,
        ARENA
    }

    private static float _colors[][] = {
            {0,0,0,0}, {1,1,1,0}, {1,0,0,0}, {0,1,0,0}, {0,0,1,0},
        {1,1,0,0}, {1,0,1,0}, {0,1,1,0},
        {1,0.5f,0, 0}, {0, 0.5f, 0, 0}, {1,0.8f, 0.5f, 0}
    };

    public static final int NUM_COLORS = 11;

    public static float[] getColor(int i) {
        if (i < 0 || i >= NUM_COLORS) return new float[] {0,0,0,0};

        return _colors[i];
    }

    public static float[] getColor(ColorEnum c) {
        return getColor(c.ordinal()).clone();
    }

    public static float[] lower(float[] c, float level) {
        return new float[] {c[0] - level, c[1] - level, c[2] - level, 0};
    }


}
