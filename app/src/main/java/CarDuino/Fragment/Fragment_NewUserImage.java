package CarDuino.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import CarDuino.R;
import CarDuino.Services.Class_Vehicle;
import CarDuino.Services.SaveSharedPreference;

import static android.app.Activity.RESULT_OK;

public class Fragment_NewUserImage extends Fragment
{
    private OnFragmentInteractionListener mListener;
    private View view;
    private CardView edit_image_cardview_id;
    private ImageView card_user_image_id;
    private Button skip_btn_id;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
//        view =  inflater.inflate(R.layout.fragment_new_user_image, container, false);
        edit_image_cardview_id = (CardView)view.findViewById(R.id.edit_image_cardview_id);
        card_user_image_id = (ImageView)view.findViewById(R.id.card_user_image_id);
//        skip_btn_id = (Button)view.findViewById(R.id.skip_btn_id);

        //User Click Edit Image Circle
        edit_image_cardview_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getUser_Profile_Image();
            }
        });

        //User click Skip
        skip_btn_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
        return view;
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
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Class_Vehicle.getResultLoadImage());
            Log.e("HERE","HERE!!!");
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
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            //This is empty because the task has already performed in the Main Page
            Log.e("HERE","HERE!!!");
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
        Log.e("I AM HERE","I AM IN ACTIVITY RESULT!! 0");
        //If user have selected the image
        if (requestCode == Class_Vehicle.getResultLoadImage() && resultCode == RESULT_OK && null != data)
        {
            Log.e("I AM HERE","I AM IN ACTIVITY RESULT!! 1");
            //Set the image into the object class
            Class_Vehicle.setSelected_image(data.getData());
            Log.e("I AM HERE","I AM IN ACTIVITY RESULT!! 2");
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Log.e("I AM HERE","I AM IN ACTIVITY RESULT!! 3");
            //Get the image path of the image with cursor
            Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(Class_Vehicle.getSelected_image(), filePathColumn, null, null, null);
            Log.e("I AM HERE","I AM IN ACTIVITY RESULT!! 4");
            //Set the selected image into the first in the cursor
            cursor.moveToFirst();
            Log.e("I AM HERE","I AM IN ACTIVITY RESULT!! 5");
            //Get the position/location of the image in the cursor: 0 <-- first
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            Log.e("I AM HERE","I AM IN ACTIVITY RESULT!! 6");
            //Set/Update the imagePath into SharedPreference
            SaveSharedPreference.setPREF_IMAGE_PATH(getActivity(),cursor.getString(columnIndex));
            Log.e("I AM HERE","I AM IN ACTIVITY RESULT!! 7");
            //Set the ImageView using Image Path
            card_user_image_id.setImageBitmap(BitmapFactory.decodeFile(getString(columnIndex)));
            Log.e("I AM HERE","I AM IN ACTIVITY RESULT!! 8");
            //Close the cursor
            cursor.close();
        }
        else {
            Log.e("I AM HERE","I AM IN ELSE ACTIVITY RESULT!!");

        }
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
    }
}
