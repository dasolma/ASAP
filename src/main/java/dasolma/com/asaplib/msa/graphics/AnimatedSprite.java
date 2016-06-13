package dasolma.com.asaplib.msa.graphics;

import java.util.Random;

/**
 * Created by dasolma on 16/04/15.
 */
public class AnimatedSprite extends PositionedSprite {


    public enum AnimationType {
        Random,
        PingPong,
        Linear,
    };


    private static Random rnd = new Random();
    int count = 0;
    int num_sprites = 1;
    AnimationType type;
    int next = +1;


    public AnimatedSprite(PatchSprite sprite, double x_offset, double y_offset, double w_scale, double h_scale, int num_sprites, AnimationType type) {
       super(sprite, x_offset, y_offset, w_scale, h_scale);

       this.num_sprites = num_sprites;
       this.count = rnd.nextInt(num_sprites);
       if( type == AnimationType.Linear) count = -1;
       this.type = type;
    }

    @Override
    public int getWidth(int tick) {

        return sprite.getWidth() / num_sprites;
    }

    @Override
    public int getX(int tick) {
        switch ( type ) {
            case Linear:
                count++;
                break;
            case Random:
                count = rnd.nextInt(num_sprites);
                if (count >= num_sprites) count = 0;
                break;

            case PingPong:
                if (count >= num_sprites-1) next = -1;
                if( count <= 0) next = +1;
                count += next;

        }
        //Log.i("AnimateSprite", "" + (sprite.getWidth() / num_sprites) * count);
        return sprite.getX() + (sprite.getWidth() / num_sprites) * count;
    }



}
