package dasolma.com.asaplib.drawing.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import dasolma.com.asaplib.drawing.IDrawer;
import dasolma.com.asaplib.drawing.opengl.spritebatcher.Drawer;
import dasolma.com.asaplib.drawing.opengl.spritebatcher.SpriteBatcher;
import dasolma.com.asaplib.gamification.BaseGamification;
import dasolma.com.asaplib.msa.Model;
import dasolma.com.asaplib.msa.Patch;
import dasolma.com.asaplib.msa.Patches;
import dasolma.com.asaplib.msa.Turtle;
import dasolma.com.asaplib.msa.graphics.AnimatedText;
import dasolma.com.asaplib.msa.graphics.ISpriteGraphic;
import dasolma.com.asaplib.msa.graphics.PatchSprite;
import dasolma.com.asaplib.msa.graphics.PositionedSprite;
import dasolma.com.asaplib.msa.graphics.Text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dasolma on 22/12/14.
 */
public class OpenGLDrawing extends GLSurfaceView implements IDrawer {

    private final String TAG = "OpenGLDrawing";
    private double patchSize;

    //private List<Model> models = new ArrayList<Model>();
    private BaseGamification gami;
    /* Screen width and height */
    private int screenWidth, screenHeight;
    private float distance;
    private MyRenderer renderer;
    private int pointers = 0;
    private int xLastPos = 0;
    private int yLastPos = 0;
    private int xStartMove = 0;
    private int yStartMove = 0;
    private boolean touchingPatches = false;
    private Patch lastTouchPatch  = null;
    private boolean doScrooll = false;
    private SpriteBatcher sb;

    final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            Log.i(TAG, "Double Tap");
            int x = (int)e.getX();
            int y = (int)e.getY();

            patchTouch(x, y, Patch.DirectionEnum.NONE);
            //touchingPatches = true;
            doScrooll = true;

            return true;

        }

        @Override
        public void onLongPress(MotionEvent e) {

        }
    };


    final GestureDetector detector = new GestureDetector(listener);

    //opengl vars
    public static FloatBuffer vertexBuffer;
    public static FloatBuffer colorBuffer;

    protected static float vertices[];

    protected static float colors[] = {};


    public void addGamification(BaseGamification gami) {
        this.gami = gami;
    }

    /*
    public void addModel(Model model) {
        if(models.size() < 2)
        models.add(model);


    }
    */

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public double getPatchSize() { return this.patchSize; }

    private final class MyRenderer implements Renderer, Drawer {
        /* Stage width and height */
        protected float w, h;
        private float zoom = 1f;
        private float yoffset = 0;
        private float xoffset = 0;
        private int tick = 0;


        public final void onDrawFrame(GL10 gl) {
            gl.glClear(GLES10.GL_COLOR_BUFFER_BIT);


            DrawGrid(gl, null);
        }

        public int getYOffset() {
            return (int)(yoffset / (h / screenHeight));
        }

        public int getXOffset() {
            return (int)(xoffset  / (w / screenWidth));
        }

        @Override
        public boolean willDraw() {
            //return lastIteration < gami.getModels()[0].getIteration();
            return true;
        }


        //draw a grid usin the model.getPatches information
        private void DrawGrid(GL10 gl, SpriteBatcher sb) {

            tick++;

            //todo: possible improving: http://www.learnopengles.com/android-lesson-seven-an-introduction-to-vertex-buffer-objects-vbos/

            if( gami != null && gami.getModels().length > 0 ) {
                int count = 0;
                int offset = 0;

                Patches ref_grid = gami.getModels()[0].getPatches();
                List<Turtle> turtles = gami.getModels()[0].getTurtles();

                //patch size calculation
                PatchesSizeCalculator patchesSize = null;
                if (sb == null ) patchesSize = new PatchesSizeCalculator(ref_grid, zoom, w, h).calculate();
                else patchesSize = new PatchesSizeCalculator(ref_grid, zoom, sb.getViewWidth(), sb.getViewHeight()).calculate();
                double dgl_point_size = patchesSize.getDgl_point_size();
                double patch_size = patchesSize.getPatch_size();
                int gl_point_size = (int)dgl_point_size;



                //backgroud
                if( sb != null ) {
                    this.w = sb.getViewWidth();
                    this.h = sb.getViewHeight();
                    screenHeight = (int)h;
                    screenWidth = (int)w;
                    if( xLastPos == 0) {
                        xLastPos = (int)w /2;
                        yLastPos = (int)h/2;
                    }

                    if ( gami.getBackGroundResourceId() > 0 ) {
                        //sb.draw(R.drawable.world, new Rect(0,0, 1000, 460), new Rect(0,0, (int)(patch_size * ref_grid.getWidth()), (int)(patch_size * ref_grid.getHeight())));
                        sb.draw(gami.getBackGroundResourceId(), new Rect(0, 0, 1000, 460), new Rect(0, 0, sb.getViewWidth(), sb.getViewHeight()));

                        sb.batchDraw(gl);
                    }
                }


                //Log.i(TAG, "zoom:" +  zoom);
                //Log.i(TAG, "w:" + w + ", h:" + h);

                //set position and color patches
                synchronized (this) {

                    for (Model model : gami.getModels()) {
                        ArrayList<Patch> patches = (ArrayList<Patch>)model.getPatches().clone();

                        count = renderPatches(sb, count, offset, patch_size, patches);

                        patches = (ArrayList<Patch>)gami.getTouchPatches();

                        count += renderPatches(sb, count, offset, patch_size, patches);

                        renderTurtles(sb, count, offset, patch_size, turtles);

                        //multi panel
                        offset += patch_size * model.getPatches().getWidth() + gami.getModels().length*patch_size;


                    }

                    if( sb == null ) {
                        //copy de buffers

                        if (vertexBuffer != null) {
                            vertexBuffer.position(0);
                            vertexBuffer.put(Arrays.copyOfRange(vertices, 0, count * 3));
                            vertexBuffer.position(0);

                        }

                        if (colorBuffer != null) {
                            colorBuffer.position(0);
                            colorBuffer.put(Arrays.copyOfRange(colors, 0, count * 4));
                            colorBuffer.position(0);

                        }


                        //Log.i(TAG, "zoom: " + zoom);


                        //draw
                        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
                        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

                        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, OpenGLDrawing.vertexBuffer);
                        gl.glDrawArrays(GL10.GL_POINTS, 0, count * 3);
                        gl.glDrawArrays(GL10.GL_COLOR_ARRAY, 0, count * 4);
                        //gl.glEnable(GL10.GL_POINT_SIZE);

                        //gl.glPointSize(gami.getModels()[0].getPatches().getPatch_size());
                        gl.glPointSize(gl_point_size);

                        // Disable the vertices buffer.
                        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

                        //gl.glFrustumf(-ratio/zoom, ratio/zoom, -1/zoom, 1/zoom, 1, 25);
                    }
                    else {
                        sb.batchDraw(gl);

                    }
                }
            }
        }

        private int renderTurtles(SpriteBatcher sb, int count, int offset, double patch_size, List<Turtle> turtles) {
            double x;
            double y;
            if ( turtles != null ) {
                for (Turtle t : turtles) {

                    if (t.hasChanged()) {

                        x = ((t.getX() * patch_size) + xoffset + offset);
                        y = ((t.getY() * patch_size) + yoffset);

                        if (sb == null) {



                        } else {
                            ISpriteGraphic is = (ISpriteGraphic) t.getShape();


                            if (is instanceof PositionedSprite) {

                                PositionedSprite ps = (PositionedSprite) is;

                                if (ps != null) {
                                    double xs = (x + (patch_size * ps.getX_offset(tick)));
                                    double ys = (y + (patch_size * ps.getY_offset(tick)));
                                    double w = ((patch_size * ps.getW_scale(tick)));
                                    double h = ((patch_size * ps.getH_scale(tick)));



                                    int psx = ps.getX(tick);
                                    int psy = ps.getY(tick);
                                    sb.draw(ps.getResourceId(), new Rect(psx, psy, psx + ps.getWidth(tick), psy + ps.getHeight(tick)),
                                            new Rect((int) xs, (int) ys, (int) (xs + w), (int) (ys + h)));
                                }

                            } else if (is instanceof PatchSprite) {

                                PatchSprite ps = (PatchSprite) is;

                                if (ps != null) {
                                    double xs = x;
                                    double ys = y;
                                    double w = patch_size;
                                    double h = patch_size;


                                    sb.draw(ps.getResourceId(), new Rect(ps.getX(), ps.getY(), ps.getX()+ ps.getWidth(),  ps.getY()+ ps.getHeight()),
                                            new Rect((int) xs, (int) ys, (int) (xs + w), (int) (ys + h)));
                                }

                            } else if (is instanceof AnimatedText) {
                                AnimatedText at = (AnimatedText) is;

                                double xs = (x + (patch_size * at.getX_offset(tick)));
                                double ys = (y + (patch_size * at.getY_offset(tick)));


                                sb.drawText(at.getResource_id(), at.getText(), (int) xs, (int) ys, at.getScale(), at.getColor());
                            } else if (is instanceof Text) {
                                Text at = (Text) is;

                                double xs = (x + (patch_size * at.getX()));
                                double ys = (y + (patch_size * at.getY()));


                                sb.drawText(at.getResource_id(), at.getText(), (int) xs, (int) ys, at.getScale(), at.getColor());
                            }


                        }

                    }

                }
            }
            return count;

        }

        private int renderPatches(SpriteBatcher sb, int count, int offset, double patch_size, ArrayList<Patch> patches) {
            double x;
            double y;
            if ( patches != null ) {
                for (Patch p : patches) {

                    if (p.hasChanged()) {

                        //x = p.getX() + offset;
                        //y = p.getY();
                        x = ((p.getCol() * patch_size) + xoffset + offset);
                        y = ((p.getRow() * patch_size) + yoffset);
                        //Log.i(TAG, "Point (" + x + "," + y + ")");


                        if (sb == null) {


                            vertices[count * 3] = (int) x;
                            vertices[count * 3 + 1] = (int) y;
                            vertices[count * 3 + 2] = 0;


                            count++;

                            float[] color = p.getColor();

                            colors[count * 4] = color[0];
                            colors[count * 4 + 1] = color[1];
                            colors[count * 4 + 2] = color[2];
                            colors[count * 4 + 3] = 0;

                        } else {
                            List<ISpriteGraphic> sprites = (List<ISpriteGraphic>) p.getSprites().clone();
                            for (ISpriteGraphic is : sprites) {

                                if (is instanceof PositionedSprite) {

                                    PositionedSprite ps = (PositionedSprite) is;

                                    if (ps != null) {
                                        double xs = (x + (patch_size * ps.getX_offset(tick)));
                                        double ys = (y + (patch_size * ps.getY_offset(tick)));
                                        double w = ((patch_size * ps.getW_scale(tick)));
                                        double h = ((patch_size * ps.getH_scale(tick)));



                                        int psx = ps.getX(tick);
                                        int psy = ps.getY(tick);
                                        sb.draw(ps.getResourceId(), new Rect(psx, psy, psx + ps.getWidth(tick), psy + ps.getHeight(tick)),
                                                new Rect((int) xs, (int) ys, (int) (xs + w), (int) (ys + h)));
                                    }

                                } else if (is instanceof PatchSprite) {

                                    PatchSprite ps = (PatchSprite) is;

                                    if (ps != null) {
                                        double xs = x;
                                        double ys = y;
                                        double w = patch_size;
                                        double h = patch_size;


                                        sb.draw(ps.getResourceId(), new Rect(ps.getX(), ps.getY(), ps.getX()+ ps.getWidth(),  ps.getY()+ ps.getHeight()),
                                                new Rect((int) xs, (int) ys, (int) (xs + w), (int) (ys + h)));
                                    }

                                } else if (is instanceof AnimatedText) {
                                    AnimatedText at = (AnimatedText) is;

                                    double xs = (x + (patch_size * at.getX_offset(tick)));
                                    double ys = (y + (patch_size * at.getY_offset(tick)));


                                    sb.drawText(at.getResource_id(), at.getText(), (int) xs, (int) ys, at.getScale(), at.getColor());
                                } else if (is instanceof Text) {
                                    Text at = (Text) is;

                                    double xs = (x + (patch_size * at.getX()));
                                    double ys = (y + (patch_size * at.getY()));


                                    sb.drawText(at.getResource_id(), at.getText(), (int) xs, (int) ys, at.getScale(), at.getColor());
                                }

                            }
                        }

                    }

                }
            }
            return count;

        }


        public final void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glClearColor(0, 0, 0, 1);


            if (width > height) {
                h = 600;
                w = width * h / height;
            } else {
                w = 600;
                h = height * w / width;
            }
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glOrthof(0, w, h, 0, -1, 1);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();


            screenWidth = width;
            screenHeight = height;
            xLastPos = width/2;
            yLastPos = height/2;
            gl.glViewport(0, 0, screenWidth, screenHeight);


        }

        public final void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // Set up alpha blending
            //gl.glEnable(GL10.GL_ALPHA_TEST);
            //gl.glEnable(GL10.GL_BLEND);
            //gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
            // We are in 2D. Who needs depth?
            gl.glDisable(GL10.GL_DEPTH_TEST);
            // Enable vertex arrays (we'll use them to draw primitives).
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            // Enable texture coordination arrays.
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);


        }

        public final void zoom(float mult){
            float z = zoom * mult;

            if( z < 1 || Float.isInfinite(z)) return;
            zoom = z;

            //Log.i(TAG, "New zoom: " + zoom);
        }

        public final void scroll(int x, int y) {
            xoffset += (x * (w / screenWidth));
            yoffset += (y * (h /screenHeight));


            validateScroll();


        }

        private void validateScroll() {
            int numModels = gami.getModels().length;
            int wh  = (int)Math.min(Math.min(w, h), Math.max(w, h) / numModels);
            double patch_size = ((double)wh / gami.getModels()[0].getPatches().getWidth()) * zoom;

            int grid_w = (int)(numModels*patch_size * gami.getModels()[0].getPatches().getWidth() + numModels*patch_size);
            int grid_h = (int)(patch_size * gami.getModels()[0].getPatches().getHeight());


            xoffset = Math.max(w - grid_w, xoffset);
            yoffset = Math.max(h - grid_h, yoffset);


            xoffset = Math.min(0, xoffset);
            yoffset = Math.min(0, yoffset);

            //Log.i(TAG, String.format("Offset (%s,%s)", xoffset, yoffset));
        }


        @Override
        public void onDrawFrame(GL10 gl, SpriteBatcher spriteBatcher) {
            DrawGrid(gl, spriteBatcher);
        }


    }

    public PatchesSizeCalculator getPatchesSizeCalculator(int w, int h) {
        return new PatchesSizeCalculator(gami.getModels()[0].getPatches(), 1,
                w, h).calculate();
    }

    public class PatchesSizeCalculator {
        private Patches ref_grid;
        private double patch_size;
        private double dgl_point_size;
        private double zoom;
        private double w, h;


        public PatchesSizeCalculator(Patches ref_grid, double zoom, double w, double h) {
            this.ref_grid = ref_grid;
            this.zoom = zoom;
            this.w = w;
            this.h = h;
        }

        public double getPatch_size() {
            return patch_size;
        }

        public double getDgl_point_size() {
            return dgl_point_size;
        }

        public PatchesSizeCalculator calculate() {
            double fw = ((double)w) / ref_grid.getWidth();
            double fh = ((double)h) / ref_grid.getHeight();
            patch_size = 0;
            dgl_point_size = 0;



            if( fw * ref_grid.getHeight() >   h ) {

                patch_size = fh * zoom;
                dgl_point_size = (((double)screenHeight) / ref_grid.getHeight()) * zoom;
            }
            else {
                patch_size = fw * zoom;
                dgl_point_size = (((double)screenWidth) / ref_grid.getWidth()) * zoom;
            }

            return this;

        }
    }

    public OpenGLDrawing(Context context, AttributeSet attrs) {
        this(context, attrs, true, null);
    }

    public OpenGLDrawing(Context context, AttributeSet attrs, boolean spriteBatcher, BaseGamification game) {
        super(context, attrs);
        this.gami = game;

        int size = 500*500;
        vertices = new float[size*3];
        renderer = new MyRenderer();
        this.getHolder().setFormat( PixelFormat.RGBA_8888);
        this.getHolder().setFormat( PixelFormat.TRANSLUCENT );
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setZOrderOnTop(true);

        if( !spriteBatcher ) {
            setRenderer(renderer);

            ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            vertexBuffer = vbb.asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);


            colors = new float[size * 4];
            ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
            cbb.order(ByteOrder.nativeOrder());
            colorBuffer = cbb.asFloatBuffer();
            colorBuffer.put(colors);
            colorBuffer.position(0);
        }
        else {
            sb = new SpriteBatcher(context, gami.getSprites(), renderer);
            //sb.setFontParams(R.string.font, new FontParams().size(10));
            sb.setMaxFPS(4);
            renderer.w = sb.getViewWidth();
            renderer.h = sb.getViewHeight();

            setRenderer(sb);

            PatchesSizeCalculator patchesSize = new
                    PatchesSizeCalculator(game.getModels()[0].getPatches(), 0,
                    sb.getViewWidth(), sb.getViewHeight()).calculate();

            this.patchSize =  patchesSize.getPatch_size();
        }




        detector.setOnDoubleTapListener(listener);
        //detector.setIsLongpressEnabled(true);

    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean r = detector.onTouchEvent(event);
        touchingPatches = true;


        //Log.i(TAG, "Consumed: " + r);
        if ( !r ) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    //touchingPatches = false;
                    doScrooll = false;
                    lastTouchPatch = null;
                    patchUp((int)event.getX(),(int)event.getY(),
                            getDirection((int)event.getX(), (int)event.getY(),
                                    xStartMove, yStartMove));
                    Log.i(TAG, "ACTION_UP");
                    return true;
                case MotionEvent.ACTION_DOWN:
                    pointers = 1;
                    xLastPos = (int) event.getX();
                    yLastPos = (int) event.getY();
                    xStartMove = xLastPos;
                    yStartMove = yLastPos;
                    Log.i(TAG, "ACTION_DOWN");
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    pointers = 2;
                    distance = fingerDist(event);

                    Log.i(TAG, "Distance: " + distance);
                    return true;
                case MotionEvent.ACTION_MOVE:

                    if (pointers == 2) {
                        float newDist = fingerDist(event);
                        float d = newDist / distance;

                        if (d > 0) {
                            renderer.zoom(d);
                            distance = newDist;
                        }
                    } else {

                        int x = (int) event.getX();
                        int y = (int) event.getY();

                        if( !doScrooll )
                            patchTouch(x,y, getDirection(x, y, xStartMove, yStartMove));
                        else
                            doScroll(x, y);
                    }
                    return true;
                default:
                    return false;

            }

        }
        else {
            xLastPos = (int) event.getX();
            yLastPos = (int) event.getY();
        }

        return r;

    }

    private Patch.DirectionEnum getDirection(int x, int y, int xStartMove, int yStartMove) {
        int size = 0;
        Patch.DirectionEnum direction = Patch.DirectionEnum.NONE;

        if( Math.abs(x-xStartMove) > Math.abs(y-yStartMove) ) {
            size = Math.abs(x-xStartMove);
            if ( x > xStartMove ) direction = Patch.DirectionEnum.RIGH;
            else direction = Patch.DirectionEnum.LEFT;
        }
        else {
            size = Math.abs(y-yStartMove);
            if ( y > yStartMove ) direction = Patch.DirectionEnum.DOWN;
            else direction = Patch.DirectionEnum.UP;
        }

        //Log.i("GL", "Size: " + size);

        if ( size < (float)patchSize * 0.8 )
            direction = Patch.DirectionEnum.NONE;

        return direction;
    }

    private void doScroll(int x, int y) {
        //Log.i(TAG, "SCROLL");
        renderer.scroll(x - xLastPos, y - yLastPos);

        xLastPos = x;
        yLastPos = y;
    }

    private void patchUp(int x, int y, Patch.DirectionEnum direction) {
        int numModels  = gami.getModels().length;

        Patches ref_grid = gami.getModels()[0].getPatches();

        PatchesSizeCalculator patchesSize = new PatchesSizeCalculator(ref_grid, renderer.zoom,
                renderer.w, renderer.h).calculate();
        double patch_size = patchesSize.getDgl_point_size();

        //Log.i(TAG, "Patch size: " + patch_size);
        //Log.i(TAG, "Touch (" + x + "," + y + ") - Screen: ("+ screenWidth + "," + screenHeight + ")" );

        int offset = 0;
        for(Model model: gami.getModels()) {
            //Log.i(TAG, String.format("Pos: (%s,%s) - offset (%s,%s)", x, y, renderer.getXOffset(), renderer.getYOffset()));

            int r = (int)( (y - renderer.getYOffset())  / patch_size );
            int c = (int)( ((x - offset - renderer.getXOffset()) / patch_size)  );

            //Patch p = model.getPatches().getPatch(x - offset, y, screenWidth, screenHeight);
            Patch p  = model.getPatches().getPatchByPos(c, r);
            if (p != null) {
                if ( lastTouchPatch == p && touchingPatches) break;
                lastTouchPatch = p;
                gami.touchUp(model, p, direction);
            }

            //Log.i(TAG, "offset: " + offset);
        }
    }

    private void patchTouch(int x, int y, Patch.DirectionEnum direction) {
        int numModels  = gami.getModels().length;

        Patches ref_grid = gami.getModels()[0].getPatches();

        PatchesSizeCalculator patchesSize = new PatchesSizeCalculator(ref_grid, renderer.zoom,
                renderer.w, renderer.h).calculate();
        double patch_size = patchesSize.getDgl_point_size();

        //Log.i(TAG, "Patch size: " + patch_size);
        //Log.i(TAG, "Touch (" + x + "," + y + ") - Screen: ("+ screenWidth + "," + screenHeight + ")" );

        int offset = 0;
        for(Model model: gami.getModels()) {


            int r = (int)( (y - renderer.getYOffset())  / patch_size );
            int c = (int)( ((x - offset - renderer.getXOffset()) / patch_size)  );

            //Patch p = model.getPatches().getPatch(x - offset, y, screenWidth, screenHeight);
            Patch p  = model.getPatches().getPatchByPos(c, r);
            if (p != null) {
                if ( lastTouchPatch == p && touchingPatches) break;
                lastTouchPatch = p;
                gami.touch(model, p, direction);
            }

            offset += patch_size * model.getPatches().getWidth() + numModels*patch_size;

            //Log.i(TAG, "offset: " + offset);
        }
    }

    protected final float fingerDist(MotionEvent event){
        if ( event.getPointerCount() > 1 ) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return FloatMath.sqrt(x * x + y * y);

        }
        return 0;
    }

}