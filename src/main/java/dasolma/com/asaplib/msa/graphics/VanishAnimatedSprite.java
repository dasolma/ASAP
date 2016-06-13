package dasolma.com.asaplib.msa.graphics;

/**
 * Created by dasolma on 18/04/15.
 */
public class VanishAnimatedSprite extends AnimatedSprite {
    double vanish_factor = 1;
    int vanish_w_count = 0;
    int vanish_h_count = 0;
    int tick = 0 ;

    public VanishAnimatedSprite(PatchSprite sprite, double x_offset, double y_offset, double w_scale, double h_scale, int num_sprites, AnimationType type, double vanish_factor) {
        super(sprite, x_offset, y_offset, w_scale, h_scale, num_sprites, type);

        this.vanish_factor = vanish_factor;
    }

    @Override
    public double getW_scale(int tick) {
        update_counts(tick);
        double w = super.getW_scale(tick) - super.getW_scale(tick)*vanish_factor*vanish_w_count;
        return w > 0? w: 0;
    }

    @Override
    public double getH_scale(int tick) {
        update_counts(tick);
        double h = super.getH_scale(tick) - super.getH_scale(tick)*vanish_factor*vanish_h_count;
        return h > 0 ? h : 0;
    }

    @Override
    public double getX_offset(int tick) {
        update_counts(tick);
        double offset = (super.getW_scale(tick)*vanish_factor*vanish_w_count) / 2;

        if ( super.getX_offset(tick) < 0.5 )
            return super.getX_offset(tick) + offset;
        else
            return super.getX_offset(tick) - offset;

    }

    @Override
    public double getY_offset(int tick) {
        update_counts(tick);
        double offset = (super.getH_scale(tick)*vanish_factor*vanish_h_count) / 2;

        if( super.getY_offset(tick) < 0.5 )
            return super.getY_offset(tick) + offset;
        else
            return super.getY_offset(tick) - offset;

    }

    protected void update_counts(int tick) {
        if( this.tick != tick ) {
            this.tick = tick;
            this.vanish_h_count++;
            this.vanish_w_count++;
        }
    }



}
