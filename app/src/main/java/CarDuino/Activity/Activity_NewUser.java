package CarDuino.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import CarDuino.Fragment.Fragment_NewUserImage;
import CarDuino.Fragment.Fragment_RegisterUser;
import CarDuino.R;
import CarDuino.Services.Connection;
import CarDuino.Services.CustomRequest;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class Activity_NewUser extends AppCompatActivity
        implements Fragment_RegisterUser.OnFragmentInteractionListener, Fragment_NewUserImage.OnFragmentInteractionListener
{
    private RequestQueue requestQueue;
    private Button continue_button_id;
    private Button already_acc_id;
    private ImageButton clearText_btn_id;
    private TextInputLayout text_input_email;
    private TextInputEditText email_field_id;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_user);
        already_acc_id = findViewById(R.id.already_acc_id);
        continue_button_id = findViewById(R.id.continue_button_id);
        clearText_btn_id = findViewById(R.id.clearText_btn_id);
        text_input_email = findViewById(R.id.text_input_email);
        email_field_id = findViewById(R.id.email_field_id);
        email_field_id.addTextChangedListener(textField_Textwatcher);


        //Continue Button
        continue_button_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Clear Focus
                text_input_email.clearFocus();
                email_field_id.clearFocus();

                //Hide Virtual Keyboard
                InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(),0);

                if(isValidEmail(email_field_id.getText().toString().trim()))
                {
                    //Check existing email
                    checkEmail(new Connection().getUsersUrl()+"/"+email_field_id.getText().toString());
                }
                else
                {
                    //Set Error Invalid email
                    text_input_email.setError("Invalid email. Please try Again.");
                }
            }
        });

        clearText_btn_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                email_field_id.setText("");
                clearText_btn_id.setVisibility(View.GONE);
            }
        });
        already_acc_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Go to Login Page
                Intent myIntent = new Intent(Activity_NewUser.this, Activity_Login.class);
                Activity_NewUser.this.startActivity(myIntent);
                finish();
            }
        });

    }

    /**
     * Description:
     * This Method will Perform 'Get' function on the current Vehicle
     * That the user had selected.
     *
     * Function:
     * The Method will select call API to perform 'Get' Function to get data
     *
     * @return: VOID
     */
    private void checkEmail(String url)
    {
        requestQueue = Volley.newRequestQueue(Activity_NewUser.this);
        //Get Method
        CustomRequest jsonObjReq = new CustomRequest(url, new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    if(response.getJSONObject(0).getString("user_email").equals(email_field_id.getText().toString()))
                    {

                        final Snackbar snackbar = Snackbar.make(Activity_NewUser.this.findViewById(android.R.id.content), "Looks like you already have an Account. Please login with the email.", Snackbar.LENGTH_LONG);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {

                //Set Clear TextButton gone
                clearText_btn_id.setVisibility(View.GONE);

                //Pass data to Fragment
                Bundle bundle = new Bundle();
                bundle.putString("new_email", email_field_id.getText().toString());
                // set Fragment Class Arguments
                Fragment_RegisterUser fragobj = new Fragment_RegisterUser();
                fragobj.setArguments(bundle);

                //Disable the email field and button in activity
                email_field_id.setEnabled(false);
                continue_button_id.setEnabled(false);

                //Set Fragment Animation
                FragmentManager mFragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.setCustomAnimations(R.anim.fragment_enter_anim,R.anim.fragment_exit_anim,R.anim.fragment_popenter_anim,R.anim.fragment_popexit_anim);
                //Go to Fragment
                ft.addToBackStack("fragment");
                ft.add(R.id.fragment_container,fragobj);
                ft.commit();
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

    private TextWatcher textField_Textwatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            String usernameInput = email_field_id.getText().toString().trim();

            continue_button_id.setEnabled(!usernameInput.isEmpty());
            clearText_btn_id.setVisibility(View.VISIBLE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0)
        {
            getFragmentManager().popBackStack();
            Intent myIntent = new Intent(Activity_NewUser.this, Activity_Login.class);
            Activity_NewUser.this.startActivity(myIntent);
            finish();
        }
        else
            {
                super.onBackPressed();
        }
    }

    private static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }


}
