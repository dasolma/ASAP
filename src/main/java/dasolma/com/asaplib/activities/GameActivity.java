package dasolma.com.asaplib.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import dasolma.com.asaplib.R;
import dasolma.com.asaplib.drawing.opengl.OpenGLDrawing;
import dasolma.com.asaplib.gamification.BaseGamification;
import dasolma.com.asaplib.gamification.AchievementInformation;
import dasolma.com.asaplib.gamification.InformationListener;
import dasolma.com.asaplib.gamification.MessageListener;
import dasolma.com.asaplib.msa.Factory;
import dasolma.com.asaplib.play.RealTimeMultiplayer;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dasolma on 19/12/14.
 */
public class GameActivity extends FragmentActivity {

    public final String TAG = "GameActivity";

    private OpenGLDrawing grid;
    protected BaseGamification game;
    private boolean showDebugMSG = false;
    private Activity context = this;
    private static List<InformationListener> createListener = new ArrayList<InformationListener>();
    private static List<InformationListener> finishListener = new ArrayList<InformationListener>();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateGame();
        Factory.addObject(game);
        Factory.addObject(game.getModels()[0]);


        onAfterGameCreated();

        if (!RealTimeMultiplayer.instance().isConnected())
            game.onReady();


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);


        //create the drawer
        grid = new OpenGLDrawing(this, null, game.useSprites(), game);

        LinearLayout gl_content = (LinearLayout)findViewById(Factory.getGlContentId());

        gl_content.addView(grid);

        android.support.v4.app.FragmentManager fragMan = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragTransaction = fragMan.beginTransaction();


        //FINISHED LISTENER
        game.setFinishedListenerListener( new InformationListener() {
            @Override
            public void info(Object source) {

                onFinishGame();
            }


        });


        game.setAchivementListener( new AchievementInformation() {
            @Override
            public void info(String message, int icon) {

                showAchievement(message, icon);
            }
        });

        game.setMessageListener(new MessageListener() {
            @Override
            public void info(MessageTypeEnum type, String message) {

                switch (type) {
                    case DEBUG:
                    case INFO:
                        if (showDebugMSG || type != MessageListener.MessageTypeEnum.DEBUG) {
                            toast(message, Toast.LENGTH_SHORT);
                        } else {
                            Log.i(TAG, message);
                        }
                        break;



                }

            }
        });

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {

                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

    }

    protected void onCreateGame() {
        game = BaseGamification.getGamification(Factory.getGamiticationId(), this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        //Here you can get the size!

        //updated_margins = true;
        game.updateSizes(grid);
    }


    private void showAchievement(String message, int icon) {


        context.runOnUiThread(new AchievementRunnableToast(message, icon) {
            @Override
            public void run() {

                //Log.i("Achi", "Achivement");

                // Inflating the layout for the toast
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_toast,
                        (ViewGroup) findViewById(R.id.toast_layout_root));

                // Typecasting and finding the image view in the inflated layout
                ImageView image = (ImageView) layout.findViewById(R.id.image);

                // set icon
                image.setImageResource(this.icon);

                // Typecasting and finding the view in the inflated layout
                TextView text = (TextView) layout.findViewById(R.id.text);


                // Setting the text to be displayed in the Toast
                text.setText(this.msg);

                // Creating the Toast
                Toast toast = new Toast(getApplicationContext());


                // Setting the duration of the Toast
                toast.setDuration(Toast.LENGTH_LONG);
                // Setting the Inflated Layout to the Toast
                toast.setView(layout);

                // Showing the Toast
                toast.show();

            }
        } );


    }


    private  class RunnableToast implements Runnable {
        private String msg;
        private int l;

        public RunnableToast(String msg, int l) {
            this.msg = msg;
            this.l = l;
        }

        public void run() {
            Toast.makeText(getApplication(), msg, l).show();
        }
    }


    private void toast(String msg, int l) {
        this.runOnUiThread( new RunnableToast(msg, l));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (grid != null)
            grid.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (grid != null)
            grid.onResume();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {

        game.onKeyDown(keyCode, event);

        return super.onKeyDown(keyCode, event);
    }



    private abstract class AchievementRunnableToast implements Runnable {

        protected String msg;
        protected int icon;

        public AchievementRunnableToast(String msg, int icon) {
            this.msg = msg;
            this.icon = icon;
        }

        public abstract  void run();
    }

    public static void seOnGameCreateListener( InformationListener listener) {
        createListener.add(listener);
    }

    protected void onAfterGameCreated() {
        for(InformationListener ml: createListener)
            ml.info(this);
    }

    public static void seOnFinishGameListener( InformationListener listener) {
        finishListener.add(listener);
    }

    protected void onFinishGame() {

        game.freeObjects();
        //Factory.removeObject(game);
        //Factory.removeObject(game.getModels()[0]);


        for(InformationListener ml: finishListener)
            ml.info(this);
    }
}