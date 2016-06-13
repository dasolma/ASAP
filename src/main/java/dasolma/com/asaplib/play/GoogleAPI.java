package dasolma.com.asaplib.play;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import dasolma.com.asaplib.gamification.InformationListener;
import dasolma.com.asaplib.msa.Factory;
import dasolma.com.asaplib.user.DataUser;
import dasolma.com.asaplib.user.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.ArrayList;

/**
 * Created by dasolma on 29/12/14.
 */
public class GoogleAPI implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {

    private static GoogleAPI instance;
    private GoogleApiClient mGoogleApiClient;
    private FragmentActivity activity;

    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private ArrayList<InformationListener> eventsListeners = new ArrayList<InformationListener>();

    private GoogleAPI() {

    }

    public static GoogleAPI instance() {
        if( instance == null ) {
            //Log.i("GoogleAPI", "Crate a new instance of GoogleAPI");
            instance = new GoogleAPI();
            Factory.addObject(instance);
        }

        return instance;

    }

    public boolean isConnected() {
        if( mGoogleApiClient == null ) return false;

        return mGoogleApiClient.isConnected();
    }

    public void disconnect() {

        mGoogleApiClient.disconnect();

    }

    public boolean isConnecting() {
        return mGoogleApiClient.isConnecting();
    }

    public GoogleApiClient getClient() {
        return mGoogleApiClient;
    }

    public void connect(FragmentActivity activity) {

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {

            this.activity = activity;

            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Games.API)
                    .addApi(Plus.API)
                    .addApi(LocationServices.API)
                    .addScope(Plus.SCOPE_PLUS_PROFILE)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();
            //Log.i("GoogleAPI", "Conected to GoogleAPI");


        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if ( mGoogleApiClient.isConnected() ) {
            Games.Invitations.registerInvitationListener(mGoogleApiClient, RealTimeMultiplayer.instance());

            getInfoPerson();

            onUserConnected();
        }

        if (connectionHint != null) {
            Invitation inv =
                    connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null && mGoogleApiClient.isConnected()) {
                //Log.i("GoogleAPI", "Found invitation");
                RealTimeMultiplayer.instance().connectInvitation(inv);
            }
        }
    }

    private void getInfoPerson() {
        try {
            int gender;
            Plus.PeopleApi.load(mGoogleApiClient, "me");
            Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
            Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

            if (person != null) {

                if (person.hasGender()) // it's not guaranteed
                    User.instance().setGender(person.getGender());

                if (person.hasAgeRange()) {
                    if (person.getAgeRange().hasMin())
                        User.instance().setMin_range_age(person.getAgeRange().getMin());
                    if (person.getAgeRange().hasMax())
                        User.instance().setMax_range_age(person.getAgeRange().getMax());

                }

                if (person.hasBirthday())
                    User.instance().setBirthday(person.getBirthday());

                if (person.hasCurrentLocation())
                    User.instance().setCurrentlocation(person.getCurrentLocation());

                if (person.hasDisplayName())
                    User.instance().setName(person.getDisplayName());

                if (person.hasNickname())
                    User.instance().setNickname(person.getNickname());

                if (person.hasLanguage())
                    User.instance().setLanguage(person.getLanguage());

                if (person.hasId())
                    User.instance().setId(person.getId());

                DataUser dataUser = DataUser.load(activity);
                User.instance().setUserEnterGender(dataUser.getUserEnterGender());
                User.instance().setUserEnterAge(dataUser.getUserEnterAge());

            }




        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(activity, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }

    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show( activity.getSupportFragmentManager(), "errordialog");
    }

    @Override
    public void onResult(People.LoadPeopleResult loadPeopleResult) {

    }


    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {

        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            //((MainActivity)getActivity()).onDialogDismissed();
        }
    }

    public void setOnUserConnected( InformationListener listener) {
        eventsListeners.add(listener);
    }

    protected void onUserConnected() {
        for(InformationListener ml: eventsListeners)
            ml.info(this);
    }

}
