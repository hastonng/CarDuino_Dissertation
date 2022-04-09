package CarDuino.Activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import CarDuino.Services.Class_BLE_BroadcastReceiver;
import CarDuino.Services.Class_BluetoothLeService;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import CarDuino.Services.Adapter_VehicleCard;
import CarDuino.Services.Class_BluetoothConnectionService;
import CarDuino.Services.Class_Vehicle;
import CarDuino.Services.Connection;
import CarDuino.Services.CustomRequest;
import CarDuino.R;
import CarDuino.Services.SaveSharedPreference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Activity_Main
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    //Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;


    //Bluetooth Statics! DO NOT EDIT WITHOUT KNOWING
    public static BluetoothGatt bluetoothGatt;
    public static Class_BluetoothLeService bluetoothLeService;


    //JSON
    public static List<Class_Vehicle> classVehicle_list = new ArrayList<>();
    public static Adapter_VehicleCard adapterVehicleCard;
    private RequestQueue requestQueue;

    //View
    private RelativeLayout root_main_id;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    public static SwipeRefreshLayout swipe_refresh_id;
    private LinearLayout navigation_drawer_id;
    private RecyclerView recyclerView;
    private ImageView card_user_image_id;
    private ImageView expand_arrow_id;
    private TextView user_account_name_id;
    private TextView user_account_email_id;
    private FloatingActionButton add_new_device_fab;
    private int navigation_menu = 0;
    private String[] permissions = {Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

            //Check Runtime Permission for Reading External Storage if granted
            if(!hasPermission(this,permissions))
            {
                //Ask User Permission if not granted
                requestStoragePermission();
            }
            else
            {
                //Get Main Page Content if granted
                getMain_Page_Activity();
            }
    }

    /**
     * Description:
     * This method will get all the content of Activity Main Page.
     *
     * Function:
     * This method will setup all contents in Activity Main page eg: Navigation Drawer, SwipeRefresh,
     * RecyclerView for CardView
     *
     * @return void
     */
    private void getMain_Page_Activity()
    {
        //Get R.Id
        setContentView(R.layout.activity_main);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        add_new_device_fab = (FloatingActionButton) findViewById(R.id.add_new_device_fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        card_user_image_id = (ImageView) view.findViewById(R.id.card_user_image_id);
        user_account_name_id = (TextView) view.findViewById(R.id.user_account_name_id);
        user_account_email_id = (TextView) view.findViewById(R.id.user_account_email_id);
        navigation_drawer_id = (LinearLayout) view.findViewById(R.id.navigation_drawer_id);
        expand_arrow_id = (ImageView)view.findViewById(R.id.expand_arrow_id);
        navigationView.getMenu().setGroupVisible(R.id.group3,false);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        root_main_id = (RelativeLayout) findViewById(R.id.root_main_id);

        //Set toolbar as ActionBar
        setSupportActionBar(toolbar);

        //Dim
        root_main_id.getForeground().setAlpha(0);

        //Register Broadcast Receiver
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(btReceiver,makeGattUpdateIntentFilter());

        // Initializes a Bluetooth adapter.
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //Prompt user to turn on Bluetooth if not turned on
        if(!bluetoothAdapter.isEnabled())
        {
            //Request Turn Bluetooth On if not on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult( enableBtIntent, 1);
        }
        //Broadcast Receiver
//        ble_broadcastReceiver = new Class_BLE_BroadcastReceiver(getApplicationContext());

        // Press Fab button to add new device
        add_new_device_fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent myIntent = new Intent(Activity_Main.this, Activity_NewVehicle.class);
                Activity_Main.this.startActivity(myIntent);
            }
        });


        //Navigation Bar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigation_drawer_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Set navigation menu from 1st page to 2nd page
                //Check current menu page
                if(navigation_menu == 0)
                {
                    //Set image to arrow up
                    expand_arrow_id.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
                    //Hide group 1 and group 2 option
                    navigationView.getMenu().setGroupVisible(R.id.group1,false);
                    navigationView.getMenu().setGroupVisible(R.id.group2,false);
                    //Show group 3 option
                    navigationView.getMenu().setGroupVisible(R.id.group3,true);
                    //Set navigation menu page to 2nd page
                    navigation_menu = 1;

                }
                //Check current menu page
                else if(navigation_menu == 1)
                {
                    //Set image to arrow down
                    expand_arrow_id.setImageResource(R.drawable.ic_arrow_drop_down_24dp);
                    //Show Group 1 and Group 2 Option
                    navigationView.getMenu().setGroupVisible(R.id.group1,true);
                    navigationView.getMenu().setGroupVisible(R.id.group2,true);
                    //Hide group 3 Option
                    navigationView.getMenu().setGroupVisible(R.id.group3,false);
                    //Set navigation menu page to 1st page
                    navigation_menu = 0;
                }
            }
        });

        //Pull to Refresh Page Function
        pull2Refresh();

        //Set User Information
        getUserInfo();

        //Check connection available
        if(connectivity())
        {
            //clear recyclerview list array
            classVehicle_list.clear();
            //CardView Linear Layout
            adapterVehicleCard = new Adapter_VehicleCard(classVehicle_list,this);
            RecyclerView.LayoutManager rc = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(rc);
            recyclerView.setAdapter(adapterVehicleCard);
            //fetch data from database
            getData();
        }
        else
        {
            //load RecyclerView from SharedPreference if there is no Connection
            loadRecyclerList();

            //CardView Linear Layout
            adapterVehicleCard = new Adapter_VehicleCard(classVehicle_list,this);
            RecyclerView.LayoutManager rc = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(rc);
            recyclerView.setAdapter(adapterVehicleCard);

            //Give user a Snackbar :)
            final Snackbar snackbar = Snackbar.make(Activity_Main.this.findViewById(android.R.id.content), "Something went wrong, an unknown network error has occurred.", Snackbar.LENGTH_LONG);
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

        Adapter_VehicleCard.connection_status = "Not Connected";
        Intent gattServiceIntent = new Intent(this, Class_BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

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

            if(bluetoothAdapter.isEnabled())
            {
                if(SaveSharedPreference.getPREF_PREVIOUS_DEVICE(Activity_Main.this).isEmpty())
                {
                   Toast.makeText(Activity_Main.this, "Not connected", Toast.LENGTH_LONG).show();
                }
                else
                {
                    bluetoothLeService.connect(SaveSharedPreference.getPREF_PREVIOUS_DEVICE(Activity_Main.this));
                }

                if(SaveSharedPreference.getRSSI_STATE(Activity_Main.this))
                {
                    Activity_Duino_Settings.readRssi = new TimerTask()
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
                    startTask.schedule(Activity_Duino_Settings.readRssi,1000,1000);
                    SaveSharedPreference.setRSSI_STATE(Activity_Main.this,true);

                }
                else
                {
                    if(Activity_Duino_Settings.readRssi != null)
                    {
                        Activity_Duino_Settings.readRssi.cancel();
                        SaveSharedPreference.setRSSI_STATE(Activity_Main.this,false);
                    }
                }
            }
//            getGattServices(bluetoothLeService.getSupportedGattServices());
//            checkBleConnection(bluetoothLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            bluetoothLeService.disconnect();
            bluetoothLeService.close();
            bluetoothLeService = null;
        }
    };



    /**
     * Description:
     * This method is the configuration of SwipeRefreshLayout for
     * RecyclerView of CardView (Pull to Refresh Recycler View).
     *
     * Function:
     * This method will setup the colour of refresh icon animation, duration of swipe and update the
     * RecylcerView of CardView
     *
     * @return void
     */
    public void pull2Refresh()
    {
        //Get SwipeRefreshUpdate
        swipe_refresh_id= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_id);
        //Set the color of swipe icon animation
        swipe_refresh_id.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        //Set the swipe duration
        swipe_refresh_id.setProgressViewOffset(false,0,300);
        //Swipe onRefreshListener to listen for any swipes
        swipe_refresh_id.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                //Set refresh to true to refresh
                swipe_refresh_id.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //Check Connection if available
                        if(connectivity())
                        {
                            //Stop the refreshing
                            swipe_refresh_id.setRefreshing(false);
                            //Clear RecyclerView List Array
                            classVehicle_list.clear();
                            //Get User Info and RecyclerView Data Again
                            getUserInfo();
                            getData();
                        }
                        else
                        {
                            //Stop the refreshing
                            swipe_refresh_id.setRefreshing(false);
                            //Give user a Snackbar
                            final Snackbar snackbar = Snackbar.make(Activity_Main.this.findViewById(android.R.id.content), "Something went wrong, an unknown network error has occurred.", Snackbar.LENGTH_LONG);
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
                    //Delay 3 seconds to wait for RecyclerView to update
                },3000);
            }
        });
    }

    /**
     * Description:
     * This method will get user information from SharedPreferences
     *
     * Function:
     * This method will get user information data from SharedPreference and display in the view
     *
     * @return void
     */
    public void getUserInfo()
    {
        card_user_image_id.setImageBitmap(BitmapFactory.decodeFile(SaveSharedPreference.getPREF_IMAGE_PATH(this)));
        user_account_name_id.setText(SaveSharedPreference.getPREF_FIRST_NAME(this)+" "+SaveSharedPreference.getPREF_LAST_NAME(this));
        user_account_email_id.setText(SaveSharedPreference.getPREF_EMAIL(this));
    }

    /**
     * Description:
     * This Method will Perform 'Get' to retrieve information from the database of the Vehicle
     *
     * Function:
     * The Method will fetch vehicle data from database by calling
     * JSON and API to pass the user_id into the database to perform Get
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
                //Select response length
                for(int i = 0; i <= response.length(); i++)
                {
                    try
                    {
                        //Create Class_Vehicle Object for each response
                        Class_Vehicle cd = new Class_Vehicle(
                                response.getJSONObject(i).getString("vehicle_name"),
                                (response.getJSONObject(i).getString("vehicle_model")),
                                (response.getJSONObject(i).getString("vehicle_image_path")),
                                (response.getJSONObject(i).getInt("vehicle_id")),
                                (response.getJSONObject(i).getString("user_duino_key")),
                                (response.getJSONObject(i).getString("device_ble_address"))
                                );

                        //Store Olass_Vehicle object into an Array List
                        classVehicle_list.add(cd);
                        //Save Array List into SharedPreference
                        setRecyclerList();
                        //Notify the RecyclerView Adapter if there is any update
                        adapterVehicleCard.notifyDataSetChanged();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //No Connection error
                if(error instanceof NoConnectionError)
                {
                    //Give user a Snackbar
                    final Snackbar snackbar = Snackbar.make(Activity_Main.this.findViewById(android.R.id.content), "Something went wrong, an unknown network error has occurred.", Snackbar.LENGTH_LONG);
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
     * This method will request Read External Storage Runtime Permission
     *
     * Function:
     * This method will prompt user to grant permission for Read External Storage
     *
     * @return void
     */
    private void requestStoragePermission()
    {
        //If permission is not granted
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            //Popup Alert Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Permission Denied");
            builder.setMessage("Without permission this app will not function properly");

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    //Close Dialog
                    dialog.cancel();
                    //Close Application
                    finishAndRemoveTask();
                }
            });
            builder.show();
        }
        else
        {
            // Request Permission from User
            ActivityCompat.requestPermissions(this,permissions,1);
        }
    }

    /**
     * Description:
     * This method will get the result of requested Runtime Permission
     *
     * Function:
     * This method will get the result of the Runtime permission
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //If user granted runtime permission after allow
        if(requestCode == 1)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Get the Page Content
                getMain_Page_Activity();
            }
        }
    }

    /**
     * Description:
     * This method will get the user selected item in the Navigation Drawer
     *
     * Function:
     * This method will control the user selected item in the Navigation Drawer action
     *
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //User select Account Setting
        if (id == R.id.account_setting_id)
        {
            Intent myIntent = new Intent(Activity_Main.this, Activity_AccountSetting.class);
            Activity_Main.this.startActivity(myIntent);
        }
        //User select Duino Setting
        //This feature is not available
        else if (id == R.id.duino_setting_id)
        {
            Intent myIntent = new Intent(Activity_Main.this, Activity_Duino_Settings.class);
            Activity_Main.this.startActivity(myIntent);
        }
        else if (id == R.id.help_feedback_id)
        {

        }
        //User select About Us
        else if (id == R.id.about_us_id)
        {
            Intent myIntent = new Intent(Activity_Main.this, Activity_AboutUs.class);
            Activity_Main.this.startActivity(myIntent);
        }
        //User select Logout
        else if (id == R.id.logout_id)
        {
            //Popup Alert Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Logout");
            builder.setMessage("Are you sure to logout?");


            //User select cancel button
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });

            //User select OK button
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    //Clear the SharePreference
                    SaveSharedPreference.clear(Activity_Main.this);
                    //Delete the RecyclerList in SharedPreference
                    deleteRecyclerList();
                    //Bring user to the Login page
                    Intent myIntent = new Intent(Activity_Main.this, Activity_Login.class);
                    Activity_Main.this.startActivity(myIntent);
                    //Finish the Activity
                    finish();
                }
            });

            builder.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

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

    /**
     * Description:
     * This method will retrieve RecyclerView List Array from SharedPreference
     *
     * Function:
     * This method will retrieve RecyclerView List Array from SharedPreference
     *
     * @return void
     */
    private void loadRecyclerList()
    {
        //Get data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPref",MODE_PRIVATE);
        //Gson open source java sirealize and deserialize Java Objects to JSON
        Gson gson = new Gson();
        //Getting 'vehicle' from SharedPreference
        String json = sharedPreferences.getString("vehicle",null);
        //Return json String into ArrayList using Type
        Type type = new TypeToken<ArrayList<Class_Vehicle>>() {}.getType();
        //Store Arraylist of return value into classVehicle_list for RecyclerView Adapter
        classVehicle_list = gson.fromJson(json,type);
    }

    /**
     * Description:
     * This method will set RecyclerView List Array from SharedPreference
     *
     * Function:
     * This method will set RecyclerView List Array from SharedPreference
     *
     * @return void
     */
    private void setRecyclerList()
    {
        //Set data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPref",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //Gson open source java sirealize and deserialize Java Objects to JSON
        Gson gson = new Gson();
        //Convert List<>=ArrayList<> into String json format
        String json = gson.toJson(classVehicle_list);
        //Set json String into SharedPreference
        editor.putString("vehicle",json);
        //Apply SharedPreference
        editor.apply();
    }

    /**
     * Description:
     * This method will delete RecyclerView List Array from SharedPreference
     *
     * Function:
     * This method will delete RecyclerView List Array from SharedPreference
     *
     * @return void
     */
    private void deleteRecyclerList()
    {
        //Set data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPref",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //Gson open source java sirealize and deserialize Java Objects to JSON
        Gson gson = new Gson();
        //Clear the SharedPreference Array List
        classVehicle_list.clear();
        //Convert List<>=ArrayList<> into String json format
        String json = gson.toJson(classVehicle_list);
        //Set json String into SharedPreference
        editor.putString("vehicle",json);
        //Apply SharedPreference
        editor.apply();
    }



    public static boolean hasPermission(Context ctx, String... permissions)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ctx != null && permissions != null)
        {
            for(String permission: permissions)
            {
                if(ActivityCompat.checkSelfPermission(ctx,permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
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
                        Adapter_VehicleCard.connection_status = "Not Available";
                        Toast.makeText(Activity_Main.this, "Bluetooth is Off", Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        Adapter_VehicleCard.connection_status = "Not Connected";
                        Toast.makeText(Activity_Main.this, "Bluetooth is On", Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Adapter_VehicleCard.connection_status = "Turning off Bluetooth...";
                        Toast.makeText(Activity_Main.this, "Turning off Bluetooth...", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Adapter_VehicleCard.connection_status = "Turning on Bluetooth...";
                        Toast.makeText(Activity_Main.this, "Turning on Bluetooth...", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if(Class_BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                Adapter_VehicleCard.connection_status = "Not Available";
                Toast.makeText(Activity_Main.this, "Bluetooth not connected", Toast.LENGTH_LONG).show();

                bluetoothLeService.disconnect();
                bluetoothLeService.close();
            }
            else if(Class_BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                Adapter_VehicleCard.connection_status = "Connected";

                Toast.makeText(Activity_Main.this, "Bluetooth connected", Toast.LENGTH_LONG).show();

            }
        }
    };

//    private void connectBTDevice()
//    {
//
//        if(bluetoothAdapter == null)
//        {
//            Log.d("HERE","IM IN THE NULL");
//        }
//        else
//        {
//            connectPaired();
//        }
//    }


//    private void connectPaired()
//    {
//        //Get the list of Paired Device
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        //Check any paired device
//        for(BluetoothDevice deviceTemp : pairedDevices)
//        {
//            if(deviceTemp.getName().equals(SaveSharedPreference.getPREF_PREVIOUS_DEVICE(Activity_Main.this)))
//            {
//                bluetoothDevice = deviceTemp;
//                btService = new Class_BluetoothConnectionService(Activity_Main.this);
//                btService.startClient(bluetoothDevice,Class_BluetoothConnectionService.uuid);
////                SaveSharedPreference.setPREF_PREVIOUS_DEVICE(Activity_Main.this,bluetoothDevice.getName());
//
//
//            }
//        }
//    }

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
//        if(ContextCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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
     * onBackPressed function
     */
    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //if Drawer is open
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            //Close the Drawer
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

//    /**
//     * Description:
//     * Load item into the menu in Navigation Drawer
//     * @param menu
//     * @return
//     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main__page_,menu);
//        return true;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                Log.d("HERE","im in onActivityResult");
//                bluetoothPermission();
            }
        }
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
