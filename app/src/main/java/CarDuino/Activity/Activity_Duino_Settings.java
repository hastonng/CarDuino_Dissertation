package CarDuino.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;


import java.util.Timer;
import java.util.TimerTask;

import CarDuino.R;
import CarDuino.Services.Class_BluetoothLeService;
import CarDuino.Services.SaveSharedPreference;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Activity_Duino_Settings extends AppCompatActivity
{
    private Toolbar toolbar_id;
    private Switch auto_pilot_id;
    public static TimerTask readRssi;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duino_settings);

        auto_pilot_id = (Switch) findViewById(R.id.auto_pilot_id);
        toolbar_id = (Toolbar) findViewById(R.id.toolbar_id);

        //Set toolbar
        setSupportActionBar(toolbar_id);

        auto_pilot_id.setChecked(SaveSharedPreference.getRSSI_STATE(this));

        //Toolbar Back Button
        toolbar_id.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Return to Previous Activity/Page
                onBackPressed();
            }
        });

        //Auto Pilot Check Listener
        auto_pilot_id.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    auto_pilot_id.setChecked(true);
                    SaveSharedPreference.setRSSI_STATE(Activity_Duino_Settings.this,true);

                    readRssi = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            if(Activity_Main.bluetoothGatt != null)
                            {
                                Activity_Main.bluetoothGatt .readRemoteRssi();
                            }
                        }
                    };
                    Timer startTask = new Timer();
                    startTask.schedule(readRssi,1000,1000);
                }
                else
                {
                    auto_pilot_id.setChecked(false);
                    if(readRssi != null)
                    {
                        readRssi.cancel();
                        SaveSharedPreference.setRSSI_STATE(Activity_Duino_Settings.this,false);
                    }


                }
            }
        });

    }
}
