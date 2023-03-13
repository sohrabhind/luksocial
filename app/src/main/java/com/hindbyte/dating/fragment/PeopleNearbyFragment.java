package com.hindbyte.dating.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.LocationActivity;
import com.hindbyte.dating.activity.ProfileActivity;
import com.hindbyte.dating.adapter.AdvancedPeopleListAdapter;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.Profile;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PeopleNearbyFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;

    Menu MainMenu;

    private RecyclerView mRecyclerView;
    private NestedScrollView mNestedView;

    TextView mMessage, mDetails;
    ImageView mSplash;

    SwipeRefreshLayout mItemsContainer;

    LinearLayout mSpotLight, mPermissionSpotlight;

    Button mGrantPermission;

    private ArrayList<Profile> itemsList;
    private AdvancedPeopleListAdapter itemsAdapter;

    private int itemId = 0;
    private int arrayLength = 0;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;
    private Boolean restore = false;
    private Boolean spotlight = true;

    private int distance = 1000;      // im miles
    private int gender = 3; // gender:  0 = male, 1= female, 2 = secret, 3 = all

    public PeopleNearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new AdvancedPeopleListAdapter(getActivity(), itemsList);

            viewMore = savedInstanceState.getBoolean("viewMore");
            restore = savedInstanceState.getBoolean("restore");
            spotlight = savedInstanceState.getBoolean("spotlight");
            itemId = savedInstanceState.getInt("itemId");
            distance = savedInstanceState.getInt("distance");

            gender = savedInstanceState.getInt("gender");

        } else {

            itemsList = new ArrayList<Profile>();
            itemsAdapter = new AdvancedPeopleListAdapter(getActivity(), itemsList);

            restore = false;
            spotlight = true;
            itemId = 0;

            readData();
        }

        // Get Location

        LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            mFusedLocationClient.getLastLocation().addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    if (task.isSuccessful() && task.getResult() != null) {

                        mLastLocation = task.getResult();

                        Log.d("GPS", "PeopleNearby onCreate" + mLastLocation.getLatitude());
                        Log.d("GPS", "PeopleNearby onCreate" + mLastLocation.getLongitude());

                        App.getInstance().setLat(mLastLocation.getLatitude());
                        App.getInstance().setLng(mLastLocation.getLongitude());

                    } else {

                        Log.d("GPS", "getLastLocation:exception", task.getException());
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_people_nearby, container, false);

        mItemsContainer = rootView.findViewById(R.id.container_items);
        mItemsContainer.setOnRefreshListener(this);

        mMessage = rootView.findViewById(R.id.message);
        mSplash = rootView.findViewById(R.id.splash);

        mSpotLight = rootView.findViewById(R.id.spotlight);
        mDetails = rootView.findViewById(R.id.openLocationSettings);

        mPermissionSpotlight = rootView.findViewById(R.id.permission_spotlight);
        mGrantPermission = rootView.findViewById(R.id.grantPermissionBtn);

        mNestedView = rootView.findViewById(R.id.nested_view);

        mRecyclerView = rootView.findViewById(R.id.recycler_view);

        final LinearLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getGridSpanCount(getActivity()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(itemsAdapter);

        itemsAdapter.setOnItemClickListener(new AdvancedPeopleListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Profile item, int position) {

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", item.getId());
                startActivity(intent);
            }
        });

        mRecyclerView.setNestedScrollingEnabled(false);

        mNestedView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY < oldScrollY) { // up


                }

                if (scrollY > oldScrollY) { // down


                }

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {

                    if (!loadingMore && (viewMore) && !(mItemsContainer.isRefreshing())) {

                        mItemsContainer.setRefreshing(true);

                        loadingMore = true;

                        getItems();
                    }
                }
            }
        });


        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        mDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), LocationActivity.class);
                startActivityForResult(i, 101);
            }
        });


        mGrantPermission.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)){

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                    } else {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
                    }
                }
            }
        });

        updateSpotLight();

        if (!restore && App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                updateSpotLight();

            } else {

                showMessage(getText(R.string.msg_loading_2).toString());

                getItems();
            }
        }


        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateSpotLight() {

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)){

                showPermissionSpotlight();
                hideNoLocationSpotlight();
                hideItemsContainer();
                hideMessage();

            } else {

                showPermissionSpotlight();
                hideNoLocationSpotlight();
                hideItemsContainer();
                hideMessage();
            }

        } else {

            hidePermissionSpotlight();

            if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

                hidePermissionSpotlight();
                hideNoLocationSpotlight();
                showItemsContainer();

            } else {

                showNoLocationSpotlight();
                hideItemsContainer();
                hideMessage();
            }
        }

        getActivity().invalidateOptionsMenu();
    }

    public void showItemsContainer() {

        mItemsContainer.setVisibility(View.VISIBLE);
    }

    public void hideItemsContainer() {

        mItemsContainer.setVisibility(View.GONE);
    }

    public void showPermissionSpotlight() {

        mPermissionSpotlight.setVisibility(View.VISIBLE);
    }

    public void showNoLocationSpotlight() {

        mSpotLight.setVisibility(View.VISIBLE);
    }

    public void hidePermissionSpotlight() {

        mPermissionSpotlight.setVisibility(View.GONE);
    }

    public void hideNoLocationSpotlight() {

        mSpotLight.setVisibility(View.GONE);
    }

    public void updateItems() {

        if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

            showMessage(getText(R.string.msg_loading_2).toString());

            itemId = 0;

            getItems();
        }
    }

    @Override
    public void onStart() {

        super.onStart();

        updateSpotLight();
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            itemId = 0;

            getItems();

        } else {

            mItemsContainer.setRefreshing(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == getActivity().RESULT_OK) {

            updateSpotLight();

            updateItems();

        } else if (requestCode == 10001 && resultCode == getActivity().RESULT_OK) {

            updateSpotLight();

            updateItems();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                        mFusedLocationClient.getLastLocation().addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {

                                if (task.isSuccessful() && task.getResult() != null) {

                                    mLastLocation = task.getResult();

                                    Log.d("GPS", "PeopleNearby onComplete" + mLastLocation.getLatitude());
                                    Log.d("GPS", "PeopleNearby onComplete" + mLastLocation.getLongitude());

                                    App.getInstance().setLat(mLastLocation.getLatitude());
                                    App.getInstance().setLng(mLastLocation.getLongitude());

                                } else {

                                    Log.d("GPS", "getLastLocation:exception", task.getException());
                                }

                                updateSpotLight();

                                updateItems();
                            }
                        });
                    }

                    updateSpotLight();

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                        showNoLocationPermissionSnackbar();
                    }
                }

                return;
            }
        }
    }

    public void showNoLocationPermissionSnackbar() {

        Snackbar.make(getView(), getString(R.string.label_no_location_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(getActivity(), getString(R.string.label_grant_location_permission), Toast.LENGTH_SHORT).show();

            }

        }).show();
    }

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("viewMore", viewMore);
        outState.putBoolean("restore", true);
        outState.putBoolean("spotlight", spotlight);
        outState.putInt("itemId", itemId);
        outState.putInt("gender", gender);
        outState.putInt("distance", distance);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    public void getNearbySettings() {

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(getText(R.string.label_nearby_settings_dialog_title));

        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_nearby_settings, null);

        b.setView(view);

        final RadioButton mAnyGenderRadio = view.findViewById(R.id.radio_gender_any);
        final RadioButton mMaleGenderRadio = view.findViewById(R.id.radio_gender_male);
        final RadioButton mFemaleGenderRadio = view.findViewById(R.id.radio_gender_female);

        final TextView mDistanceLabel = view.findViewById(R.id.distance_label);

        final AppCompatSeekBar mDistanceSeekBar = view.findViewById(R.id.choice_distance);

        switch (gender) {

            case 0: {

                mMaleGenderRadio.setChecked(true);

                break;
            }

            case 1: {

                mFemaleGenderRadio.setChecked(true);

                break;
            }

            default: {

                mAnyGenderRadio.setChecked(true);

                break;
            }
        }


        mDistanceSeekBar.setProgress(distance);
        mDistanceLabel.setText(String.format(Locale.getDefault(), getString(R.string.label_distance), distance));

        mDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mDistanceLabel.setText(String.format(Locale.getDefault(), getString(R.string.label_distance), progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        b.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                // get distance
                distance = mDistanceSeekBar.getProgress();
                // Gender

                if (mAnyGenderRadio.isChecked()) {
                    gender = 3;
                }

                if (mMaleGenderRadio.isChecked()) {
                    gender = 0;
                }

                if (mFemaleGenderRadio.isChecked()) {
                    gender = 1;
                }

                // Save filters settings

                saveData();
                // Reload items list
                itemId = 0;
                getItems();
            }
        });

        b.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        AlertDialog d = b.create();

        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);
        d.show();
    }

    public void getItems() {

        if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

            mItemsContainer.setRefreshing(true);

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_PEOPLE_NEARBY_GET, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!isAdded() || getActivity() == null) {

                                Log.e("ERROR", "PeopleNearbyFragment Not Added to Activity");

                                return;
                            }

                            if (!loadingMore) {

                                itemsList.clear();
                            }

                            try {

                                arrayLength = 0;

                                if (!response.getBoolean("error")) {

                                    itemId = response.getInt("itemId");

                                    if (response.has("items")) {

                                        JSONArray usersArray = response.getJSONArray("items");

                                        arrayLength = usersArray.length();

                                        if (arrayLength > 0) {

                                            for (int i = 0; i < usersArray.length(); i++) {

                                                JSONObject userObj = (JSONObject) usersArray.get(i);

                                                Profile profile = new Profile(userObj);

                                                itemsList.add(profile);
                                            }
                                        }
                                    }

                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                loadingComplete();

                                Log.d("Success", response.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (!isAdded() || getActivity() == null) {

                        Log.e("ERROR", "PeopleNearbyFragment Not Added to Activity");

                        return;
                    }

                    loadingComplete();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("lat", Double.toString(App.getInstance().getLat()));
                    params.put("lng", Double.toString(App.getInstance().getLng()));
                    params.put("itemId", Long.toString(itemId));
                    params.put("distance", String.valueOf(distance));
                    params.put("sex", String.valueOf(gender));

                    return params;
                }
            };

            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(15), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void loadingComplete() {

        viewMore = arrayLength == LIST_ITEMS;

        itemsAdapter.notifyDataSetChanged();

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        loadingMore = false;
        mItemsContainer.setRefreshing(false);
    }

    public void showMessage(String message) {

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);

        mSplash.setVisibility(View.VISIBLE);
    }

    public void hideMessage() {

        mMessage.setVisibility(View.GONE);

        mSplash.setVisibility(View.GONE);
    }

    private void readData() {

        gender = App.getInstance().getSharedPref().getInt(getString(R.string.settings_nearby_gender), 3); // 3 = all
        distance = App.getInstance().getSharedPref().getInt(getString(R.string.settings_nearby_distance), 1000);
    }

    public void saveData() {

        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_nearby_gender), gender).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_nearby_distance), distance).apply();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_nearby, menu);

        MainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_nearby_settings: {

                getNearbySettings();

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
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