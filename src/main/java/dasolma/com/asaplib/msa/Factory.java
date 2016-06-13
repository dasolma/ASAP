package dasolma.com.asaplib.msa;

import android.app.Activity;

import dasolma.com.asaplib.drawing.Colors;
import dasolma.com.asaplib.gamification.BaseGamification;
import dasolma.com.asaplib.logging.ADAPServerLogger;
import dasolma.com.asaplib.logging.DeviceNotExistCondition;
import dasolma.com.asaplib.logging.ILogger;
import dasolma.com.asaplib.logging.IMonitor;
import dasolma.com.asaplib.logging.PropertiesMonitor;
import dasolma.com.asaplib.logging.UserNotExistCondition;
import dasolma.com.asaplib.play.GoogleAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dasolma on 22/12/14.
 */
public class Factory {

    private static Random rnd = new Random();
    private static List<Object> _objects = new ArrayList<Object>();
    private static BaseGamification gamification;
    private static GoogleAPI googleAPI;
    private static ADAPServerLogger logger;
    private static Activity ctx;
    private static int glContentId;
    private static int gamiticationId;

    public static float[] getDefaultPatchColor() {
        //return new float[]{0,1,0};
        int ic = rnd.nextInt(Colors.NUM_COLORS);
        return  Colors.getColor(ic);
    }

    public static void addObject(Object obj) {
        _objects.add(obj);

        if ( obj instanceof BaseGamification)
            gamification = (BaseGamification)obj;

        if ( obj instanceof GoogleAPI)
            googleAPI = (GoogleAPI)obj;

    }

    public static void removeObject(Object obj) {
        _objects.remove(obj);
    }

    public static void setContext(Activity ctx) {
        Factory.ctx = ctx;
    }

    public static Activity getContext() { return ctx;}


    public static Object getObject(String className) {
        try {
            Class<?> cls = Class.forName(className);


            for( Object obj: _objects) {
                if( cls.isInstance(obj) ) {
                    return obj;
                }
            }

            return null;

        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static BaseGamification getGamification() {
        return gamification;
    }

    public static GoogleAPI getGooglePAI() {
        return googleAPI;
    }

    public static ILogger getLogger() { return logger; }

    public static void createLogger(Activity ctx) {
        Factory.setContext(ctx);
        if ( logger == null) {

            ArrayList<IMonitor> monitors = new ArrayList<IMonitor>();
            PropertiesMonitor monitor = new PropertiesMonitor("inits",
                    "dasolma.com.asaplib.models.ConwayLifeGameV2.ConwayLifeGame",
                    "alivePatches|dasolma.com.asaplib.logging.PatchesEncoding,location,ip,gameId,playerId,userId,deviceId,iteration,mode",
                    IMonitor.EnumWhen.Start);
            monitors.add(monitor);

            monitor = new PropertiesMonitor("inits",
                    "dasolma.com.asaplib.models.ConwayLifeGameV2.Bot",
                    "alivePatches|dasolma.com.asaplib.logging.PatchesEncoding,location,ip,gameId,playerId,userId,deviceId,iteration,mode",
                    IMonitor.EnumWhen.Start);
            monitors.add(monitor);

            monitor = new PropertiesMonitor("interactions",
                    "dasolma.com.asaplib.models.ConwayLifeGameV2.ConwayLifeGame",
                    "changedPatches|dasolma.com.asaplib.logging.PatchesEncoding,iteration,gameId,playerId,userId,deviceId",
                    IMonitor.EnumWhen.Event, "commitPatches");
            monitors.add(monitor);

            monitor = new PropertiesMonitor("interactions",
                    "dasolma.com.asaplib.models.ConwayLifeGameV2.Bot",
                    "changedPatches|dasolma.com.asaplib.logging.PatchesEncoding,iteration,gameId,playerId,userId,deviceId",
                    IMonitor.EnumWhen.Event, "botCommitPatches");
            monitors.add(monitor);

            monitor = new PropertiesMonitor("users",
                    "dasolma.com.asaplib.user.User",
                    "name,nickname,id,gender,userEnterGender,userEnterAge",
                    IMonitor.EnumWhen.Connect,
                    new UserNotExistCondition());

            monitors.add(monitor);

            monitor = new PropertiesMonitor("devices",
                    "dasolma.com.asaplib.user.Device",
                    "osVersion,device,MAC,sdk,product,model,screenWidth,screenHeight,id,densityDpi",
                    IMonitor.EnumWhen.Connect,
                    new DeviceNotExistCondition());
            monitors.add(monitor);

            monitor = new PropertiesMonitor("endings",
                    "dasolma.com.asaplib.models.ConwayLifeGameV2.ConwayLifeGame",
                    "iteration,myBornedPatches,myDeathPatches,murderPatches,oponentsPatches,livingPatches,patchesToChange,opoPatchesToChange,gameId,playerId,userId,deviceId,mode",
                    IMonitor.EnumWhen.Finish);
            monitors.add(monitor);

            logger = new ADAPServerLogger(monitors);
        }
    }

    public static int getGamiticationId() {
        return gamiticationId;
    }

    public static void setGamiticationId(int gamiticationId) {
        Factory.gamiticationId = gamiticationId;
    }

    public static int getGlContentId() {
        return glContentId;
    }

    public static void setGlContentId(int glContentId) {
        Factory.glContentId = glContentId;
    }
}
