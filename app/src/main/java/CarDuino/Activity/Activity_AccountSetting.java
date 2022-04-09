package CarDuino.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import CarDuino.R;
import CarDuino.Services.Class_Vehicle;
import CarDuino.Services.Connection;
import CarDuino.Services.CustomRequest;
import CarDuino.Services.SaveSharedPreference;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Activity_AccountSetting
        extends AppCompatActivity
{

    private LinearLayout reg_device_list_id;
    private RequestQueue requestQueue;
    private Toolbar toolbar_id;
    private ImageView user_image_id;
    private Button profile_setting_btn_id;
    public static TextView account_name_id;
    public static TextView account_age_id;
    public static TextView account_email_id;
    private TextView account_reg_device_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        findViewByIdes();

        user_image_id.setImageBitmap(BitmapFactory.decodeFile(SaveSharedPreference.getPREF_IMAGE_PATH(this)));
        account_name_id.setText(SaveSharedPreference.getPREF_FIRST_NAME(this)+" "+SaveSharedPreference.getPREF_LAST_NAME(this));
        account_age_id.setText(Integer.toString(SaveSharedPreference.getPREF_USER_AGE(this)));
        account_email_id.setText(SaveSharedPreference.getPREF_EMAIL(this));
        account_reg_device_id.setText(Integer.toString(SaveSharedPreference.getPREF_REG_DEVICES(this)));


        //Set toolbar as ActionBar
        setSupportActionBar(toolbar_id);

        //Back Button
        toolbar_id.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        //Edit Profile Button
        profile_setting_btn_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Intent enableBtIntent = new Intent(Activity_AccountSetting.this, Activity_EditProfile.class);
                startActivityForResult( enableBtIntent, 2);
            }
        });


        //Get User Info
        getUser_Information();
        //Set User Registered Device;
        getUserRegDevice();

    }

    private void findViewByIdes()
    {
        setContentView(R.layout.activity_account__setting);
        toolbar_id = findViewById(R.id.toolbar_id);
        user_image_id = findViewById(R.id.user_image_id);
        profile_setting_btn_id = findViewById(R.id.profile_setting_btn_id);
        account_name_id = findViewById(R.id.account_name_id);
        account_age_id = findViewById(R.id.account_age_id);
        account_email_id = findViewById(R.id.account_email_id);
        account_reg_device_id = findViewById(R.id.account_reg_device_id);
        reg_device_list_id = findViewById(R.id.reg_device_list_id);
    }

    /**
     * Description:
     * This method will get the User Registered Devices from the List and display.
     *
     * Function:
     * This method will get Registered Device from SaveSharedPreference and display the value
     * and call List Array to get the Vehicle Name and display
     *
     * @return: VOID
     */
    private void getUserRegDevice()
    {
        //Linear Layout Configuration
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(100);

        //Check User Registered Device
        if(SaveSharedPreference.getPREF_REG_DEVICES(Activity_AccountSetting.this) <= 0)
        {
            account_reg_device_id.setText("0");
        }
        else
        {
            //Check if the LinearLayout is more than 0, clear all the Views and reset the Vehicle Name
            if(reg_device_list_id.getChildCount() > 0)
            {
                //Remove all the items in the LinearLayout
                reg_device_list_id.removeAllViews();
                for(int i = 0; i < SaveSharedPreference.getPREF_REG_DEVICES(this); i++)
                {
                    //Create the TextVeiw in Activity
                    final TextView car_name = new TextView(Activity_AccountSetting.this);
                    //Set the text in the TextView with RecyclerView ArrayList
                    car_name.setText(Activity_Main.classVehicle_list.get(i).getCar_name());
                    //Set the Text Size
                    car_name.setTextSize(12);
                    //Set the TextView in the LinearLayout
                    car_name.setLayoutParams(params);
                    //Display the TextView
                    reg_device_list_id.addView(car_name);
                }
            }
            else
            {
                //Reset the Vehicle Name
                for(int i = 0; i < SaveSharedPreference.getPREF_REG_DEVICES(this); i++)
                {
                    //Create the TextVeiw in Activity
                    final TextView car_name = new TextView(Activity_AccountSetting.this);
                    //Set the text in the TextView with RecyclerView ArrayList
                    car_name.setText(Activity_Main.classVehicle_list.get(i).getCar_name());
                    //Set the Text Size
                    car_name.setTextSize(12);
                    //Set the TextView in the LinearLayout
                    car_name.setLayoutParams(params);
                    //Display the TextView
                    reg_device_list_id.addView(car_name);
                }
            }
        }
    }

    /**
     * Description:
     * This Method will Perform 'Get' function on the current Vehicle
     * That the user had selected.
     *
     * Function:
     * The Method will select the user_id of the RecyclerView Adapter and call JSON
     * and API to pass the user_id into the database to perform Get
     *
     * @return: VOID
     */
    public void getUser_Information()
    {
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(this);


        try {
            jsonAdd.put("user_id",SaveSharedPreference.getPREF_USER_ID(this));

            jsonObj.put("user",jsonAdd);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST, new Connection().getUsersUrl(), jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {

                try
                {
                    //Set the Result into SaveSharedPreference and Display
                    SaveSharedPreference.setPREF_IMAGE_PATH(Activity_AccountSetting.this,response.getJSONObject(0).getString("user_image_path"));
                    SaveSharedPreference.setPREF_FIRST_NAME(Activity_AccountSetting.this,response.getJSONObject(0).getString("user_first_name"));
                    SaveSharedPreference.setPREF_LAST_NAME(Activity_AccountSetting.this,response.getJSONObject(0).getString("user_last_name"));
                    SaveSharedPreference.setPREF_USER_AGE(Activity_AccountSetting.this,response.getJSONObject(0).getInt("user_age"));
                    SaveSharedPreference.setPREF_REG_DEVICES(Activity_AccountSetting.this,response.getJSONObject(0).getInt("user_registered_devices"));
                    account_name_id.setText(response.getJSONObject(0).getString("user_first_name")+" "+response.getJSONObject(0).getString("user_last_name"));
                    account_email_id.setText(response.getJSONObject(0).getString("user_email"));
                    account_age_id.setText(Integer.toString(response.getJSONObject(0).getInt("user_age")));
                    account_reg_device_id.setText(Integer.toString(response.getJSONObject(0).getInt("user_registered_devices")));
                    user_image_id.setImageBitmap(BitmapFactory.decodeFile(response.getJSONObject(0).getString("user_image_path")));

                    //LinearLayout Configuration
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMarginStart(100);

                    //Check User Registered Device
                    if(SaveSharedPreference.getPREF_REG_DEVICES(Activity_AccountSetting.this) <= 0)
                    {
                        account_reg_device_id.setText("0");
                    }
                    else
                    {
                        //Check if the LinearLayout is more than 0, clear all the Views and reset the Vehicle Name
                        if(reg_device_list_id.getChildCount() > 0)
                        {
                            reg_device_list_id.removeAllViews();
                            for(int i = 0; i < SaveSharedPreference.getPREF_REG_DEVICES(Activity_AccountSetting.this); i++)
                            {
                                final TextView car_name = new TextView(Activity_AccountSetting.this);
                                car_name.setText(Activity_Main.classVehicle_list.get(i).getCar_name());
                                car_name.setTextSize(12);
                                car_name.setLayoutParams(params);

                                reg_device_list_id.addView(car_name);
                            }
                        }
                        //Reset the Vehicle Name
                        else
                        {
                            for(int i = 0; i < SaveSharedPreference.getPREF_REG_DEVICES(Activity_AccountSetting.this); i++)
                            {
                                final TextView car_name = new TextView(Activity_AccountSetting.this);
                                car_name.setText(Activity_Main.classVehicle_list.get(i).getCar_name());
                                car_name.setTextSize(12);
                                car_name.setLayoutParams(params);

                                reg_device_list_id.addView(car_name);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("Error!","Error Retrieving Data!!!!");
            }
        })
        {
            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }

                return volleyError;
            }
        };

        requestQueue.add(jsonObjReq);

    }


    /**
     * Description:
     * This is the onActivityResult Method to get the result of the activity_layout intent when user selected
     * the image
     *
     * Function:
     * This method will be invoke to retrieve the Image Path of the user selected and display
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 1 && resultCode == RESULT_OK && null != data)
        {
            //Get File Path
            Class_Vehicle.setSelected_image(data.getData());
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Log.e("filePathColumn",filePathColumn[0]);

            //Cursor to get the selected Image
            Cursor cursor = getContentResolver().query(Class_Vehicle.getSelected_image(), filePathColumn, null, null, null);
            cursor.moveToFirst();

            //Set Image to the Image View
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            user_image_id.setImageBitmap(BitmapFactory.decodeFile(cursor.getString(columnIndex)));
            cursor.close();
        }
        else if(requestCode == 2 && resultCode == RESULT_OK)
        {
            getUser_Information();
        }
    }
}
