package CarDuino.Fragment;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import CarDuino.Activity.Activity_Login;
import CarDuino.R;
import CarDuino.Services.Connection;
import CarDuino.Services.CustomRequest;
import CarDuino.Services.SaveSharedPreference;

public class Fragment_RegisterUser extends Fragment {
    //This is a Regex (Regular Expression) Combination of Username Pattern
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^" +
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 6 characters
                    "$");

    //This is a Regex (Regular Expression) Combination of Password Pattern
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    ".{6,}" +               //at least 6 characters
                    "$");

    private RequestQueue requestQueue;
    private View view;
    private TextInputLayout textLayout_name_id;
    private TextInputEditText full_name_id;
    private TextInputLayout textLayout_username_id;
    private TextInputLayout textLayout_password_id;
    private Button create_user_btn_id;
    private int newId;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view =  inflater.inflate(R.layout.fragment_new_user, container, false);
        create_user_btn_id = (Button) view.findViewById(R.id.create_user_btn_id);
        textLayout_name_id = (TextInputLayout)view.findViewById(R.id.textLayout_name_id);
        full_name_id = (TextInputEditText) view.findViewById(R.id.full_name_id);
        textLayout_username_id = (TextInputLayout)view.findViewById(R.id.textLayout_username_id);
        textLayout_password_id = (TextInputLayout)view.findViewById(R.id.textLayout_password_id);

        create_user_btn_id.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!isUsername(textLayout_username_id.getEditText().getText().toString())
                        | !isPassword(textLayout_password_id.getEditText().getText().toString()))
                {
                    return;
                }
                else
                {
                    createUserProfile();
                }
            }
        });

        return view;
    }


    private void createUserLogin()
    {
        //Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(getActivity());

        try
        {
            jsonAdd.put("user_login_id", newId);
            jsonAdd.put("username", textLayout_username_id.getEditText().getText().toString());
            jsonAdd.put("user_password", textLayout_password_id.getEditText().getText().toString());

            jsonObj.put("user",jsonAdd);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST, new Connection().getUsersUrl(), jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    SaveSharedPreference.setPREF_USER_NAME(getActivity().getApplicationContext(),response.getJSONObject(0).getString("username"));
                    createUserImage();
                    Log.e("USERNAME",response.getJSONObject(0).getString("username"));
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


    private void createUserProfile()
    {
//       Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(getActivity());
        String[] nameSplit = full_name_id.getText().toString().split("\\s+");

        try
        {
            jsonAdd.put("user_first_name", nameSplit[0]);
            jsonAdd.put("user_last_name", nameSplit[1]);
            jsonAdd.put("user_email", getArguments().getString("new_email"));
            jsonAdd.put("user_registered_devices", -1);

            jsonObj.put("user",jsonAdd);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST, new Connection().getUsersUrl(), jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    newId = response.getJSONObject(0).getInt("user_id");
                    SaveSharedPreference.setPREF_USER_ID(getActivity().getApplicationContext(),response.getJSONObject(0).getInt("user_id"));
                    SaveSharedPreference.setPREF_FIRST_NAME(getActivity().getApplicationContext(),response.getJSONObject(0).getString("user_first_name"));
                    SaveSharedPreference.setPREF_EMAIL(getActivity().getApplicationContext(),response.getJSONObject(0).getString("user_email"));
                    SaveSharedPreference.setPREF_LAST_NAME(getActivity().getApplicationContext(),response.getJSONObject(0).getString("user_last_name"));
                    SaveSharedPreference.setPREF_REG_DEVICES(getActivity().getApplicationContext(),0);
                    createUserLogin();
                    Log.e("SharedPreference:",Integer.toString(SaveSharedPreference.getPREF_USER_ID(getActivity())));
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


    private void createUserImage()
    {
//       Creating Json Object
        final JSONObject jsonObj = new JSONObject();
        JSONObject jsonAdd = new JSONObject();

        requestQueue = Volley.newRequestQueue(getActivity());

        try
        {
            jsonAdd.put("user_id", newId);
            jsonAdd.put("user_image_path", " ");


            jsonObj.put("user",jsonAdd);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST, new Connection().getUsersUrl(), jsonObj, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response)
            {
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Intent intent = new Intent(getActivity(), Activity_Login.class);
                startActivity(intent);
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


    private boolean isUsername(CharSequence target)
    {
        if(TextUtils.isEmpty(target))
        {
            textLayout_username_id.setError("Field cannot be empty");
            return false;
        }
        else if(target.length() > 15)
        {
            textLayout_username_id.setError("Username too long");
            return false;
        }
        else if(!USERNAME_PATTERN.matcher(target).matches())
        {
            textLayout_username_id.setError("Space is not allowed, at least 6 characters");
            return false;
        }
        else
        {
            textLayout_username_id.setError(null);
            return true;
        }
    }

    private boolean isPassword(CharSequence target)
    {
        if(TextUtils.isEmpty(target))
        {
            textLayout_password_id.setError("Field cannot be empty");
            return false;
        }
        else if(!PASSWORD_PATTERN.matcher(target).matches())
        {
            String errorMessage = "Password at least 1 digit, 1 lowercase,1 uppercase";
            textLayout_password_id.setError(errorMessage);
            return false;
        }
        else
        {
            textLayout_password_id.setError(null);
            return true;
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
