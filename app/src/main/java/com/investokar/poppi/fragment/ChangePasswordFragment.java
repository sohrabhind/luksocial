package com.investokar.poppi.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import com.investokar.poppi.R;
import com.investokar.poppi.app.App;
import com.investokar.poppi.constants.Constants;
import com.investokar.poppi.util.CustomRequest;
import com.investokar.poppi.util.Helper;
import com.investokar.poppi.util.ToastWindow;

public class ChangePasswordFragment extends Fragment implements Constants {

    private ProgressDialog pDialog;

    EditText mCurrentPassword, mNewPassword;

    ToastWindow toastWindow = new ToastWindow();
    String sCurrentPassword, sNewPassword;

    private Boolean loading = false;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
setHasOptionsMenu(true);

        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_change_password, container, false);

        if (loading) {

            showpDialog();
        }

        mCurrentPassword = rootView.findViewById(R.id.currentPassword);
        mNewPassword = rootView.findViewById(R.id.newPassword);


        // Inflate the layout for this fragment
        return rootView;
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(requireActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_set_password) {
            sCurrentPassword = mCurrentPassword.getText().toString();
            sNewPassword = mNewPassword.getText().toString();

            if (checkCurrentPassword(sCurrentPassword)) {

                if (checkNewPassword(sNewPassword)) {

                    if (App.getInstance().isConnected()) {

                        accountSetPassword();

                    } else {

                        toastWindow.makeText(getText(R.string.msg_network_error), 2000);
                    }

                }
            }

            return true;
        }

        return false;
    }

    public void accountSetPassword() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_PASSWORD, null,
                response -> {

                    try {

                        if (response.has("error")) {

                            if (!response.getBoolean("error")) {

                                toastWindow.makeText(getText(R.string.msg_password_changed), 2000);
                                requireActivity().finish();

                            } else {

                                toastWindow.makeText(getText(R.string.error_password), 2000);
                            }
                        }

                    } catch (JSONException e) {

                        e.printStackTrace();

                    } finally {

                        loading = false;

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("currentPassword", sCurrentPassword);
                params.put("newPassword", sNewPassword);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public Boolean checkCurrentPassword(String password) {

        Helper helper = new Helper();

        if (password.length() == 0) {

            mCurrentPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            mCurrentPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            mCurrentPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mCurrentPassword.setError(null);

        return true;
    }

    public Boolean checkNewPassword(String password) {

        Helper helper = new Helper();

        if (password.length() == 0) {

            mNewPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            mNewPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            mNewPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mNewPassword.setError(null);

        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}