package dasolma.com.asaplib.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dasolma on 19/05/15.
 */
public class Http {

    public static String get(String _url) {

        URL url = null;
        String res = "";
        try {
            url = new URL( _url );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url != null ) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.connect();

                // Read response
                StringBuilder responseSB = new StringBuilder();
                InputStream response = connection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(response));

                String line;
                while ((line = br.readLine()) != null)
                    responseSB.append(line);

                // Close streams
                br.close();
                res = responseSB.toString();


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return res;
    }
}
