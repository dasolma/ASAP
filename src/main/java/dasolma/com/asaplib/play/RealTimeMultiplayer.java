package dasolma.com.asaplib.play;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import dasolma.com.asaplib.R;
import dasolma.com.asaplib.activities.GameActivity;
import dasolma.com.asaplib.io.Bytes;
import dasolma.com.asaplib.msa.Factory;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

/**
 * Created by dasolma on 29/12/14.
 */
public class RealTimeMultiplayer implements RoomUpdateListener, RealTimeMessageReceivedListener, OnInvitationReceivedListener {

    private static RealTimeMultiplayer instance;

    private FragmentActivity activity;
    private String mRoomId;
    private String oponentId;
    private String playerId;
    private String mIncomingInvitationId;
    private ProgressDialog progressDialog;

    private List<DataReceiverListener> receiversListeners = new ArrayList<DataReceiverListener>();
    private List<LeaveRoomListener> leaveRoomListeners = new ArrayList<LeaveRoomListener>();

    // request code for the "select players" UI
    // can be any number as long as it's unique
    public static final byte FINISH_PLAY = (byte)255;
    public static final byte OPPONENT_FINISH_PLAY = (byte)254;
    public final static int RC_SELECT_PLAYERS = 10000;

    // arbitrary request code for the waiting room UI.
    // This can be any integer that's unique in your Activity.
    public final static int RC_WAITING_ROOM = 10002;

    // request code (can be any number, as long as it's unique)
    public final static int RC_INVITATION_INBOX = 10001;


    private final String TAG = "RealTimeMultiplayer";
    private Room room;

    private RealTimeMultiplayer() {

    }

    public static RealTimeMultiplayer instance() {
        if( instance == null ) {
            instance = new RealTimeMultiplayer();
            Factory.addObject(instance());
        }

        return instance;
    }

    public void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }

    public void startQuickGame(FragmentActivity activity) {
        progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.loading));


        this.activity = activity;

        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(GoogleAPI.instance().getClient(), roomConfig);



        // prevent screen from sleeping during handshake
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this);
                //.setRoomStatusUpdateListener(this);
    }

    public void invitePlayers(FragmentActivity activity) {

        this.activity = activity;

        // launch the player selection screen
        // minimum: 1 other player; maximum: 3 other players
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(
                GoogleAPI.instance().getClient(), 1, 1);
        activity.startActivityForResult(intent, RC_SELECT_PLAYERS);

    }


    public void connectInvitation(Invitation inv) {

        progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.loading));

        // accept invitation
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setInvitationIdToAccept(inv.getInvitationId());
        Games.RealTimeMultiplayer.join(GoogleAPI.instance().getClient(),
                roomConfigBuilder.build());


        // prevent screen from sleeping during handshake
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen
    }

    public void onSelectPlayers(Intent data) {

        // get the invitee list
        //Bundle extras = data.getExtras();
        final ArrayList<String> invitees =
                data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

        // get auto-match criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers =
                data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers =
                data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

        if (minAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
        } else {
            autoMatchCriteria = null;
        }

        // create the room and specify a variant if appropriate
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.addPlayersToInvite(invitees);
        if (autoMatchCriteria != null) {
            roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        RoomConfig roomConfig = roomConfigBuilder.build();
        Games.RealTimeMultiplayer.create(GoogleAPI.instance().getClient(), roomConfig);

        // prevent screen from sleeping during handshake
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onCancelRoom() {
        // Waiting room was dismissed with the back button. The meaning of this
        // action is up to the game. You may choose to leave the room and cancel the
        // match, or do something else like minimize the waiting room and
        // continue to connect in the background.

        // in this example, we take the simple approach and just leave the room:
        /*
        Games.RealTimeMultiplayer.leave(GoogleAPI.instance().getClient(),
                this, mRoomId);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        */
        if( progressDialog != null )
            progressDialog.dismiss();
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.

            return;
        }


        this.room = room;
        mRoomId = room.getRoomId();
        GoogleApiClient client = GoogleAPI.instance().getClient();

        // get waiting room intent
        dimissLoading();
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(client,
                room, Integer.MAX_VALUE);
        activity.startActivityForResult(i, RC_WAITING_ROOM);

    }

    private void dimissLoading() {
        if(progressDialog != null) progressDialog.dismiss();
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.

            return;
        }


        // get waiting room intent
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(GoogleAPI.instance().getClient(),
                room, Integer.MAX_VALUE);
        activity.startActivityForResult(i, RC_WAITING_ROOM);



    }

    @Override
    public void onLeftRoom(int i, String s) {
        // left room. Ready to start or join another room.
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.
        }
        else {

            this.room = room;
            mRoomId = room.getRoomId();
            GoogleApiClient client = GoogleAPI.instance().getClient();
            String myid =  room.getParticipantId(Games.Players.getCurrentPlayerId(client));

            playerId = myid;

            for (String id : room.getParticipantIds() ){
                if ( id != myid) this.oponentId = id;
            }

            startGame();
        }
    }

    public void startGame() {
        if( progressDialog != null ) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        Intent i = new Intent(activity, GameActivity.class);
        activity.startActivity(i);
    }


    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        // get real-time message
        byte[] data  = rtm.getMessageData();
        try {
            data = Bytes.decompress(data);

            //Log.i(TAG, "Data 0:" + data[0]);

            if( data[0] == FINISH_PLAY) {
                onLeftRoomListener();
            }
            else {
                for (DataReceiverListener listener : receiversListeners)
                    listener.dataReceived(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataFormatException e) {
            e.printStackTrace();
        }

        // process message


    }

    private void onLeftRoomListener() {
        for (LeaveRoomListener listener : leaveRoomListeners)
            listener.leaveRoom(OPPONENT_FINISH_PLAY);
    }

    public void setDataReceiversListeners( DataReceiverListener listener ) {
        receiversListeners.add(listener);
    }

    public void setLeaveRoomListeners( LeaveRoomListener listener ) {
        leaveRoomListeners.add(listener);
    }

    public void clearListerners() {
        receiversListeners.clear();
        leaveRoomListeners.clear();
    }

    public void sendData(byte[] data) {
        if ( mRoomId != null ) {
            int i = 0;

            //Log.i(TAG, "Sending data type:" + data[0] + ", " + data.length + " bytes");

            data = Bytes.compress(data);

            int max = Multiplayer.MAX_RELIABLE_MESSAGE_LEN;

            while (i < data.length) {

                int numBytes = Math.min(data.length - i, max);
                byte[] bytes = new byte[numBytes];

                System.arraycopy(data, i, bytes, 0, numBytes);

                try {
                    int tokenId = Games.RealTimeMultiplayer.sendReliableMessage(
                            GoogleAPI.instance().getClient(),
                            new com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer.ReliableMessageSentCallback() {
                                @Override
                                public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                                    if (statusCode != GamesStatusCodes.STATUS_OK)
                                        Log.i(TAG, "Error sending msg! code: " + statusCode);

                                    if (statusCode == GamesStatusCodes.STATUS_PARTICIPANT_NOT_CONNECTED ||
                                            statusCode == GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED) {
                                        onLeftRoomListener();
                                    }
                                }
                            },
                            bytes, mRoomId, this.oponentId);


                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                i += numBytes;

            }
        }

    }

    public void LeftRoom() {
        if ( mRoomId != null ) {
            Games.RealTimeMultiplayer.leave(GoogleAPI.instance().getClient(),
                    this, mRoomId);

            mRoomId = null;
        }

        clearListerners();

    }


    @Override
    public void onInvitationReceived(Invitation invitation) {
        // show in-game popup to let user know of pending invitation
        toast(activity.getString(R.string.new_invitation), Toast.LENGTH_LONG);

        // store invitation for use when player accepts this invitation
        mIncomingInvitationId = invitation.getInvitationId();
    }

    @Override
    public void onInvitationRemoved(String s) {

    }

    private void toast(String msg, int l) {
        activity.runOnUiThread(new RunnableToast(msg, l));
    }

    public void showInvitations(Activity mainActivity) {
        // launch the intent to show the invitation inbox screen
        Intent intent = Games.Invitations.getInvitationInboxIntent(GoogleAPI.instance().getClient());
        activity.startActivityForResult(intent, RC_INVITATION_INBOX);
    }





    private  class RunnableToast implements Runnable {
        private String msg;
        private int l;


        public RunnableToast(String msg, int l) {
            this.msg = msg;
            this.l = l;
        }

        public void run() {
            Toast.makeText(activity.getApplication(), msg, l).show();
        }
    }

    public void submitScore(String board_id, long score) {
        Games.Leaderboards.submitScore(GoogleAPI.instance().getClient(), board_id, score);
    }

    public void loadAchievements()  {

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
            Achievement ach = buf.get( i );

            // here you now have access to the achievement's data
            String id = ach.getAchievementId();  // the achievement ID string
            boolean unlocked = ach.getState() == Achievement.STATE_UNLOCKED;  // is unlocked
            boolean incremental = ach.getType() == Achievement.TYPE_INCREMENTAL;  // is incremental
            if ( incremental ) {
                int steps = ach.getCurrentSteps();  // current incremental steps

            }
        }
        buf.close();
        r.release();
    }

    public String getGameId() {
        //return mRoomId;
        ArrayList<String> ids = room.getParticipantIds();
        Collections.sort(ids);

        return TextUtils.join("_", ids);
    }

    public boolean isConnected() {

        return mRoomId != null;

    }

    public String getPalyerId() {
        return playerId;
    }
}
