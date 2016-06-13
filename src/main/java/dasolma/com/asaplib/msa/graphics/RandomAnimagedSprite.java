package dasolma.com.asaplib.msa.graphics;

import java.util.Random;

/**
 * Created by dasolma on 7/04/15.
 */
public class RandomAnimagedSprite extends PositionedSprite {

    private double random_factor;
    private static Random rnd = new Random();
    public RandomAnimagedSprite(PatchSprite sprite,
                                double x_offset, double y_offset,
                                double w_scale, double h_scale,
                                double random_factor) {
        super(sprite, x_offset, y_offset, w_scale, h_scale);

        this.random_factor = random_factor;
    }

    @Override
    public double getX_offset(int tick) { return super.getX_offset(tick) +
            (super.getX_offset(tick) * rnd.nextDouble() * random_factor); }

    @Override
    public double getY_offset(int tick) {  return super.getY_offset(tick) +
            (super.getY_offset(tick) * rnd.nextDouble() * random_factor);}


}
