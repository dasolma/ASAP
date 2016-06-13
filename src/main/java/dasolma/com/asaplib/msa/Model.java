package dasolma.com.asaplib.msa;

import android.util.Log;

import dasolma.com.asaplib.gamification.InformationListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by dasolma on 23/12/14.
 */
public abstract class Model {

    private Thread thread;
    private long msWaitBetweenTick = 0;
    protected Patches patches;
    protected List<Turtle> turtles = new ArrayList<Turtle>();
    protected boolean canStopped = true;
    private boolean running = false;
    private boolean stop = false;
    protected boolean destroy = false;
    private String id;
    private static HashMap<String, Model> models = new HashMap<String, Model>();
    private List<TouchListener> touchListeners = new ArrayList<TouchListener>();
    private final String TAG = "Gamification.Model";
    private double minTickMs = 0;
    private Lock runningLock = new ReentrantLock();
    protected int iteration = 0;
    protected boolean external_tick_control = true;
    protected boolean updating =false;

    private List<InformationListener> tickListerner = new ArrayList<InformationListener>();

    public Model(String id) {
        this.id = id;

        models.put(id, this);
    }

    public static Model getModel(String id) {
        return models.get(id);
    }

    public Patches getPatches() {
        return patches;
    }

    public List<Turtle> getTurtles() { return turtles; }

    public void setup(Patches patches) {
        this.patches = patches;

    }

    public abstract void process_tick();

    public abstract android.support.v4.app.Fragment getInfoFragment(int player);

    public abstract android.support.v4.app.Fragment getPointFragment(String opoid, boolean inverse);

    public void setTimeBetweenTicks(long time) {
        if( time > 0 )
            msWaitBetweenTick = time;
    }

    public void faster(int ms) {
        msWaitBetweenTick -= ms;
        if( msWaitBetweenTick < 0 ) msWaitBetweenTick = 0;
    }

    public void slower(int ms) {

        msWaitBetweenTick += ms;
        if( msWaitBetweenTick < 0 ) msWaitBetweenTick = 0;
    }

    public void run() {

        thread = new Thread() {

            @Override
            public synchronized void run() {


                while (!destroy) {
                    try {
                        //Log.i(TAG, "Sleep time: " + msWaitBetweenTick);
                        if( msWaitBetweenTick > 0 ) Thread.sleep(msWaitBetweenTick);


                        if( running && !external_tick_control ) {
                            tick();

                        }
                        else {
                            Thread.sleep((long)minTickMs);
                        }


                        if (stop) {
                            stop = false;
                            running = false;

                        }




                    } catch (InterruptedException e) {
                    }

                }
            }
        };

        thread.start();
    }

    public synchronized void tick()  {

        if( running ) {
            //Log.i(TAG, String.format("Start Tick %d", iteration));
            double startms = System.currentTimeMillis();
            synchronized (this.patches) {
                updating = true;
                process_tick();
                updating = false;
            }
            //Log.i(TAG, String.format("Finish Tick %d", iteration));
            double finishms = System.currentTimeMillis();

            if (finishms - startms < minTickMs && !stop) {
                long ms = (long) (minTickMs - (finishms - startms));

                if (ms > 0) {
                    //Log.i(TAG, "Sleep ms: " + ms );
                    try {
                        Thread.sleep(ms);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            iteration++;

            onAfterListener();
        }
    }

    private void waitThread() {
        try {
            thread.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void pause() {
        if( canStopped && running ) {

            stop = true;
            try {
                Thread.sleep((long)minTickMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void goFordward(int ticks) {
        if( !running ) {
            for(int i = 0; i < ticks; i++)
                process_tick();
        }
        else {
            Log.i(TAG, "Impossiblo go fordward. Running");
        }
    }

    public synchronized void resume() {
        running = true;
    }

    public int touch(Patch p, float[] color) {
        for(TouchListener l: touchListeners)
            l.onTouch(p);

        return 0;
    }

    public boolean isRunning() {
        //return thread.getState() == Thread.State.RUNNABLE;
        return running;
    }

    public boolean canStopped() {
        return canStopped;
    }

    public void setCanStopped(boolean s) {
        canStopped = s;
    }

    public String getId() {
        return id;
    }

    public void setTickListerner(InformationListener listener) {
        tickListerner.add(listener);
    }

    protected void onAfterListener() {
        for(InformationListener info: tickListerner)
            info.info(this);
    }

    public void setTouchListener( TouchListener l) {
        touchListeners.add(l);
    }

    public void setMinTickMs(double ms) {
        minTickMs = ms;
    }

    public double getMinTickMs() {
        return minTickMs;
    }


    public int getIteration() {
        return iteration;
    }

    public void setExternal_tick_control( boolean value ) {
        this.external_tick_control = value;
    }

    public abstract int[] getSprites();

    public boolean isUpdating() { return updating; }

    public void removeListerners() {
        tickListerner.clear();
    }


}
