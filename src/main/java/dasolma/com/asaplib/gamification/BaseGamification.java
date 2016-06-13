package dasolma.com.asaplib.gamification;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import dasolma.com.asaplib.activities.GameActivity;
import dasolma.com.asaplib.drawing.opengl.OpenGLDrawing;
import dasolma.com.asaplib.msa.Model;
import dasolma.com.asaplib.msa.Patch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dasolma on 30/12/14.
 */
public abstract class BaseGamification {

    protected int eachInformationTick = 1;
    protected boolean _useSprites = false;

    List<InformationListener> readyListerners = new ArrayList<InformationListener>();
    List<InformationListener> tickListerners = new ArrayList<InformationListener>();
    List<MessageListener> messageListeners = new ArrayList<MessageListener>();
    List<EventListener> eventsListeners = new ArrayList<EventListener>();
    List<InformationListener> finishedListener = new ArrayList<InformationListener>();
    List<AchievementInformation> achivementListener = new ArrayList<AchievementInformation>();
    protected int backGroundResourceId = -1;

    public void setAchivementListener( AchievementInformation info) {
        achivementListener.add(info);
    }

    public void setReadyListener( InformationListener info) {
        readyListerners.add(info);
    }

    public void setTickListener( InformationListener info) {
        tickListerners.add(info);
    }

    public void setFinishedListenerListener( InformationListener info) {
        finishedListener.add(info);
    }

    protected void onAfterTick() {
        for(InformationListener info: tickListerners)
            info.info(this);
    }

    public void onReady() {
        for(InformationListener info: readyListerners)
            info.info(this);
    }

    protected void onFinishedListener() {
        for(InformationListener info: finishedListener)
            info.info(this);
    }

    public void setMessageListener( MessageListener listener) {
        messageListeners.add(listener);
    }

    public void setEventListener( EventListener listener) {
        eventsListeners.add(listener);
    }

    protected void onEvent(String event) {
        for(EventListener ml: eventsListeners)
            ml.info(event);
    }

    protected void onMessage(MessageListener.MessageTypeEnum type, String message) {
        for(MessageListener ml: messageListeners)
            ml.info(type, message);
    }

    protected void onAchivementInfo(String message, int icon) {
        for(AchievementInformation ai : achivementListener)
            ai.info(message, icon);
    }

    public abstract Model[] getModels();

    public boolean useSprites() { return _useSprites; }

    public static BaseGamification getGamification(int resId, GameActivity activity) {
        try {
            Class classToInvestigate = Class.forName(activity.getResources().getString(resId));
            Constructor[] aClassConstructors = classToInvestigate.getDeclaredConstructors();
            for(Constructor c : aClassConstructors){
                return (BaseGamification)c.newInstance(activity);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void onKeyDown(int keyCode, KeyEvent event) {

    }


    public int getBackGroundResourceId() {
        return backGroundResourceId;
    }

    public void touch(Model m, Patch p, Patch.DirectionEnum direction) {

    }

    public int[] getSprites() {
        return new int[0];
    }


    public List<Patch> getTouchPatches() {
        return null;
    }


    public void updateSizes(OpenGLDrawing grid) {

    }


    public void freeObjects() {

    }

    public int calculate_patch_size() {

        int patch_size =0 ;
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();

        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        /*
        int mwh = Math.min(Math.min(width, height), Math.max(width, height) / 2);

        return (int)Math.floor(mwh / grid_width);
        */

        double min = Math.min(width, height);
        double max = Math.max(width, height);
        double p = (double)min / max;


        if( p > 0.6)
            return 4;

        return 5;

    }

    public void touchUp(Model model, Patch p, Patch.DirectionEnum direction) {

    }
}
