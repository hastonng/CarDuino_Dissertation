package CarDuino.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import CarDuino.Services.Connection;
import CarDuino.Services.CustomRequest;
import CarDuino.R;
import CarDuino.Services.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Activity_Login extends AppCompatActivity {

    //variable
    private RelativeLayout login_relative_id;
    private RelativeLayout root_layout_id;
    private PopupWindow popUp;
    private EditText username_id;
    private EditText password_text_id;
    private Button dismiss_button_field;
    private Button login_btn_id;
//    private TextView login_help_id;
    private TextView first_time_id;
    private String userName;
    private String password;
    private RequestQueue requestQueue;
    private AnimationDrawable animationDrawable;
    private ProgressBar progress_bar_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(SaveSharedPreference.getPREF_USER_ID(Activity_Login.this) != 0)
        {
            Intent myIntent = new Intent(Activity_Login.this, Class_SplashScreen.class);
            Activity_Login.this.startActivity(myIntent);
        }
        else
        {
            setContentView(R.layout.activity_login);
            popUp = new PopupWindow(this);


            //Getting fields
            login_relative_id = findViewById(R.id.login_relative_id);
            root_layout_id = findViewById(R.id.root_layout_id);
            username_id = findViewById(R.id.username_id);
            password_text_id = findViewById(R.id.password_text_id);
//            login_help_id = findViewById(R.id.login_help_id);
            first_time_id = findViewById(R.id.first_time_id); // <-- First Time Login
            login_btn_id = findViewById(R.id.login_btn_id);
            progress_bar_id = findViewById(R.id.progress_bar_id);


            animationDrawable = (AnimationDrawable) login_relative_id.getBackground();
            animationDrawable.setEnterFadeDuration(4500);
            animationDrawable.setExitFadeDuration(4500);
            animationDrawable.start();

            //Dimming
            root_layout_id.getForeground().setAlpha(0);

            //Text Change Listener
            login_btn_id.setEnabled(false);
            username_id.addTextChangedListener(textField_Textwatcher);
            password_text_id.addTextChangedListener(textField_Textwatcher);

            //Login Button
            login_btn_id.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Set Progress to visible
                    progress_bar_id.setVisibility(View.VISIBLE);
                    //Call Login Function
                    login();
                }
            });

            first_time_id.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Go to Register Page
                    Intent myIntent = new Intent(Activity_Login.this, Activity_NewUser.class);
                    Activity_Login.this.startActivity(myIntent);
                }
            });
        }

    }


    /**
     * Description:
     * This Method will Perform 'Get' function on the current Vehicle
     * That the user had selected.
     *
     * Function:
     * The Method will select the username and user_password of the user key-in and call JSON
     * and API to pass the username and user_password into the database to perform Get
     *
     * @return: VOID
     */
    public void login()
    {
        //Close Keyboard
        closeKeyboard();
        //Hide the Text in Button
        login_btn_id.setTextSize(0);

        //Get the user key in data from field
        userName = username_id.getText().toString();
        password = password_text_id.getText().toString();

        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();


        //Getting this context: Activity_Login
        requestQueue = Volley.newRequestQueue(this);



        try {
            //{"username","user_password"}
            jsonAdd.put("username",userName);
            jsonAdd.put("user_password",password);

            //{"user":{"username,"user_password"}}
            jsonObj.put("user",jsonAdd);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,new Connection().getUsersUrl(), jsonObj, new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    //Get the user_id and username
                    int user_id = response.getJSONObject(0).getInt("user_id");
                    String username = response.getJSONObject(0).getString("username");
                    //Set SharedPreference user id and username
                    SaveSharedPreference.setPREF_USER_ID(Activity_Login.this, user_id);
                    SaveSharedPreference.setPREF_USER_NAME(Activity_Login.this, username);
                    getUserDetail();

                    //Log.e("User_ID: ",Integer.toString(User_Id));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                Log.e("Response Rest Response", response.toString());
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("Error Rest Response", error.toString());

                //If there is no connection
                if(error instanceof NoConnectionError)
                {
                    progress_bar_id.setVisibility(View.INVISIBLE);
                    login_btn_id.setTextSize(11);

                    final Snackbar snackbar = Snackbar.make(Activity_Login.this.findViewById(android.R.id.content), "Something went wrong, an unknown network error has occurred.", Snackbar.LENGTH_LONG);
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
                else
                    {
                        //Dim the background
                        root_layout_id.getForeground().setAlpha(128);
                        //Getting Display size
                        DisplayMetrics dm = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(dm);

                        //Getting width and height of display
                        int width = dm.widthPixels;
                        int height = dm.heightPixels;

                        //Setting up the view of Popup Window
                        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                        final View popupView = inflater.inflate(R.layout.view_invalid_user_password, null);
                        //Getting the dismiss button id in the Popup Window
                        dismiss_button_field =(Button)popupView.findViewById(R.id.dismiss_button_id);
                        //Creating the PopupWindow with the width and height
                        final PopupWindow popupWindow = new PopupWindow(popupView, (int)(width*.8),(int)(height*.3), false);

                        // show the popup window
                        // which view you pass in doesn't matter, it is only used for the window tolken
                        popupWindow.showAtLocation(Activity_Login.this.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
                        //Dismiss button action listener
                        dismiss_button_field.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                login_btn_id.setTextSize(11);
                                progress_bar_id.setVisibility(View.INVISIBLE);
                                root_layout_id.getForeground().setAlpha(0);
                                popupWindow.dismiss();
                            }
                        });
                    }
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
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjReq);
    }

    /**
     * Description:
     * This Method will Perform 'Get' function on the current Vehicle
     * That the user had selected.
     *
     * Function:
     * The Method will select the user_id of the user key-in and call JSON
     * and API to pass the uuser_id into the database to perform Get
     *
     * @return: VOID
     */
    private void getUserDetail()
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

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST, new Connection().getUsersUrl(), jsonObj, new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {

                try
                {
                    //Set result into SaveSharedPreference
                    SaveSharedPreference.setPREF_IMAGE_PATH(Activity_Login.this,response.getJSONObject(0).getString("user_image_path"));
                    SaveSharedPreference.setPREF_FIRST_NAME(Activity_Login.this,response.getJSONObject(0).getString("user_first_name"));
                    SaveSharedPreference.setPREF_LAST_NAME(Activity_Login.this,response.getJSONObject(0).getString("user_last_name"));
                    SaveSharedPreference.setPREF_USER_AGE(Activity_Login.this,response.getJSONObject(0).getInt("user_age"));
                    SaveSharedPreference.setPREF_EMAIL(Activity_Login.this,response.getJSONObject(0).getString("user_email"));
                    SaveSharedPreference.setPREF_REG_DEVICES(Activity_Login.this,response.getJSONObject(0).getInt("user_registered_devices"));

                    //Go to Main Page
                    Intent myIntent = new Intent(Activity_Login.this, Class_SplashScreen.class);
                    Activity_Login.this.startActivity(myIntent);

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
     * This Method will close Virtual KeyBoard
     *
     * Function:
     * This method will call 'InputMethodManager' to close Virtual Keyboard
     *
     * @return: VOID
     */
    private void closeKeyboard()
    {
        View v = this.getCurrentFocus();
        if(v != null)
        {
            InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
        }
    }


    /**
     * Description:
     * This method will check the Text Field so see if its empty
     *
     * Function
     * This is the TextWatcher to check the state of the Text Field
     * 'onTextChanged' will allow Login Button to be clickable if username and password is not empty
     */
    private TextWatcher textField_Textwatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            String usernameInput = username_id.getText().toString().trim();
            String passwordInput = password_text_id.getText().toString().trim();

            login_btn_id.setEnabled(!usernameInput.isEmpty() && !passwordInput.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
