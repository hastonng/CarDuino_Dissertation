package CarDuino.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.snackbar.Snackbar;

import CarDuino.Services.Adapter_VehicleCard;
import CarDuino.Services.Class_BluetoothLeService;
import CarDuino.Services.Class_GattAttributes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import CarDuino.Services.Class_BluetoothConnectionService;
import CarDuino.Services.Connection;
import CarDuino.Services.CustomRequest;
import CarDuino.Fragment.Fragment_EditCarDetail;
import CarDuino.R;
import CarDuino.Services.SaveSharedPreference;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;

import static CarDuino.Activity.Activity_Main.bluetoothLeService;
import static android.bluetooth.BluetoothProfile.GATT;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTING;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTING;


public class Activity_CarDetail extends AppCompatActivity
{
    //Bluetooth
    private BluetoothGattCharacteristic CAR_DUINO_CHARACTERISTIC;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private Intent gattServiceIntent;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics
            = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();


    private RequestQueue requestQueue;
    private Toolbar toolbar;
    private Button bluetooth_lock_button_id;
    private Button bluetooth_unlock_button_id;
    private Button bt_enable_button_id;
    private LottieAnimationView simple_tick_animation_id;
    private CardView car_detail_image_id;
    private ImageView vehicle_image_id;
    private TextView car_name_id;
    private TextView car_model_id;
    private TextView car_conn_status_id;
//    private Bundle bundle = new Bundle();
    private int position;
    private int vehicle_id;
    private String vehicle_path;

    public static boolean SELECTED_DEVICE = false;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private String device_ble_address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_car_detail);
        car_name_id = findViewById(R.id.car_name_id);
        car_model_id = findViewById(R.id.car_model_id);
        car_conn_status_id = findViewById(R.id.car_conn_status_id);
        toolbar = findViewById(R.id.toolbar_id);
        vehicle_image_id = findViewById(R.id.vehicle_image_id);
        simple_tick_animation_id = findViewById(R.id.simple_tick_animation_id);
        car_detail_image_id = findViewById(R.id.car_detail_image_id);
        bluetooth_lock_button_id = findViewById(R.id.bluetooth_lock_button_id);
        bluetooth_unlock_button_id = findViewById(R.id.bluetooth_unlock_button_id);
        bt_enable_button_id = findViewById(R.id.bt_enable_button_id);

        //Set toolbar as ActionBar
        setSupportActionBar(toolbar);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(btReceiver,makeGattUpdateIntentFilter());

        //Get Bluetooth Adapter
        // Initializes a Bluetooth adapter.
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //Get getIntent data of vehicle in recyclerview
        position = getIntent().getIntExtra("position", 0);
        vehicle_id = getIntent().getIntExtra("vehicle_id", 0);
        vehicle_path = getIntent().getStringExtra("vehicle_path");

        //Display Car Details {car name, car model...}
        car_name_id.setText(getIntent().getStringExtra("vehicle_name"));
        car_model_id.setText(getIntent().getStringExtra("vehicle_model"));
        vehicle_image_id.setImageBitmap(BitmapFactory.decodeFile(vehicle_path));


//        gattServiceIntent = new Intent(this, Class_BluetoothLeService.class);
//        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        //Check Status Connected
        if(getIntent().getStringExtra("conn_status").equals("Not Available") | getIntent().getStringExtra("conn_status").equals("Not Connected"))
        {
            car_conn_status_id.setText("Not Connected");
            bt_enable_button_id.setEnabled(true);
            simple_tick_animation_id.setVisibility(View.INVISIBLE);
        }
        else
        {
            car_conn_status_id.setText("Connected");
            bt_enable_button_id.setTextSize(0);
            simple_tick_animation_id.setVisibility(View.VISIBLE);
            simple_tick_animation_id.playAnimation();
            bt_enable_button_id.setEnabled(false);
        }
        //Toolbar Back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                onBackPressed();
                //Hide Buttons
                bluetooth_lock_button_id.setVisibility(View.INVISIBLE);
                bluetooth_unlock_button_id.setVisibility(View.INVISIBLE);
           }
        });

        //Enable Bluetooth Button
        bt_enable_button_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult( enableBtIntent, 2);
            }
        });

        //Lock button
        bluetooth_lock_button_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(!car_conn_status_id.getText().equals("Connected"))
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult( enableBtIntent, 2);
                }
                else
                {
                    String v1 = "LOCK";
                    byte[] bytes = v1.getBytes(Charset.defaultCharset());
                    getServices();
                    CAR_DUINO_CHARACTERISTIC.setValue(bytes);
                    bluetoothLeService.writeCharacteristic(CAR_DUINO_CHARACTERISTIC);
                    Class_BluetoothLeService.AUTO_LOCK_INDICATOR = true;
                }
            }
        });

        //Unlock Button
        bluetooth_unlock_button_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!car_conn_status_id.getText().equals("Connected"))
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult( enableBtIntent, 2);
                }
                else
                {
                    String v1 = "UNLOCK";
                    byte[] bytes = v1.getBytes(Charset.defaultCharset());
                    getServices();
                    CAR_DUINO_CHARACTERISTIC.setValue(bytes);
                    bluetoothLeService.writeCharacteristic(CAR_DUINO_CHARACTERISTIC);
//                    bluetoothLeService.setCharacteristicNotification(CAR_DUINO_CHARACTERISTIC, true);

                }
            }
        });

        getData();

    }

    public void getServices()
    {
        List<BluetoothGattService> services = bluetoothLeService.getSupportedGattServices();
        for(BluetoothGattService service : services)
        {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for(BluetoothGattCharacteristic characteristic : characteristics)
            {
                if(characteristic.getUuid().equals(Class_BluetoothLeService.UUID_CAR_DUINO))
                {
                    CAR_DUINO_CHARACTERISTIC = characteristic;
                }
            }
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service)
        {
            bluetoothLeService = ((Class_BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize())
            {
                Log.e("TAG: ", "Unable to initialize Bluetooth");
                finish();
            }

            Log.e("TAG: ", "Outside Initialize");
            checkBleConnection(bluetoothLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            Log.e("TAG: ", "On disconnected service");
            checkBleConnection(bluetoothLeService);
        }
    };

    /**
     * Description:
     * This Method will Perform 'DELETE' function on the current Vehicle
     * That the user had selected.
     *
     * Function:
     * The Method will select the vehicle_id of the RecyclerView Adapter and call JSON
     * and API to pass the vehicle_id into the database to perform DELETE
     *
     * @return: VOID
     */
    private void deleteVehicle()
    {

        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(this);

        try
        {
            //{"vehicle_id": "YOUR VEHICLE ID"}
            jsonAdd.put("vehicle_id",vehicle_id);

            //{"device":{"user_id": "YOUR USER ID"}}
            jsonObj.put("device",jsonAdd);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,new Connection().getDevicesUrl(),jsonObj, new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if(error instanceof NoConnectionError)
                {
                    final Snackbar snackbar = Snackbar.make(new View(Activity_CarDetail.this), "Something went wrong, an unknown network error has occurred.", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.parseColor("#3F51B5"));
                    snackbar.setAction("OK", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            snackbar.dismiss();
                        }
                    });

                    snackbar.show();
                }
            }
        })
        {
            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError)
            {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null)
                {
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }

                return volleyError;
            }
        };
        //Add Request Queue with jSonObjReq
        requestQueue.add(jsonObjReq);

    }

    /**
     * Description:
     * This Method will Perform 'Update' function on the current Vehicle
     * That the user had selected.
     *
     * Function:
     * The Method will select the user_registered_devices and user_id
     * from the RecyclerView Adapter and call JSON and API to pass the user_registered_devices
     * and user_id into the database to perform update
     *
     * @return: VOID
     */
    private void updateRegDevice()
    {
        // Get usersUrl
        String usersUrl = new Connection().getUsersUrl();

        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(this);
        try
        {

            if(SaveSharedPreference.getPREF_REG_DEVICES(Activity_CarDetail.this) <= 1)
            {
                jsonAdd.put("user_registered_devices",-1);
                jsonAdd.put("user_id",SaveSharedPreference.getPREF_USER_ID(this));
            }
            else
            {
                jsonAdd.put("user_registered_devices",SaveSharedPreference.getPREF_REG_DEVICES(this) - 1);
                jsonAdd.put("user_id",SaveSharedPreference.getPREF_USER_ID(this));
            }

            jsonObj.put("user",jsonAdd);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.PUT, usersUrl, jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {

                SaveSharedPreference.setPREF_REG_DEVICES(Activity_CarDetail.this,SaveSharedPreference.getPREF_REG_DEVICES(Activity_CarDetail.this) - 1);
                Activity_Main.classVehicle_list.clear();
                Intent myIntent = new Intent(Activity_CarDetail.this, Activity_Main.class);
                Activity_CarDetail.this.startActivity(myIntent);
                finish();

                Log.e("Where am i"," I AM IN THE     ERROR!!!");
            }
        })
        {
            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError)
            {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null)
                {
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }

                return volleyError;
            }
        };
        //Add Request Queue with jSonObjReq
        requestQueue.add(jsonObjReq);
    }


    /**
     * Description:
     * This Method will Perform 'Get' to retrieve information from the database of the Vehicle
     *
     * Function:
     * The Method will select the user_id from the RecyclerView Adapter
     * and call JSON and API to pass the user_id into the database to perform Get
     *
     * @return: VOID
     */
    public void getData()
    {
        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(this);

        try
        {
            //{"user_id": "YOUR USER ID"}
            jsonAdd.put("user_id",SaveSharedPreference.getPREF_USER_ID(this));

            //{"device":{"user_id": "YOUR USER ID"}}
            jsonObj.put("device",jsonAdd);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,new Connection().getDevicesUrl(),jsonObj, new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
//                    car_name_id.setText(response.getJSONObject(position).getString("vehicle_name"));
//                    car_model_id.setText(response.getJSONObject(position).getString("vehicle_model"));
//                    vehicle_path = response.getJSONObject(position).getString("vehicle_image_path");
                    device_ble_address = response.getJSONObject(position).getString("device_ble_address");
//                    vehicle_image_id.setImageBitmap(BitmapFactory.decodeFile(vehicle_path));


                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("Testing Failed: ","Failed to Retreieve Data!!!");

                if(error instanceof NoConnectionError)
                {
                    final Snackbar snackbar = Snackbar.make(new View(Activity_CarDetail.this), "Something went wrong, an unknown network error has occurred.", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.parseColor("#3F51B5"));
                    snackbar.setAction("OK", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }

            }
        })
        {
            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError)
            {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null)
                {
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }

                return volleyError;
            }
        };
        //Add Request Queue with jSonObjReq
        requestQueue.add(jsonObjReq);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_car_detail_page,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_delete_id:

                //PopUp Dialog to Confirm Delete
                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_CarDetail.this);
                builder.setCancelable(true);
                builder.setTitle("Confirm Delete");
                builder.setMessage("Are you sure to delete this vehicle?");

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(!connectivity())
                        {
                            //Toast if No Internet Connection
                            Toast toast = Toast.makeText(Activity_CarDetail.this, "No Internet Connection", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        else
                        {
                            //Delete the Current Vehicle
                            deleteVehicle();
                            //Update User Registered Vehicle
                            updateRegDevice();
                        }
                    }
                });
                builder.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    // Create a BroadcastReceiver for ACTION_BOND_STATE_CHANGED.
    private final BroadcastReceiver btReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        //Set UI
                        car_conn_status_id.setText("Not Available");
                        bt_enable_button_id.setTextSize(13);
                        simple_tick_animation_id.setVisibility(View.INVISIBLE);
                        bt_enable_button_id.setEnabled(true);
                        bluetoothLeService.disconnect();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //Set UI
                        car_conn_status_id.setText("Not Connected");
                        bluetoothLeService.connect(SaveSharedPreference.getPREF_PREVIOUS_DEVICE(Activity_CarDetail.this));

                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        car_conn_status_id.setText("Turning Bluetooth off...");
                        car_conn_status_id.setText("Not Available");
                        bt_enable_button_id.setTextSize(13);
                        simple_tick_animation_id.setVisibility(View.INVISIBLE);
                        bt_enable_button_id.setEnabled(true);
                        bluetoothLeService.disconnect();
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        car_conn_status_id.setText("Turning Bluetooth on...");
                        break;
                }

            }
        }
    };



    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (Class_BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                //Echo back received data, with something inserted
                final byte[] rxBytes = CAR_DUINO_CHARACTERISTIC.getValue();
                Log.d("HELLO", String.valueOf(CAR_DUINO_CHARACTERISTIC.getValue()));
                final byte[] insertSomething = {(byte)'t'};
                byte[] txBytes = new byte[insertSomething.length + rxBytes.length];
                System.arraycopy(insertSomething, 0, txBytes, 0, insertSomething.length);
                System.arraycopy(rxBytes, 0, txBytes, insertSomething.length, rxBytes.length);

                if(CAR_DUINO_CHARACTERISTIC != null)
                {
                    CAR_DUINO_CHARACTERISTIC.setValue(txBytes);
                    bluetoothLeService.writeCharacteristic(CAR_DUINO_CHARACTERISTIC);
                    bluetoothLeService.setCharacteristicNotification(CAR_DUINO_CHARACTERISTIC,true);
                }
            }
            else if (Class_BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                // Get all the supported services and characteristics
                getGattServices(bluetoothLeService.getSupportedGattServices());

            }
            else if (Class_BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                car_conn_status_id.setText("Connected");
                bt_enable_button_id.setTextSize(0);
                simple_tick_animation_id.setVisibility(View.VISIBLE);
                simple_tick_animation_id.playAnimation();
                bt_enable_button_id.setEnabled(false);
                SaveSharedPreference.setPREF_PREVIOUS_DEVICE(Activity_CarDetail.this,device_ble_address);
            }
            else if(Class_BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                car_conn_status_id.setText("Not Connected");
                bt_enable_button_id.setEnabled(true);
                bt_enable_button_id.setTextSize(13);
                simple_tick_animation_id.setVisibility(View.INVISIBLE);
                Adapter_VehicleCard.connection_status = "Not Connected";

                bluetoothLeService.disconnect();
            }
        }
    };


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    private void getGattServices(List<BluetoothGattService> gattServices) {

        UUID UUID_HM_10 = UUID.fromString(Class_GattAttributes.CAR_DUINO_UUID);

        if (gattServices == null) return;
        String uuid;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices)
        {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, Class_GattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
            {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, Class_GattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

                //Check if it is "HM_10"
                if(uuid.equals(Class_GattAttributes.CAR_DUINO_UUID))
                {
                    CAR_DUINO_CHARACTERISTIC = gattService.getCharacteristic(UUID_HM_10);
                }
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }


    private void checkBleConnection(Class_BluetoothLeService bluetoothLeService)
    {
        //Check Connection
        switch(bluetoothLeService.getConnectionState())
        {
            //BLE CONNECTED
            case STATE_CONNECTED:
                car_conn_status_id.setText("Connected");
                bt_enable_button_id.setTextSize(0);
                simple_tick_animation_id.setVisibility(View.VISIBLE);
                simple_tick_animation_id.playAnimation();
                bt_enable_button_id.setEnabled(false);
//                scanLeDevice(true);
                SaveSharedPreference.setPREF_PREVIOUS_DEVICE(Activity_CarDetail.this,device_ble_address);
                break;

            //BLE CONNECTING
            case STATE_CONNECTING:
                car_conn_status_id.setText("Connected");
                bt_enable_button_id.setTextSize(0);
                simple_tick_animation_id.setVisibility(View.VISIBLE);
                simple_tick_animation_id.playAnimation();
                bt_enable_button_id.setEnabled(false);
//                scanLeDevice(true);
                SaveSharedPreference.setPREF_PREVIOUS_DEVICE(Activity_CarDetail.this,device_ble_address);
                break;

            //BLE DISCONNECTED
            case STATE_DISCONNECTED:
                car_conn_status_id.setText("Not Connected");
                bt_enable_button_id.setEnabled(true);
//                bt_enable_button_id.setTextSize(13);
                simple_tick_animation_id.setVisibility(View.INVISIBLE);
//                scanLeDevice(false);
                break;

            //BLE DISCONNECTING
            case STATE_DISCONNECTING:
                car_conn_status_id.setText("Not Connected");
                bt_enable_button_id.setEnabled(true);
//                bt_enable_button_id.setTextSize(13);
                simple_tick_animation_id.setVisibility(View.INVISIBLE);
//                scanLeDevice(false);
                break;

        }
    }

    /**
     * Description:
     * This method will first Check the Location Runtime Permission(Android 6.0 and above).
     * Next, by Intent to bring user to Bluetooth Page to connect to the Bluetooth Device.
     * Next, by using IntentFilter to get Bluetooth Device if Found, if Connected, if Disconnected
     * and if Disconnected Request
     * Last, register IntentFilter into a BroaccastReceiver to receive the broadcast result
     *
     * Function:
     * This method will call 'checkSelfPermission' to check the Runtime Permission for Location.
     * Next, call 'requestPermissions' to get Permission if Runtime Location not allowed
     * Next, call 'Intent' to bring to Bluetooth Settings Page
     * Next, call 'IntentFilter' to set filger actions
     * Last, call 'registerReceiver' to register Receiver
     *
     * @return VOID
     */
//    private void bluetoothPermission()
//    {
//        //Check Location Permission Android 6.0 and above
//        //If permission granted
//        if(ContextCompat.checkSelfPermission(Activity_CarDetail.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
//        {
//            //Connect to the Bluetooth Device
//            connectBTDevice();
//        }
//        else
//        {
//            //Prompt Runtime location permission
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Class_BluetoothConnectionService.REQUEST_ACCESS_COARSE_LOCATION);
//        }
//    }



    /**
     * Description:
     * This method will call check the connectivity of user device
     *
     * Function:
     * This method will call ConnectivityManager to get the NetworkType and NetworkInfo
     * to check the connection
     *
     * @return 1) True if connection is available 2) False if connection is not available
     */
    public boolean connectivity()
    {
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            return connected;
        }
        else
            connected = false;
        return connected;
    }

    @Override
    public void onBackPressed()
    {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            car_detail_image_id.setVisibility(View.VISIBLE);
            bluetooth_lock_button_id.setVisibility(View.VISIBLE);
            bluetooth_unlock_button_id.setVisibility(View.VISIBLE);

        } else {
            getFragmentManager().popBackStack();
            car_detail_image_id.setVisibility(View.VISIBLE);
            bluetooth_lock_button_id.setVisibility(View.VISIBLE);
            bluetooth_unlock_button_id.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == 1 )
        {
            if(resultCode == RESULT_OK)
            {
                car_conn_status_id.setText(data.getData().toString());
            }
        }
        else if(requestCode == 2)
        {
            if(resultCode == RESULT_OK)
            {
//                bluetoothPermission();
                Toast.makeText(Activity_CarDetail.this, "Bluetooth is Enabled", Toast.LENGTH_LONG).show();
                bluetoothLeService.connect(device_ble_address);
//                checkBleConnection(bluetoothLeService);

            }
            else
            {
                Toast.makeText(Activity_CarDetail.this, "Please enable Bluetooth", Toast.LENGTH_LONG).show();
            }
        }
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//
//        if (bluetoothLeService != null)
//        {
//            bluetoothLeService.connect(SaveSharedPreference.getPREF_PREVIOUS_DEVICE(Activity_CarDetail.this));
//            getGattServices(bluetoothLeService.getSupportedGattServices());
//            checkBleConnection(bluetoothLeService);
//
//        }
//    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            unregisterReceiver(btReceiver);
            unregisterReceiver(mGattUpdateReceiver);

        }catch (Exception e) { }
    }

    private static IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Class_BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Class_BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Class_BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Class_BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }

}
