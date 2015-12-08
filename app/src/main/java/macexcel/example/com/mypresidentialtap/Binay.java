package macexcel.example.com.mypresidentialtap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Binay extends AppCompatActivity {
    private int seconds = 0;
    private boolean wasRunning = false;
    private boolean isRunning = false;
    private boolean counterrunner = false;
    private int tapbinay = 0;
    private int limitcounter = 86400; //1 day waiting
    private int timelimit = 3600; //1hour time limit
    private int taplimit = 1;

    SharedPreferences pref;
    String sharedPrefName = "shared_preference_presidentialtap";

    //private MobileServiceClient mClient;
    //private MobileServiceTable<PresidentialTap> mTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binay);

        // declare the GetSharedPreferences singleton instance.
        pref = this.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);

        // get or retrieve the last known seconds.
        seconds = pref.getInt("seconds", 0);
        limitcounter = pref.getInt("limitcounter", 86400);
        timelimit = pref.getInt("timelimit", 3600);

        runTimer();
    }

    //Start the stopwatch running when the Start button is clicked.
    public void onClickStart(View view) {
        String toastMsg = String.format("Time Limit Exceeded\n Vote in the next %d seconds", limitcounter);

        if (!isRunning && taplimit > 0) {
            isRunning = true;
            taplimit--;
        }
        else if(isRunning) {
            tapbinay++;
        }
        else {
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
        }

        TextView tapView = (TextView)findViewById(R.id.textTap);
        String taps = String.format("%d", tapbinay);
        tapView.setText(taps);
    }

    //Sets the number of seconds on the timer.
    private void runTimer() {
        final TextView timeView = (TextView)findViewById(R.id.textViewTime);
        //final TextView limitView = (TextView)findViewById(R.id.textLimit);
        final TextView dateView = (TextView)findViewById(R.id.textDate);
        final TextView boolView = (TextView)findViewById(R.id.textBool);

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
                Calendar resultdate = Calendar.getInstance();
                String ms = String.format("%s", sdf.format(resultdate.getTime()));
                String time = String.format("%d / 3600", seconds);
                //String limit = String.format("%d", taplimit);
                if (isRunning)
                    boolView.setText("You can still vote today!");
                else
                    boolView.setText("Voting ended, try again tomorrow");

                dateView.setText(ms);
                timeView.setText(time);


                if (isRunning) {
                    seconds++;
                }
                if (counterrunner)
                    limitcounter--;
                if (limitcounter == 0) {
                    seconds = 0;
                    counterrunner = false;
                    limitcounter = 86400;
                    taplimit++;
                }
                if (seconds > timelimit) {
                    counterrunner = true;
                    isRunning = false;

                }

                handler.postDelayed(this, 1000);
            }
        });
    }

    // Handle configuration changes by saving the Activity state variables
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasRunning = isRunning;
        isRunning = false;

        // save the preferences for Binay.
        pref.edit().putInt("tapbinay", tapbinay).apply();
        pref.edit().putInt("seconds", seconds).apply();
        pref.edit().putInt("limitcounter", limitcounter).apply();
        pref.edit().putInt("timelimit",timelimit).apply();

        // save to Azure backend.
        try {
            String serviceUrl = getString(R.string.serviceUri);
            String key = getString(R.string.serviceKey);

            MobileServiceClient mClient = new MobileServiceClient(serviceUrl, key, this);
            MobileServiceTable<PresidentialTap> mTable = mClient.getTable(PresidentialTap.class);

            new Shared().UpdateTaps(mTable, "binay", tapbinay);

            // display message toaster.
            Toast.makeText(this, "Binay's taps count has been updated!", Toast.LENGTH_SHORT).show();
        }
        catch(Exception exception) {
            Log.e("MobileService", exception.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        wasRunning = isRunning;
        isRunning = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRunning = wasRunning;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = wasRunning;

        // restore the saved preferences.
        tapbinay = 0; //pref.getInt("tapbinay", 0);
        seconds = pref.getInt("seconds", 0);
        timelimit = pref.getInt("timelimit", 3600);
        limitcounter = pref.getInt("limitcounter", 86400);

        // update the textTap label control.
        TextView tapView = (TextView)findViewById(R.id.textTap);
        String taps = String.format("%d", tapbinay);
        tapView.setText(taps);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}