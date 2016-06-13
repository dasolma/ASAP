package dasolma.com.asaplib.play;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by dasolma on 16/04/15.
 */
public class Player {

    private final static int REQUEST_LEADERBOARD = 10011;
    private final static int REQUEST_ACHIEVEMENTS = 1012;

    private static HashMap<String, Achievement> achievements = new HashMap<String, Achievement>();
    private static Context context;

    public static void loadAchievements(Context context) {
        Player.context = context;

        Thread th = new Thread( new Runnable() {
            @Override
            public void run() {
                _loadAchievements();
            }
        });

        th.start();
    }

    private static void _loadAchievements()  {

        boolean fullLoad = false;  // set to 'true' to reload all achievements (ignoring cache)
        long waitTime = 60;    // seconds to wait for achievements to load before timing out

        // load achievements
        PendingResult p = Games.Achievements.load( GoogleAPI.instance().getClient(), fullLoad );
        Achievements.LoadAchievementsResult r =
                (Achievements.LoadAchievementsResult)p.await( waitTime, TimeUnit.SECONDS );
        int status = r.getStatus().getStatusCode();
        if ( status != GamesStatusCodes.STATUS_OK )  {
            r.release();
            return;           // Error Occured
        }

        // cache the loaded achievements
        AchievementBuffer buf = r.getAchievements();
        int bufSize = buf.getCount();
        for ( int i = 0; i < bufSize; i++ )  {
            com.google.android.gms.games.achievement.Achievement ach = buf.get(i);
            String id = ach.getAchievementId();

            // here you now have access to the achievement's data
              // the achievement ID string
            boolean unlocked = ach.getState() ==
                    com.google.android.gms.games.achievement.Achievement.STATE_UNLOCKED;  // is unlocked
            boolean incremental = ach.getType() ==
                    com.google.android.gms.games.achievement.Achievement.TYPE_INCREMENTAL;  // is incremental
            int steps = 0;
            if ( incremental ) {
                steps = ach.getCurrentSteps();  // current incremental steps

            }

            Achievement myach = new Achievement(unlocked, incremental, steps);
            achievements.put(id, myach);

        }
        buf.close();
        r.release();
    }

    public static boolean isUnlocked(int id) {
        String sid = context.getResources().getString(id);
        Achievement achi = achievements.get(sid);
        if ( achi != null ) return achi.isUnlocked();
        return false;
    }

    public static int getSteps(int id) {
        String sid = context.getResources().getString(id);
        boolean incremental = achievements.get(sid).isIncremental();

        if ( incremental )
            return achievements.get(sid).steps();  // current incremental steps

        return 0;
    }

    public static  void unlock(int achievement) {
        String id = context.getResources().getString(achievement);
        Games.Achievements.unlock(GoogleAPI.instance().getClient(),
                id);

        if ( achievements.get(id) != null )
            achievements.get(id).setUnlocked();

    }

    public static int increment(int achievement, int count) {
        String id = context.getResources().getString(achievement);
        Games.Achievements.increment(GoogleAPI.instance().getClient(),
                id, count);

        if ( achievements.get(id) != null )
            return achievements.get(id).increment(count);

        return count;

    }

    private static class Achievement {
        private boolean unlocked;
        private boolean incremental;
        private int steps;

        public Achievement(boolean unlocked, boolean incremental, int steps) {
            this.unlocked = unlocked;
            this.steps = steps;
            this.incremental = incremental;
        }

        public boolean isUnlocked() { return unlocked; }

        public int steps() { return steps; }

        public boolean isIncremental() { return incremental; }

        public void setUnlocked() {
            this.unlocked = true;
        }

        public int increment(int count) { return ++this.steps;  }
    }

    public static void showLeaderBoard(Activity mainActivity) {
        mainActivity.startActivityForResult(
                Games.Leaderboards.getAllLeaderboardsIntent(GoogleAPI.instance().getClient()),
                REQUEST_LEADERBOARD);
    }

    public static void showAchievement(Activity mainActivity) {
        mainActivity.startActivityForResult(
                Games.Achievements.getAchievementsIntent(GoogleAPI.instance().getClient()),
                REQUEST_ACHIEVEMENTS);
    }
}
