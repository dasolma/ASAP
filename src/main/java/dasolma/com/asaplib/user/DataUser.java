package dasolma.com.asaplib.user;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by dasolma on 17/08/15.
 */
public class DataUser implements Serializable {

    private final static String DATA_USER = "datauser.dat";
    private String userEnterGender;
    private int userEnterAge;
    private boolean sawTutorial;

    public String getUserEnterGender() {
        return userEnterGender;
    }

    public void setUserEnterGender(String userEnterGender) {
        this.userEnterGender = userEnterGender;
    }

    public boolean isSawTutorial() {
        return sawTutorial;
    }

    public void setSawTutorial(boolean sawTutorial) {
        this.sawTutorial = sawTutorial;
    }

    public int getUserEnterAge() {
        return userEnterAge;
    }

    public void setUserEnterAge(int userEnterAge) {
        this.userEnterAge = userEnterAge;
    }

    public void save(Context context) {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        try {
            fos = context.openFileOutput(DATA_USER, Context.MODE_PRIVATE);

            os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DataUser load(Context context) throws IOException, ClassNotFoundException {

        FileInputStream fis = context.openFileInput(DATA_USER);
        ObjectInputStream is = new ObjectInputStream(fis);
        DataUser dataUser = (DataUser) is.readObject();
        is.close();
        fis.close();

        return dataUser;
    }


}
