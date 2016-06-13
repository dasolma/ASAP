package dasolma.com.asaplib.user;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import dasolma.com.asaplib.io.Http;
import dasolma.com.asaplib.msa.Factory;

/**
 * Created by dasolma on 19/05/15.
 */
public class Device {

    private static Device instance;

    private String _ip = "";

    private Device() {

    }

    public static Device instance() {
        if( instance == null ) {
            instance = new Device();
            Factory.addObject(instance);
        }

        return instance;
    }

    public String getOsVersion() {
        return System.getProperty("os.version");
    }

    public int getSdk() {
        return Build.VERSION.SDK_INT;
    }

    public String getDevice() {
        return Build.DEVICE;
    }

    public String getModel() {
        return Build.MODEL;
    }

    public String getProduct() {
        return Build.PRODUCT;
    }

    public String getId() {
        TelephonyManager telephonyManager =
                (TelephonyManager)Factory.getContext().getSystemService(Factory.getContext().TELEPHONY_SERVICE);
        String id = telephonyManager.getDeviceId();

        if ( id == null )
            id = getMAC();

        return id;
    }

    public String getMAC() {
        WifiManager wifiMan = (WifiManager) Factory.getContext().getSystemService(
                Factory.getContext().WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        return wifiInf.getMacAddress();
    }

    public int getScreenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();

        Factory.getContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return metrics.widthPixels;
    }

    public int getScreenHeight() {
        DisplayMetrics metrics = new DisplayMetrics();

        Factory.getContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return metrics.heightPixels;
    }

    public int getDensityDpi() {
        DisplayMetrics metrics = new DisplayMetrics();

        Factory.getContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return metrics.densityDpi;
    }


    public String getIP() {
        if (_ip != "" ) return _ip;

        _ip = Http.get("http://wtfismyip.com/text");

        return _ip;

    }


}
