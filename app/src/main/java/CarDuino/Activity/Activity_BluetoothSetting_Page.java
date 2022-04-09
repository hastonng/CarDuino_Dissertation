package CarDuino.Activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import CarDuino.Services.Class_BLE_BroadcastReceiver;
import CarDuino.Services.Class_BluetoothLeService;
import CarDuino.Services.Connection;
import CarDuino.Services.CustomRequest;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import CarDuino.R;
import CarDuino.Services.Adapter_BluetoothList;
import CarDuino.Services.Class_BluetoothConnectionService;
import CarDuino.Services.SaveSharedPreference;


public class Activity_BluetoothSetting_Page extends AppCompatActivity
    implements AdapterView.OnItemClickListener
{
    private Toolbar toolbar;
    private TextView bluetooth_text_id;
    private ListView bluetooth_list_id;
    private Button rescan_btn_id;
    private Button done_btn_id;

    //Bluetooth
    public ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private HashMap<String, BluetoothDevice>bluetoothDeviceHashMap = new HashMap<>();
    public Adapter_BluetoothList adapterBluetoothList;
    private BluetoothAdapter bluetoothAdapter;
    private Class_BluetoothLeService bluetoothLeService;
    private Class_BLE_BroadcastReceiver ble_broadcastReceiver;
    private BluetoothLeScanner bluetoothLeScanner;
    private int signalStrength = -75;
    private ScanCallback scanCallBack;
    private boolean scanningState;
    private Handler handler;
    private static final long SCAN_PERIOD = 7500; //<-- scan for 10 seconds
//    Class_BluetoothConnectionService btService;

    //JSON
    private RequestQueue requestQueue;
    private String vehiclePath;
    private String device_ble_address;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_setting);
        toolbar = (Toolbar) findViewById(R.id.toolbar_id);
        bluetooth_text_id = (TextView) findViewById(R.id.bluetooth_text_id);
        bluetooth_list_id = (ListView) findViewById(R.id.bluetooth_list_id);
        rescan_btn_id = (Button) findViewById(R.id.rescan_btn_id);
        done_btn_id = (Button) findViewById(R.id.done_btn_id);

        //Set toolbar as ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pair device");
        handler = new Handler();


        // Check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "Device does not support BLE.", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Initialize
        ble_broadcastReceiver = new Class_BLE_BroadcastReceiver(getApplicationContext());

        // Initializes a Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this,"Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //Toolbar Back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Implemented by activity_layout
            }
        });

        //Done Button
        done_btn_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(getIntent().getStringExtra("vehicle_image_path").equals(" "))
                {
                    vehiclePath = " ";
                    createVehicle();
                    createImage();
                    updateRegDevice();
                    //Clear the RecyclerView ArrayList
                    Activity_Main.classVehicle_list.clear();
                    //Bring user to Main Page
                    Intent myIntent = new Intent(Activity_BluetoothSetting_Page.this, Activity_Login.class);
                    Activity_BluetoothSetting_Page.this.startActivity(myIntent);
                    //Finish Activity
                    finish();
                }
                else if(device_ble_address.isEmpty())
                {
                    Toast.makeText(Activity_BluetoothSetting_Page.this, "Please pair Bluetooth Device", Toast.LENGTH_LONG).show();
                }
                else
                {
                    vehiclePath = getIntent().getStringExtra("vehicle_image_path");
                    createVehicle();
                    createImage();
                    updateRegDevice();
                    //Clear the RecyclerView ArrayList
                    Activity_Main.classVehicle_list.clear();
                    //Bring user to Main Page
                    Intent myIntent = new Intent(Activity_BluetoothSetting_Page.this, Activity_Login.class);
                    Activity_BluetoothSetting_Page.this.startActivity(myIntent);
                    //Finish Activity
                    finish();
                }
                //Create the Vehicle and send to Database

            }
        });

        //Bluetooth Rescan for bluetooth Device
        rescan_btn_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Clear Everything on the list
                bluetoothPermission();
            }
        });

        bluetoothPermission();

        //Set item clickable
        bluetooth_list_id.setOnItemClickListener(Activity_BluetoothSetting_Page.this);

        Intent gattServiceIntent = new Intent(this, Class_BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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
    private void bluetoothPermission()
    {
        //Check Location Permission Android 6.0 and above
        //If permission granted
        if(ContextCompat.checkSelfPermission(Activity_BluetoothSetting_Page.this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED)
        {
                scanLeDevice(true);
        }
        else
        {
            //Prompt Runtime location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, Class_BluetoothConnectionService.REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    private void scanLeDevice(final boolean enable)
    {
        if (enable)
        {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run() {
                    scanningState = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    bluetooth_list_id.setAdapter(adapterBluetoothList);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            scanningState = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            scanningState = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            bluetooth_list_id.setAdapter(adapterBluetoothList);
        }
        invalidateOptionsMenu();
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback()
            {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {

                            if(!bluetoothDeviceHashMap.containsKey(device.getName()) && rssi > signalStrength)
                            {
                                bluetoothDeviceHashMap.put(device.getName(),device);
                                bluetoothDevices.add(device);

                                adapterBluetoothList = new Adapter_BluetoothList(Activity_BluetoothSetting_Page.this,R.layout.view_bluetooth_list,bluetoothDevices);
                                adapterBluetoothList.notifyDataSetChanged();
                            }
                            else
                            {
                                adapterBluetoothList = new Adapter_BluetoothList(Activity_BluetoothSetting_Page.this,R.layout.view_bluetooth_list,bluetoothDevices);
                                adapterBluetoothList.notifyDataSetChanged();
                            }
                            adapterBluetoothList.notifyDataSetChanged();
                        }
                    });
                }
            };

//    // Create a BroadcastReceiver for ACTION_FOUND.
//    private final BroadcastReceiver foundDevice_Receiver = new BroadcastReceiver()
//    {
//        public void onReceive(Context context, Intent intent)
//        {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action))
//            {
//                // Discovery has found a device. Get the BluetoothDevice
//                // object and its info from the Intent.
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                bluetoothDevices.add(device);
//                //Set Discovered Bluetooth Device on the ListView Adapter
//                adapterBluetoothList = new Adapter_BluetoothList(context,R.layout.view_bluetooth_list,bluetoothDevices);
//                bluetooth_list_id.setAdapter(adapterBluetoothList);
//            }
//        }
//    };
//
//
//    // Create a BroadcastReceiver for ACTION_BOND_STATE_CHANGED.
//    private final BroadcastReceiver bondStateChanged = new BroadcastReceiver()
//    {
//        public void onReceive(Context context, Intent intent)
//        {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
//            {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//               if(device.getBondState() == BluetoothDevice.BOND_BONDED)
//               {
//                   Toast.makeText(Activity_BluetoothSetting_Page.this, "Paired", Toast.LENGTH_LONG).show();
//                   //Set temp bluetooth device to the current selected bluetooth device
//                   btDevice = device;
//                   //Start the connection to the current bluetooth device
//                   startBtConnection(btDevice,Class_BluetoothConnectionService.uuid);
//                   SaveSharedPreference.setPREF_PREVIOUS_DEVICE(Activity_BluetoothSetting_Page.this,btDevice.getName());
//
//
//               }
//               if(device.getBondState() == BluetoothDevice.BOND_BONDING)
//               {
//                   Toast.makeText(Activity_BluetoothSetting_Page.this, "Pairing", Toast.LENGTH_LONG).show();
//               }
//               if(device.getBondState() == BluetoothDevice.BOND_NONE)
//               {
//                   Toast.makeText(Activity_BluetoothSetting_Page.this, "Not Paired", Toast.LENGTH_LONG).show();
//               }
//            }
//        }
//    };



    /**
     * Description:
     * This Method will Perform 'PUT' to update the User Registered Device value into Database
     *
     * Function:
     * The Method will select the user_registered_devices,user_id
     * and call JSON and API to pass the values into the database to perform PUT
     *
     * @return: VOID
     */
    private void updateRegDevice()
    {
        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(this);

        try
        {
            //if User Registered Device in SharedPreference is less than 0
            if(SaveSharedPreference.getPREF_REG_DEVICES(this) < 0)
            {
                //{"user_id":,"user_registered_devices:"}
                jsonAdd.put("user_registered_devices",1);
                jsonAdd.put("user_id",SaveSharedPreference.getPREF_USER_ID(this));
            }
            else
            {
                //{"user_id":,"user_registered_devices:"}
                jsonAdd.put("user_registered_devices",SaveSharedPreference.getPREF_REG_DEVICES(this) + 1);
                jsonAdd.put("user_id",SaveSharedPreference.getPREF_USER_ID(this));
            }

            //{"device":{"user_id": "YOUR USER ID"}}
            jsonObj.put("user",jsonAdd);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.PUT, new Connection().getUsersUrl(), jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
                Log.e("WHere am i ","I AM  IN RESPONSE!!!!");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //Set the New Vehicle Registered Value into SharedPreference
                SaveSharedPreference.setPREF_REG_DEVICES(Activity_BluetoothSetting_Page.this,SaveSharedPreference.getPREF_REG_DEVICES(Activity_BluetoothSetting_Page.this) + 1);
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
     * This Method will Perform 'POST' to create a new Vehicle in Database
     *
     * Function:
     * The Method will select the user_id,user_duino_key,vehicle_name,vehicle_model
     * and call JSON and API to pass the user_id,user_duino_key,vehicle_name,vehicle_model
     * into the database to perform POST
     *
     * @return: VOID
     */
    private void createVehicle()
    {
        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(this);

        try
        {
            //{"user_id":,user_duino_key:,vehicle_name:,vehicle_model:}
            jsonAdd.put("user_id",SaveSharedPreference.getPREF_USER_ID(this));
            jsonAdd.put("user_duino_key",getIntent().getStringExtra("user_duino_key"));
            jsonAdd.put("vehicle_name",getIntent().getStringExtra("vehicle_name"));
            jsonAdd.put("vehicle_model",getIntent().getStringExtra("vehicle_model"));
            jsonAdd.put("device_ble_address",device_ble_address);

            //{"device":{"user_id": "YOUR USER ID"}}
            jsonObj.put("device",jsonAdd);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST, new Connection().getDevicesUrl(), jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {

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
     * This Method will Perform 'POST' to create a new Vehicle Image data in Database
     *
     * Function:
     * The Method will select the user_id,vehicle_image_path
     * and call JSON and API to pass the user_id,vehicle_image_path into the database to perform POST
     *
     * @return: VOID
     */
    private void createImage()
    {
        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(this);


        try
        {
            //{"user_id":,"vehicle_image_path:"}
            jsonAdd.put("user_id",SaveSharedPreference.getPREF_USER_ID(this));
            jsonAdd.put("vehicle_image_path",vehiclePath);

            //{"device":{"user_id": "YOUR USER ID"}}
            jsonObj.put("device",jsonAdd);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST, new Connection().getDevicesUrl(), jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
//        //Stop Discovery Mode
//        bluetoothAdapter.cancelDiscovery();
//
//
//        //Pair with the device
//        bluetoothDevices.get(position).createBond();
//        btDevice = bluetoothDevices.get(position);
//        btService = new Class_BluetoothConnectionService(Activity_BluetoothSetting_Page.this);

//        Log.d("Name",deviceName);

        final BluetoothDevice device = adapterBluetoothList.getDevice(position);
        device_ble_address = device.getAddress();
        SaveSharedPreference.setPREF_PREVIOUS_DEVICE(Activity_BluetoothSetting_Page.this,device.getAddress());
        bluetoothLeService.connect(device.getAddress());

    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service)
        {
            bluetoothLeService = ((Class_BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e("TAG: ", "Unable to initialize Bluetooth");
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            bluetoothLeService = null;
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == 1 && resultCode == Activity.RESULT_CANCELED)
        {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }

        // Initializes list view adapter.
        adapterBluetoothList = new Adapter_BluetoothList(this,R.layout.view_bluetooth_list,bluetoothDevices);
        scanLeDevice(true);
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        adapterBluetoothList.clear();
    }
}
