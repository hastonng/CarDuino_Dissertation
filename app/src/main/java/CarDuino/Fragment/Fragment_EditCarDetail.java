package CarDuino.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import CarDuino.Activity.Activity_CarDetail;
import CarDuino.Services.Class_Vehicle;
import CarDuino.Services.Connection;
import CarDuino.Services.CustomRequest;
import CarDuino.R;
import CarDuino.Services.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;

public class Fragment_EditCarDetail extends Fragment
{
    private View view;
    private Toolbar toolbar;
    private RequestQueue requestQueue;
    private FrameLayout vehicle_image_layout_id;
    private ImageView edit_vehicle_image_id;
    private EditText edit_car_name_id;
    private EditText edit_car_model_id;
    private ImageButton toolbar_done_btn_id;
    private int vehicle_id;
    private String vehicle_path;

    private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        view = inflater.inflate(R.layout.fragment_edit_car_detail, container, false);
        toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        vehicle_image_layout_id = (FrameLayout) view.findViewById(R.id.vehicle_image_layout_id);
        edit_vehicle_image_id = (ImageView) view.findViewById(R.id.edit_vehicle_image_id);
        edit_car_name_id = (EditText) view.findViewById(R.id.edit_car_name_id);
        edit_car_model_id = (EditText) view.findViewById(R.id.edit_car_model_id);
        toolbar_done_btn_id = (ImageButton) view.findViewById(R.id.toolbar_done_btn_id);

        //Get the Intent value from activity_layout
        vehicle_id = getArguments().getInt("vehicle_id",0);
        vehicle_path = getArguments().getString("vehicle_image_path");

        //Set View
        edit_vehicle_image_id.setImageBitmap(BitmapFactory.decodeFile(vehicle_path));
        edit_car_name_id.setText(getArguments().getString("vehicle_name"));
        edit_car_model_id.setText(getArguments().getString("vehicle_model"));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getActivity().onBackPressed();
            }
        });

        vehicle_image_layout_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //If Read External Storage Runtime Permission granted
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
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
        });

        confirmUpdate();

       return view;
    }

    /**
     * Description:
     * This method will prompt user to confirm update vehicle details
     *
     * Function:
     * This method will prompt user with Dialog Box to update vehicle details
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

                //Close all Virtual Keyboard
                closeKeyboard(getActivity(), edit_car_name_id.getWindowToken());
                closeKeyboard(getActivity(), edit_car_model_id.getWindowToken());

                //Popup Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(true);
                builder.setTitle("Confirm Update");
                builder.setMessage("Are you sure to update profile?");


                //User if click Cancel
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

                //User if click Confirm
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        //If no Connection
                        if (!connectivity())
                        {
                            //Bring user back to previous Stack
                            getFragmentManager().popBackStack();

                            //Give user a Toast Bread
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        else
                        {
                            //Send Update
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
     * This Method will Perform 'PUT' to update the selected Vehicle into Database
     *
     * Function:
     * The Method will select the user_id,vehicle_name,vehicle_model,vehicle_id,vehicle_image_path
     * and call JSON and API to pass the values into the database to perform PUT
     *
     * @return: VOID
     */
    private void send()
    {
        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(getActivity());
        try
        {
            //{"user_id","vehicle_name","vehicle_model,"vehicle_id","vehicle_image_path"}
            jsonAdd.put("user_id",SaveSharedPreference.getPREF_USER_ID(getActivity()));
            jsonAdd.put("vehicle_name",edit_car_name_id.getText());
            jsonAdd.put("vehicle_model",edit_car_model_id.getText());
            jsonAdd.put("vehicle_id",vehicle_id);
            jsonAdd.put("vehicle_image_path",vehicle_path);

            //{"device":}
            jsonObj.put("device",jsonAdd);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.PUT, new Connection().getDevicesUrl(), jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //Get the Data in the Car Detail Activity to refresh
                Activity_CarDetail data = (Activity_CarDetail)getActivity();
                data.getData();
                //Pop user back to the previous stack
                getActivity().onBackPressed();
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
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            //This is empty because the task has already performed in the Main Page
        }
        else
        {
            //Ask User permission
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},Class_Vehicle.getPermissionCode());
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
            Log.e("filePathColumn",filePathColumn[0]);
            //Set the selected image into the first in the cursor
            Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(Class_Vehicle.getSelected_image(), filePathColumn, null, null, null);
            cursor.moveToFirst();
            //Get the position/location of the image in the cursor: 0 <-- first
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //Get image path using the index and store into a variable
            vehicle_path = (cursor.getString(columnIndex));
            //Close the cursor
            cursor.close();
            //Set the ImageView using Image Path
            edit_vehicle_image_id.setImageBitmap(BitmapFactory.decodeFile(vehicle_path));

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
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity_layout and potentially other fragments contained in that
     * activity_layout.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
