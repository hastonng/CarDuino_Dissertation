package CarDuino.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class Activity_NewVehicle extends AppCompatActivity {

    private Toolbar toolbar_id;
    private Button next_button_id;
    private CardView edit_image_cardview_id;
    private ImageView vehicle_image_id;
    private TextInputEditText vehicle_name_id;
    private TextInputEditText vehicle_model_id;
    private TextInputEditText vehicle_duino_key_id;
    private String vehicle_path;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_vehicle);

        //Get R.ids
        toolbar_id = findViewById(R.id.toolbar_id);
        edit_image_cardview_id = findViewById(R.id.edit_image_cardview_id);
        next_button_id = findViewById(R.id.next_button_id);
        vehicle_image_id = findViewById(R.id.vehicle_image_id);
        vehicle_name_id = findViewById(R.id.vehicle_name_id);
        vehicle_model_id = findViewById(R.id.vehicle_model_id);
        vehicle_duino_key_id = findViewById(R.id.vehicle_duino_key_id);

        //Toolbar Back Button
        toolbar_id.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        next_button_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(vehicle_name_id.getText().toString().isEmpty() || vehicle_duino_key_id.getText().toString().isEmpty())
                {
                    //Give user a Toast bread to fil up the details
                    Toast toast = Toast.makeText(Activity_NewVehicle.this, "Field cannot be empty", Toast.LENGTH_LONG);
                    toast.show();
                }
                else if(vehicle_image_id.getDrawable() == null)
                {
                    Intent intent = new Intent(Activity_NewVehicle.this, Activity_BluetoothSetting_Page.class);
                    intent.putExtra("user_duino_key",vehicle_duino_key_id.getText().toString());
                    intent.putExtra("vehicle_name",vehicle_name_id.getText().toString());
                    intent.putExtra("vehicle_model",vehicle_model_id.getText().toString());
                    intent.putExtra("vehicle_image_path"," ");
                    Activity_NewVehicle.this.startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(Activity_NewVehicle.this, Activity_BluetoothSetting_Page.class);
                    intent.putExtra("user_duino_key",vehicle_duino_key_id.getText().toString());
                    intent.putExtra("vehicle_name",vehicle_name_id.getText().toString());
                    intent.putExtra("vehicle_model",vehicle_model_id.getText().toString());
                    intent.putExtra("vehicle_image_path",vehicle_path);
                    Activity_NewVehicle.this.startActivity(intent);
                }

            }
        });

        //Done Button onClick
//        toolbar_done_btn_id.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                //If vehicle Name and Duino Key Duino Key TextField is empty
//                if(vehicle_name_id.getText() == null || vehicle_duino_key_id.getText() == null)
//                {
//                    //Give user a Toast bread to fil up the details
//                    Toast toast = Toast.makeText(Activity_NewVehicle.this, "Please fill up the details.", Toast.LENGTH_LONG);
//                    toast.show();
//                }
//                //If Image is not selected
//                else if(vehicle_image_id.getDrawable() == null)
//                {
//                    //Set Vehicle Path to empty
//                    vehicle_path = " ";
//
//                    //Create the Vehicle and send to Database
//                    createVehicle();
//                    createImage();
//                    updateRegDevice();
//                    //Clear the RecyclerView ArrayList
//                    Activity_Main.classVehicle_list.clear();
//                    //Bring user to Main Page
//                    Intent myIntent = new Intent(Activity_NewVehicle.this, Activity_Main.class);
//                    Activity_NewVehicle.this.startActivity(myIntent);
//                    //Finish Activity
//                    finish();
//                }
//                else
//                {
//                    //Create the Vehicle and send to Database
//                    createVehicle();
//                    createImage();
//                    updateRegDevice();
//                    //Clear the RecyclerView ArrayList
//                    Activity_Main.classVehicle_list.clear();
//                    //Bring user to Main Page
//                    Intent myIntent = new Intent(Activity_NewVehicle.this, Activity_Main.class);
//                    Activity_NewVehicle.this.startActivity(myIntent);
//                    //Finish Activity
//                    finish();
//                }
//            }
//        });

        //CardView Image onClick
        edit_image_cardview_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Bring user to select Image
                getUser_Profile_Image();
            }
        });

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
        //If Read External Storage Runtime Permission granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            //Bring user to select Image in External Content URI
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //Start the Activity for the selected image
            startActivityForResult(intent, Class_Vehicle.getResultLoadImage());
        }
        else
        {
            //Ask user to grant permission
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
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            //This is empty because the task has already performed in the Main Page
        }
        else
        {
            //Ask User permission
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},Class_Vehicle.getPermissionCode());
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
            //Get the image path of the image with cursor
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = this.getContentResolver().query(Class_Vehicle.getSelected_image(), filePathColumn, null, null, null);
            //Set the selected image into the first in the cursor
            cursor.moveToFirst();
            //Get the position/location of the image in the cursor: 0 <-- first
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //Get image path using the index and store into a variable
            vehicle_path = (cursor.getString(columnIndex));
            //Close the cursor
            cursor.close();
            //Set the ImageView using Image Path
            vehicle_image_id.setImageBitmap(BitmapFactory.decodeFile(vehicle_path));
        }
    }
}
