package dasolma.com.asaplib.logging;

import android.util.Log;

import dasolma.com.asaplib.activities.GameActivity;
import dasolma.com.asaplib.gamification.BaseGamification;
import dasolma.com.asaplib.gamification.EventListener;
import dasolma.com.asaplib.gamification.InformationListener;
import dasolma.com.asaplib.msa.Factory;
import dasolma.com.asaplib.msa.Model;
import dasolma.com.asaplib.play.GoogleAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by dasolma on 13/05/15.
 */
public class ADAPServerLogger implements ILogger {

    private static final String DEBUG_TAG = "Debug CLG" ;
    List<IMonitor> monitors = new ArrayList<IMonitor>();
    Queue<LogData> logData = new ArrayDeque<LogData>();
    BaseGamification gamification;
    GoogleAPI googleAPI;
    Gson gson;
    Thread apiThread;
    String url = "http://mowento.cs.us.es:8571/";

    public ADAPServerLogger(List<IMonitor> mon) {
        this.monitors = mon;

        GsonBuilder gsonb = new GsonBuilder();
        gsonb.registerTypeAdapter(KeyValueList.class, new KeyValueSerializer());
        gson = gsonb.create();
        //gson= new Gson();


        GameActivity.seOnGameCreateListener(new InformationListener() {
            @Override
            public void info(Object source) {
                attachToListeners();
            }
        });


        this.googleAPI = Factory.getGooglePAI();

        googleAPI.setOnUserConnected(new InformationListener() {
            @Override
            public void info(Object source) {
                for (IMonitor monitor : monitors) {
                    if (monitor.getWhen() == IMonitor.EnumWhen.Connect)
                        put(monitor);
                }
            }
        });

        apiThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while(true) {

                    if( logData.size() > 0) {
                        LogData ld = logData.poll();
                        boolean meetCondition = true;
                        IMonitor monitor = ld.getMonitor();
                        if ( monitor.getCondition() != null )
                            meetCondition = monitor.getCondition().meet();

                        if ( meetCondition && !putApi(ld.getType(), ld.getData()) )
                            logData.add(ld);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        apiThread.start();
    }

    private void attachToListeners() {
        this.gamification = Factory.getGamification();


        //start handler
        gamification.setReadyListener(new InformationListener() {
            @Override
            public void info(Object source) {
                for (IMonitor monitor : monitors) {
                    if (monitor.getWhen() == IMonitor.EnumWhen.Start)
                        put(monitor);
                        //Log.i(DEBUG_TAG, "Logging init...");
                        //new PutMonitorAsync(monitor).run();
                }
            }
        });


        //each tick handler
        for( Model model: this.gamification.getModels() ) {
            model.setTickListerner(new InformationListener() {
                @Override
                public void info(Object source) {
                    for(IMonitor monitor: monitors) {
                        if( monitor.getWhen() == IMonitor.EnumWhen.EachTick )
                            put(monitor);
                    }
                }
            });
        }

        //finish handler
        gamification.setFinishedListenerListener(new InformationListener() {
            @Override
            public void info(Object source) {
                for (IMonitor monitor : monitors) {
                    if (monitor.getWhen() == IMonitor.EnumWhen.Finish)
                        put(monitor);
                }
            }
        });

        //event handler
        gamification.setEventListener(new EventListener() {
            @Override
            public void info(String event) {
                for (IMonitor monitor : monitors) {
                    if (monitor.getWhen() == IMonitor.EnumWhen.Event && monitor.getEvent() == event)
                        put(monitor);
                }
            }
        });
    }


    private void put(IMonitor monitor) {
        Thread th = new Thread( new PutMonitorAsync(monitor) );
        th.start();
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class PutMonitorAsync implements Runnable {

        private final IMonitor monitor;

        public PutMonitorAsync(IMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void run() {
            Log.i(DEBUG_TAG, "Logging....");

            if ( monitor.getCondition() == null || monitor.getCondition().meet() )
                logData.add(new LogData(monitor.getType(), monitor.get(), monitor));
        }
    }

    private boolean putApi(String collection, Object logdata) {

        try {
            URL url = new URL(this.url + collection);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");


            String postData = gson.toJson(logdata);
            connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));

            // Write data
            OutputStream os = connection.getOutputStream();
            os.write(postData.getBytes());

            // Read response
            StringBuilder responseSB = new StringBuilder();
            InputStream response = connection.getInputStream();
            int status = connection.getResponseCode();

            if ( status == 201 ) {
                BufferedReader br = new BufferedReader(new InputStreamReader(response));

                String line;
                while ((line = br.readLine()) != null)
                    responseSB.append(line);

                // Close streams
                br.close();
                os.close();

                String res = responseSB.toString();

                return true;
            }
            else {
                return false;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exist(String collection, String key, String value) {

        try {
            String query = URLEncoder.encode( String.format("{ \"%s\":\"%s\" }", key, value), "UTF-8" );
            query = String.format("%s?query=%s", collection, query);
            URL url = new URL(this.url + query );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();


            // Read response
            StringBuilder responseSB = new StringBuilder();
            InputStream response = connection.getInputStream();
            int status = connection.getResponseCode();

            if ( status == 200 ) {
                BufferedReader br = new BufferedReader(new InputStreamReader(response));

                String line;
                while ((line = br.readLine()) != null)
                    responseSB.append(line);

                // Close streams
                br.close();
                String res = responseSB.toString();

                if (res.contains("\"total_count\": 0"))
                    return false;
            }

            return true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }


}
