 package com.example.forestfirealert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.StringResponse;
import kong.unirest.Unirest;

import static com.example.forestfirealert.Alerts.CHANNEL_1_ID;

public class RegisteredSensor extends AppCompatActivity {
    Button seeLocation;
    TextView textView;
    Boolean sentF = false, sentFa = false, went = false;
    Thread fireSense;
    Thread status;
    MediaPlayer mp;
    MediaPlayer smp;
    JSONObject r;
    NotificationManagerCompat notificationManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_sensor);
        mp = MediaPlayer.create(this, R.raw.siren);
        smp = MediaPlayer.create(this, R.raw.sos);

        seeLocation = (Button) findViewById(R.id.get_direction);
        seeLocation.setEnabled(false);

        seeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location;
                try {
                   location = r.getString("location");
                    Intent i = new Intent(getBaseContext(), Locate.class);
                    i.putExtra("location", location);
                    went = true;
                    if (mp.isPlaying())
                        mp.stop();
                    startActivity(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        textView = (TextView) findViewById(R.id.active_status);
        notificationManagerCompat = NotificationManagerCompat.from(this);
// ...

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String serverlUrl = "192.168.43.167:5000";
        String url = "http://" + serverlUrl + "/offline_status";

// Request a string response from the provided URL.
        StringRequest statusRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        seeLocation.setEnabled(true);
                        if(!seeLocation.isClickable()){
                            seeLocation.setEnabled(true);
                        }
                        int s;
                        try {
                            r = new JSONObject(response);

                            s = r.getInt("online");

                            if (s == 0) {
                                textView.setText("Offline");
                                textView.setTextColor(Color.RED);
                                playSatusSound();
                                if (!sentF) {
                                    sendFaultAlert();

                                    sentF = true;
                                }

                            } else {
                                String v = textView.getText().toString();
                                if(!v.equals("Fire Detected !!")) {
                                    textView.setText("Online");
                                    textView.setTextColor(Color.GREEN);
                                    sentF = false;
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection", Toast.LENGTH_LONG).show();
            }
        });

// Add the request to the RequestQueue.

        status = new Thread() {
            public void run() {
                while (true) {

                    queue.add(statusRequest);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        status.start();


        RequestQueue fqueue = Volley.newRequestQueue(this);
        String furl = "http://" + serverlUrl + "/fire_status";

        // Request a string response from the provided URL.
        StringRequest fireRequest = new StringRequest(Request.Method.GET, furl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(), "" + response, Toast.LENGTH_LONG).show();
                        try {
                            JSONObject obj = new JSONObject(response);

                            if (obj.getInt("fire") == 1) {
                                textView.setText("Fire Detected !!");
                                textView.setTextColor(Color.RED);
                                playFireSound();
                                if (!sentFa) {
                                    sendFireAlert();
                                    sentFa = true;
                                }
                            }else {
                                String v = textView.getText().toString();
                                if(v.equals("Online")) {
                                    textView.setText("Online");
                                    textView.setTextColor(Color.GREEN);
                                } else if(v.equals("Offline") || v.equals("Fire Detected !!")){
                                    textView.setText("Offline");
                                    textView.setTextColor(Color.RED);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Please Your Check Internet Connection", Toast.LENGTH_LONG).show();
            }
        });
        // Add the request to the RequestQueue.

        fireSense = new Thread() {
            public void run() {
                while (true) {

                    fqueue.add(fireRequest);

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        fireSense.start();
    }

    private void playFireSound() {

        if (!mp.isPlaying())
            mp.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mp.isPlaying())
            mp.stop();
        if (smp.isPlaying())
            smp.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mp.isPlaying())
            mp.stop();
        if (smp.isPlaying())
            smp.stop();
    }

    private void playSatusSound() {
        if (!smp.isPlaying() && !went)
            smp.start();
    }

    public void sendFireAlert() {
        String location;

        try {
            location = r.getString("location");
            Intent i = new Intent(getBaseContext(), Locate.class);
            i.putExtra("location", location);
            went = true;
            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 100, i, PendingIntent.FLAG_CANCEL_CURRENT);
            Uri sound = Uri.parse("android.resource://" +
                    getApplicationContext().getPackageName() +
                    "/" + R.raw.siren);
            Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.forest_icon)
                    .setContentTitle("Fire Has been Detected in Mabira Forest")
                    .setContentText("Tap here for NAVIGATION")
                    .setSound(sound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManagerCompat.notify(1, notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void sendFaultAlert() {
        String location = null;
        try {
            location = r.getString("location");
            Intent i = new Intent(getBaseContext(), Locate.class);
            i.putExtra("location", location);
            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 100, i, PendingIntent.FLAG_CANCEL_CURRENT);
            Uri sound = Uri.parse("android.resource://" +
                    getApplicationContext().getPackageName() +
                    "/" + R.raw.sos);
            Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.forest_icon)
                    .setContentTitle("Sensor 1 has gone offline in Mabira Forest")
                    .setContentText("TAP here for NAVIGATION")
                    .setSound(sound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManagerCompat.notify(1, notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}

