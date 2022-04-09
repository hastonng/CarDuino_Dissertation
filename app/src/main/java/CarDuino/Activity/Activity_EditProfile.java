package CarDuino.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import CarDuino.Services.Class_Vehicle;
import CarDuino.Services.Connection;
import CarDuino.Services.CustomRequest;
import CarDuino.R;
import CarDuino.Services.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Activity_EditProfile extends AppCompatActivity
{

    private RequestQueue requestQueue;
    private Toolbar toolbar_id;
    private CardView edit_image_cardview_id;
    private ImageView card_user_image_id; //<-- Edit Fragment Image
    private ImageButton toolbar_done_btn_id;
    private TextInputEditText account_first_name_id;
    private TextInputEditText account_last_name_id;
    private TextInputEditText account_age_id;
    private TextInputEditText account_email_id;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inflate the layout for this fragment
        card_user_image_id = (ImageView) findViewById(R.id.card_user_image_id);
        account_first_name_id = (TextInputEditText)findViewById(R.id.account_first_name_id);
        account_last_name_id = (TextInputEditText)findViewById(R.id.account_last_name_id);
        account_age_id = (TextInputEditText)findViewById(R.id.account_age_id);
        account_email_id = (TextInputEditText)findViewById(R.id.account_email_id);
        toolbar_id =  findViewById(R.id.toolbar_id);
        toolbar_done_btn_id = findViewById(R.id.toolbar_done_btn_id);

        //Set toolbar as ActionBar
        setSupportActionBar(toolbar_id);

        //Set the ImageView
        card_user_image_id.setImageBitmap(BitmapFactory.decodeFile(SaveSharedPreference.getPREF_IMAGE_PATH(this)));

        //Set split the String with space
        String str =  Activity_AccountSetting.account_name_id.getText().toString();
        String[] user_splitted_name = str.split("\\s+");
        //Set the first name and last name
        account_first_name_id.setText(user_splitted_name[0]);
        account_last_name_id.setText(user_splitted_name[1]);
        //Set all user detail
        account_age_id.setText(Activity_AccountSetting.account_age_id.getText());
        account_email_id.setText(Activity_AccountSetting.account_email_id.getText());
        edit_image_cardview_id = (CardView)findViewById(R.id.edit_image_cardview_id);

        //Toolbar Navigation Bar Back press
        toolbar_id.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //Return to previous Activity/Page
                onBackPressed();
            }
        });

        edit_image_cardview_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //Get image
                getUser_Profile_Image();
            }
        });

        confirmUpdate();
    }

    /**
     * Description:
     * This method will prompt user to confirm update user details
     *
     * Function:
     * This method will prompt user with Dialog Box to update user details
     *
     * @return void
     */
    private void confirmUpdate()
    {

        toolbar_done_btn_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Close all the virtual Keyboard
                closeKeyboard(Activity_EditProfile.this, account_first_name_id.getWindowToken());
                closeKeyboard(Activity_EditProfile.this, account_last_name_id.getWindowToken());
                closeKeyboard(Activity_EditProfile.this, account_age_id.getWindowToken());
                closeKeyboard(Activity_EditProfile.this, account_email_id.getWindowToken());

                //Popup Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_EditProfile.this);
                builder.setCancelable(true);
                builder.setTitle("Confirm Update");
                builder.setMessage("Are you sure to update profile?");

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
                    public void onClick(DialogInterface dialog, int which) {

                        //If no Connection
                        if (!connectivity()) {

                            //Pop user back to pervious stack
                            getFragmentManager().popBackStack();

                            //Give user a Toast Bread
                            Toast toast = Toast.makeText(Activity_EditProfile.this.getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        else
                        {
                            //Submit Update
                            send();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    /**
     * Description:
     * This Method will Perform 'PUT' to update the User Details into Database
     *
     * Function:
     * The Method will select the user_id,user_first_name,user_last_name,user_age,user_email,user_image_path
     * and call JSON and API to pass the values into the database to perform PUT
     *
     * @return: VOID
     */
    private void send()
    {
        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(Activity_EditProfile.this);

        try {
            //{"user_id","user_first_name","user_last_name","user_age","user_email","user_image_path"}
            jsonAdd.put("user_id", SaveSharedPreference.getPREF_USER_ID(Activity_EditProfile.this));
            jsonAdd.put("user_first_name", account_first_name_id.getText());
            jsonAdd.put("user_last_name", account_last_name_id.getText());
            jsonAdd.put("user_age", account_age_id.getText());
            jsonAdd.put("user_email", account_email_id.getText());
            jsonAdd.put("user_image_path", SaveSharedPreference.getPREF_IMAGE_PATH(Activity_EditProfile.this));


            //{"user":}
            jsonObj.put("user", jsonAdd);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.PUT, new Connection().getUsersUrl(), jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //Get User Data in Account Setting Activity to refresh
//                Activity_AccountSetting data = (Activity_AccountSetting) getActivity();
//                data.getUser_Information();
                //Pop user back to the previous stack
//                getFragmentManager().popBackStack();

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        }) {

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
     * This method will get check the External Storage Runtime Permission
     *
     * Function:
     * This method will check the Runtime Permission
     *
     * @return void
     */
    private void getUser_Profile_Image()
    {
        if(ContextCompat.checkSelfPermission(Activity_EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Class_Vehicle.getResultLoadImage());
        }
        else
        {
            requestStoragePermission();
        }
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
        if(ActivityCompat.shouldShowRequestPermissionRationale(Activity_EditProfile.this,Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            //This is empty because the task has already performed in the Main Page
        }
        else
        {
            //Ask User permission
            ActivityCompat.requestPermissions(Activity_EditProfile.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},Class_Vehicle.getPermissionCode());
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
        if(requestCode == Class_Vehicle.getPermissionCode())
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Bring user to select Image in External Content URI
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //Start the Activity for the selected image
                startActivityForResult(intent, Class_Vehicle.getResultLoadImage());
            }
        }
    }

    /**
     * Description:
     * This method is to get the Activity Result of the user selected image
     *
     * Function:
     * This method is get the Activity Result after user selected image
     *
     * @return void
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        //If user have selected the image
        if (requestCode == Class_Vehicle.getResultLoadImage() && resultCode == RESULT_OK && null != data)
        {
            //Set the image into the object class
            Class_Vehicle.setSelected_image(data.getData());
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            //Get the image path of the image with cursor
            Cursor cursor = Activity_EditProfile.this.getApplicationContext().getContentResolver().query(Class_Vehicle.getSelected_image(), filePathColumn, null, null, null);
            //Set the selected image into the first in the cursor
            cursor.moveToFirst();
            //Get the position/location of the image in the cursor: 0 <-- first
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //Set/Update the imagePath into SharedPreference
            SaveSharedPreference.setPREF_IMAGE_PATH(Activity_EditProfile.this,cursor.getString(columnIndex));
            //Close the cursor
            cursor.close();
            //Set the ImageView using Image Path
            card_user_image_id.setImageBitmap(BitmapFactory.decodeFile(SaveSharedPreference.getPREF_IMAGE_PATH(Activity_EditProfile.this)));
        }
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
        ConnectivityManager connectivityManager = (ConnectivityManager) Activity_EditProfile.this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
     * This method will close all Virtual keyboards
     *
     * Function:
     * This method will close all Virtual keyboards
     *
     * @param c
     * @param windowToken
     */
    public static void closeKeyboard(Context c, IBinder windowToken)
    {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }
}
