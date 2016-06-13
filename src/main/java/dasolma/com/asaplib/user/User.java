package dasolma.com.asaplib.user;

import android.location.Location;

import dasolma.com.asaplib.msa.Factory;
import dasolma.com.asaplib.play.GoogleAPI;
import com.google.android.gms.location.LocationServices;

/**
 * Created by dasolma on 15/05/15.
 */
public class User {

    private static User instance;
    private int gender;
    private String name;
    private String language;
    private int min_range_age;
    private int max_range_age;
    private String birthday;
    private String nickname;
    private String currentlocation;
    private String id;
    private String userEnterGender;
    private int userEnterAge;

    private User() {

    }

    public static User instance() {
        if( instance == null ) {
            instance = new User();
            Factory.addObject(instance);
        }

        return instance;
    }

    public String getId() {return id;}
    public void setId(String id) { this.id = id;}


    public int getGender() {return gender;}
    public void setGender(int gender) { this.gender = gender;}

    public int getMin_range_age() {return min_range_age;}
    public void setMin_range_age(int min_range_age) { this.min_range_age = min_range_age;}

    public int getMax_range_age() {return max_range_age;}
    public void setMax_range_age(int max_range_age) { this.max_range_age = max_range_age;}

    public String getName() {return name;}
    public void setName(String name) { this.name = name;}

    public String getLanguage() {return language;}
    public void setLanguage(String language) { this.language = language;}

    public String getBirthday() {return birthday;}
    public void setBirthday(String birthday) { this.birthday = birthday;}

    public String getCurrentlocation() {return currentlocation;}
    public void setCurrentlocation(String currentlocation) { this.currentlocation = currentlocation;}

    public String getNickname() {return nickname;}
    public void setNickname(String nickname) { this.nickname = nickname;}

    public String getLocation() {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(GoogleAPI.instance().getClient());

        if ( mLastLocation != null ) {
            return String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }

        return "";
    }

    public String getUserEnterGender() {
        return userEnterGender;
    }

    public void setUserEnterGender(String userEnterGender) {
        this.userEnterGender = userEnterGender;
    }

    public int getUserEnterAge() {
        return userEnterAge;
    }

    public void setUserEnterAge(int userEnterAge) {
        this.userEnterAge = userEnterAge;
    }
}
