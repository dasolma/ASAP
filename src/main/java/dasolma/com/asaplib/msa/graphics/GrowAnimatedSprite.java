package dasolma.com.asaplib.msa.graphics;

/**
 * Created by dasolma on 18/04/15.
 */
public class GrowAnimatedSprite extends VanishAnimatedSprite {
    public GrowAnimatedSprite(PatchSprite sprite, double x_offset, double y_offset, double w_scale, double h_scale, int num_sprites, AnimationType type, double vanish_factor) {
        super(sprite, x_offset, y_offset, w_scale, h_scale, num_sprites, type, vanish_factor);

        this.vanish_h_count = 8;
        this.vanish_w_count = 8;
    }


    protected void update_counts(int tick) {
        if( this.tick != tick ) {
            this.tick = tick;
            this.vanish_h_count = Math.max(0, --this.vanish_h_count);
            this.vanish_w_count = Math.max(0, --this.vanish_w_count);

        }
    }
}

