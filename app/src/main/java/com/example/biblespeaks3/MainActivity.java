package com.example.biblespeaks3;

import static com.example.biblespeaks3.playService.mediaPlayer;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    RecyclerView parentRecyclerView;
    LinearLayout linearLayout;
    LocalBroadcastManager lbm;
    ListView listView;
    FloatingActionButton floatingActionButton,floatingActionButton2,floatingActionButton3;
    SeekBar seekBar;
    TextView textView,textView2,textView3,textView4;
    ImageView imageView;
    Button button;
    CheckBox checkBox;
    public static AlarmManager staticAlarmManager;
    public static Intent staticIntent;
    public static PendingIntent staticPendingIntent;
    public static Calendar calendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parentRecyclerView = findViewById(R.id.Parent_recyclerView);
        linearLayout = findViewById(R.id.LinearLayout);
        imageView = findViewById(R.id.imageView);
        textView2 = findViewById(R.id.textView2);
        textView = findViewById(R.id.textView3);
        textView3 = findViewById(R.id.textView4);
        textView4 = findViewById(R.id.textView);
        seekBar = findViewById(R.id.seekBar2);
        listView = findViewById(R.id.list);
        floatingActionButton2 = findViewById(R.id.floatingActionButton);
        floatingActionButton = findViewById(R.id.floatingActionButton2);
        floatingActionButton3 = findViewById(R.id.floatingActionButton3);
        button = findViewById(R.id.button);
        checkBox = findViewById(R.id.checkBox);

        linearLayout.setVisibility(View.INVISIBLE);

        LinearLayoutManager parentLayoutManager = new LinearLayoutManager(MainActivity.this);
        parentRecyclerView.setLayoutManager(parentLayoutManager);

        ArrayList<ParentModel> parentModelArrayList = new ArrayList<>();
        ParentRecyclerViewAdapter ParentAdapter = new ParentRecyclerViewAdapter(parentModelArrayList, MainActivity.this);
        parentRecyclerView.setAdapter(ParentAdapter);

        sharedpreferences = getSharedPreferences("" + R.string.app_name, MODE_PRIVATE);
        editor = sharedpreferences.edit();

        if(sharedpreferences.getString("1","1").equals("1")){
            floatingActionButton.setEnabled(false);
            floatingActionButton2.setEnabled(false);
            floatingActionButton3.setEnabled(false);
        }

        checkBox.setChecked(sharedpreferences.getBoolean("checkBox",true));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {

            //Background work here

            HttpURLConnection conn;
            try {
//                String urlString = "https://docs.google.com/spreadsheets/d/1UbLcCmbUnN6BK2jRZCf2t2FMa2IYXZwTP15wmgxn1bY/export?format=csv";
                String urlString = "https://docs.google.com/spreadsheets/d/1TuWYSw3pY0eWaNmcEuRUtWyHCg5D8UelAQ6b-MZEF0I/export?format=csv";
//                String urlString = "https://docs.google.com/spreadsheets/d/1VxQruR4Yt1Ive6qLqQZ2iV7qCr4x9GRL49yA9XM5GP8/export?format=csv";

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                if(conn.getResponseCode() == 200)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String inputLine;

                    while ((inputLine = br.readLine()) != null) {
                        if(!inputLine.startsWith("update")) {
                            String[] AS = inputLine.split("ALBUM");
                            String artist = AS[0].replace(",", "");
                            Set<String> set1 = new HashSet<>();

                            for (String as : AS) {
                                String albumName = " ", albumImage = " ";
                                Set<String> set = new HashSet<>();
                                String[] AS1 = as.split(",");
                                if (AS1.length > 1) {
                                    albumName = AS1[1];
                                    albumImage = AS1[AS1.length - 1];
                                }
                                for (int i = 2; i < AS1.length - 1; i++) {
                                    if (i % 2 == 0) {
                                        set.add(AS1[i]);
                                    } else {
                                        keepString(artist + "/" + albumName + "/" + AS1[i - 1], AS1[i]);
                                    }
                                }
                                if (set.size() > 0) {
                                    set1.add(albumName);
                                    keepString(artist + "/" + albumName + "/image", albumImage);
                                    keepStringSet(artist + "/" + albumName, set);
                                }
                            }
                            keepStringSet(artist, set1);
                            parentModelArrayList.add(new ParentModel(artist));
                        }else {
                            String[] splitStr = inputLine.split(",");
                            keepFloat(Float.parseFloat(splitStr[1]));
                            keepString("updateUrl",splitStr[2]);
                        }
                    }
                    //runOnUiThread(() -> parentRecyclerView.setAdapter(ParentAdapter));
                }

            }catch (Exception e){
                keepString("noInternet", "M-158\n"+e.getMessage());
            }


            handler.post(() -> {
                //UI Thread work here
                parentRecyclerView.setAdapter(ParentAdapter);
            });
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, playService.class));
        }else {
            startService(new Intent(this, playService.class));
        }

        if (sharedpreferences.getBoolean("one", true)) {

            setAlarming(this);
            keepBool("one",false);

        }
/*
        Calendar calendar = Calendar.getInstance();

        //if (sharedpreferences.getBoolean("checkBox", true) && staticPendingIntent == null && staticAlarmManager == null && calendar.get(Calendar.HOUR_OF_DAY)<22)
        if(true&&true&&true&&false)
        {
            setAlarming(this);
            //Toast.makeText(this, "a", Toast.LENGTH_SHORT).show();
        }
*/

    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String str = intent.getStringExtra("key");
                switch (str){
                    case "playImg":
                        floatingActionButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                        break;

                    case "pauseImg":
                        floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
                        break;

                    case "enable":
                        floatingActionButton.setEnabled(true);
                        floatingActionButton2.setEnabled(true);
                        floatingActionButton3.setEnabled(true);
                        break;

                    case "visibility":
                        parentRecyclerView.setVisibility(View.INVISIBLE);
                        linearLayout.setVisibility(View.VISIBLE);
                        String album = sharedpreferences.getString("ShowAlbum"," ");
                        Picasso.get()
                                .load(sharedpreferences.getString(album+"/image"," "))
                                .into(imageView);
                        String[] splitStr = album.split("/");
                        textView2.setText(splitStr[1]);
                        Set<String> set = sharedpreferences.getStringSet(album, null);
                        ArrayList<String> albumItems = new ArrayList<>(set);
                        Collections.sort(albumItems);
                        ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, albumItems){
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view =super.getView(position, convertView, parent);
                                TextView textView= view.findViewById(android.R.id.text1);
                                textView.setTextColor(Color.WHITE);
                                return view;
                            }
                        };

                        listView.setAdapter(adapter);

                        break;

                    case "PauseMax":
                        floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
                        int total = mediaPlayer.getDuration();
                        seekBar.setMax(total);
                        keepString("totalDuration",""+total % 3600000 / 60000+" : "+total % 3600000 % 60000 / 1000);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + str);
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter("UI"));

        if(mediaPlayer!=null){
            if(mediaPlayer.getCurrentPosition()>0){
                int total = mediaPlayer.getDuration();
                seekBar.setMax(total);
                keepString("totalDuration",""+total % 3600000 / 60000+" : "+total % 3600000 % 60000 / 1000);
            }
            if(mediaPlayer.isPlaying())
                floatingActionButton.setImageResource(R.drawable.ic_baseline_pause_24);
        }else {
            floatingActionButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }

        floatingActionButton.setOnClickListener(view -> playService.getInstance().playpause(MainActivity.this));

        floatingActionButton2.setOnClickListener(view -> playService.getInstance().prev(MainActivity.this));

        floatingActionButton3.setOnClickListener(view -> playService.getInstance().next(MainActivity.this));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(mediaPlayer!=null) {
                    int duration = mediaPlayer.getCurrentPosition();
                    keepString("currentDuration",""+duration % 3600000 / 60000+" : "+duration % 3600000 % 60000 / 1000);
                    seekBar.setProgress(duration);
                }else {
                    keepString("currentDuration","0 : 0");
                    keepString("totalDuration","0 : 0");
                    seekBar.setProgress(0);
                }
                textView3.setText(String.format("   %s", sharedpreferences.getString("currentDuration", "0 : 0")));
                textView4.setText(sharedpreferences.getString("totalDuration", "0 : 0"));

                if(sharedpreferences.getString("url","").contains("http"))
                    textView.setText(sharedpreferences.getString("text", "play"));
                else if(sharedpreferences.getString("noInternet","").contains("docs.google.com"))
                    textView.setText(String.format("   %s","No Internet"));
                else textView.setText(sharedpreferences.getString("noInternet", " "));


                handler.postDelayed(this, 500);
            }
        }, 0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) if(mediaPlayer!=null) mediaPlayer.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {

            String album = sharedpreferences.getString("ShowAlbum","");

            String topic = ""+adapterView.getItemAtPosition(i);

            keepString("album",album);
            keepString("topic",topic);
            keepString("subject", album.substring(album.indexOf("/")+1));
            keepString("image",sharedpreferences.getString(album+"/image",""));
            keepString("url",sharedpreferences.getString(album+"/"+topic,""));

            playService.getInstance().playpause(MainActivity.this);
        });

        checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            keepBool("checkBox",b);
            if(b) setAlarming(MainActivity.this);
            else if (staticPendingIntent != null && staticAlarmManager != null)
                staticAlarmManager.cancel(staticPendingIntent);
        });

        if(Float.parseFloat("1.4") < sharedpreferences.getFloat("version", Float.parseFloat("-1.0") ))
        button.setVisibility(View.VISIBLE);
        else button.setVisibility(View.INVISIBLE);

        button.setOnClickListener(view -> MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sharedpreferences.getString("updateUrl", "http://www.google.com")))));

    }

    private void keepFloat(float value) {
        editor.putFloat("version", value);
        editor.apply();
    }

    private void keepStringSet(String keyStr1, Set<String> valueStr1) {
        editor.putStringSet(keyStr1, valueStr1);
        editor.apply();
    }

    private void keepString(String keyStr1, String valueStr1) {
        editor.putString(keyStr1, valueStr1);
        editor.apply();
    }

    private void keepBool(String key, boolean value){
        editor.putBoolean(key,value);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        if(linearLayout.getVisibility()==View.VISIBLE){
            parentRecyclerView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lbm.unregisterReceiver(receiver);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static void setAlarming(Context context){

        staticIntent = new Intent(context, playBackground.class).setAction("alarm");

        staticPendingIntent = PendingIntent.getBroadcast(context, 0, staticIntent, 0);

        staticAlarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        calendar = Calendar.getInstance();


//        if (calendar.get(Calendar.HOUR_OF_DAY) <=2) {
//            Log.e("my","2");
//
//            calendar.set(Calendar.HOUR_OF_DAY, 4);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//        } else
        if((calendar.get(Calendar.HOUR_OF_DAY) >= 3 && calendar.get(Calendar.HOUR_OF_DAY) <= 6) || (calendar.get(Calendar.HOUR_OF_DAY) >= 16 && calendar.get(Calendar.HOUR_OF_DAY) <= 21)) {
            calendar.set(Calendar.HOUR_OF_DAY, (calendar.get(Calendar.HOUR_OF_DAY) + 1));
            //calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE) + 2));
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else if(calendar.get(Calendar.HOUR_OF_DAY) >= 7 && calendar.get(Calendar.HOUR_OF_DAY) <= 15) {
            calendar.set(Calendar.HOUR_OF_DAY, 17);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else{
            //calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
//            Log.e("my","nn = "+(calendar.get(Calendar.DAY_OF_WEEK) + 1));
//            calendar.set(Calendar.DAY_OF_WEEK,(calendar.get(Calendar.DAY_OF_WEEK) + 1));
            calendar.set(Calendar.HOUR_OF_DAY, 4);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if(calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            staticAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), staticPendingIntent);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            staticAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), staticPendingIntent);
        else
            staticAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), staticPendingIntent);

    }
}