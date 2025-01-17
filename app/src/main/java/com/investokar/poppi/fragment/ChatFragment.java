package com.investokar.poppi.fragment;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.investokar.poppi.R;
import com.investokar.poppi.activity.UpgradeActivity;
import com.investokar.poppi.adapter.ChatListAdapter;
import com.investokar.poppi.app.App;
import com.investokar.poppi.common.ActivityBase;
import com.investokar.poppi.constants.Constants;
import com.investokar.poppi.model.ChatItem;
import com.investokar.poppi.util.CustomRequest;
import com.investokar.poppi.util.Helper;
import com.investokar.poppi.util.ToastWindow;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class ChatFragment extends Fragment implements Constants {

    private static final String STATE_LIST = "State Adapter Data";

    public final static int STATUS_START = 100;

    public final static String PARAM_TASK = "task";
    public final static String PARAM_STATUS = "status";

    public final static String BROADCAST_ACTION = "com.investokar.poppi.chat";
    public final static String BROADCAST_ACTION_SEEN = "com.investokar.poppi.seen";
    public final static String BROADCAST_ACTION_TYPING_START = "com.investokar.poppi.typing_start";
    public final static String BROADCAST_ACTION_TYPING_END = "com.investokar.poppi.typing_end";

    final String LOG_TAG = "myLogs";

    public static final int RESULT_OK = -1;

    private ProgressDialog pDialog;

    Menu MainMenu;

    View mListViewHeader;

    RelativeLayout mLoadingScreen, mErrorScreen;
    LinearLayout mContentScreen, mTypingContainer, mContainerImg, mChatListViewHeaderContainer;

    ImageView mSendMessage, mActionContainerImg, mDeleteImg, mPreviewImg;
    EditText mMessageText;

    ListView listView;

    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View mBottomSheet;

    BroadcastReceiver br, br_seen, br_typing_start, br_typing_end;

    private ArrayList<ChatItem> chatList;

    private ChatListAdapter chatAdapter;

    String withProfile = "", messageText = "", messageImg = "";
    int chatId = 0, lastMessageId = 0, messagesCount = 0, position = 0;
    long profileId = 0;

    String lMessage = "";

    Boolean blocked = false;

    Boolean img_container_visible = false;

    long fromUserId = 0, toUserId = 0;

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "";

    int arrayLength = 0;
    Boolean loadingMore = false;
    Boolean viewMore = false;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;
    private Boolean visible = true;

    ToastWindow toastWindow = new ToastWindow();

    private Boolean inboxTyping = false, outboxTyping = false;

    private String with_user_username = "", with_user_fullname = "", with_user_photo_url = "";
    private int level = 0, with_user_verified = 0;

    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initpDialog();
        Intent i = requireActivity().getIntent();
        position = i.getIntExtra("position", 0);
        chatId = i.getIntExtra("chatId", 0);
        profileId = i.getLongExtra("profileId", 0);
        withProfile = i.getStringExtra("withProfile");

        with_user_username = i.getStringExtra("with_user_username");
        with_user_fullname = i.getStringExtra("with_user_fullname");
        with_user_photo_url = i.getStringExtra("with_user_photo_url");

        level = i.getIntExtra("level", 0);
        with_user_verified = i.getIntExtra("with_user_verified", 0);

        blocked = i.getBooleanExtra("blocked", false);

        fromUserId = i.getLongExtra("fromUserId", 0);
        toUserId = i.getLongExtra("toUserId", 0);

        chatList = new ArrayList<ChatItem>();
        chatAdapter = new ChatListAdapter(requireActivity(), chatList);
    }

    View rootView;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        
        //
        imgFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    if (result.getData() != null) {
                        selectedImage = result.getData().getData();
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = requireContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        selectedImagePath = cursor.getString(columnIndex);
                        cursor.close();

                        mPreviewImg.setImageURI(null);
                        mPreviewImg.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));
                        showImageContainer();
                    }
                }
            }
        });

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = false;
            String storage_permission = Manifest.permission.READ_EXTERNAL_STORAGE;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

                storage_permission = Manifest.permission.READ_MEDIA_IMAGES;
            }

            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {

                if (x.getKey().equals(storage_permission)) {

                    if (x.getValue()) {

                        granted = true;
                    }
                }
            }

            if (granted) {
                Log.e("Permissions", "granted");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imgFromGalleryActivityResultLauncher.launch(intent);
            } else {

                Log.e("Permissions", "denied");


                Snackbar snackbar = Snackbar.make(requireView(), getString(R.string.label_no_storage_permission), Snackbar.LENGTH_LONG);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    snackbar.setText(getString(R.string.label_grant_media_permission));
                }
                snackbar.setAction(getString(R.string.action_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);
                    }
                }).show();

            }

        });

        if (savedInstanceState != null) {
            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");
            img_container_visible = savedInstanceState.getBoolean("img_container_visible");
        } else {
            App.getInstance().setCurrentChatId(chatId);
            restore = false;
            loading = false;
            preload = false;
            img_container_visible = false;
        }

        br_typing_start = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                typing_start();
            }
        };

        IntentFilter intFilt4 = new IntentFilter(BROADCAST_ACTION_TYPING_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().registerReceiver(br_typing_start, intFilt4, Context.RECEIVER_EXPORTED);
            } else {
                requireActivity().registerReceiver(br_typing_start, intFilt4);
            }
        }

        br_typing_end = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);
                typing_end();
            }
        };

        IntentFilter intFilt3 = new IntentFilter(BROADCAST_ACTION_TYPING_END);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().registerReceiver(br_typing_end, intFilt3, Context.RECEIVER_EXPORTED);
            } else {
                requireActivity().registerReceiver(br_typing_end, intFilt3);
            }
        }

        br_seen = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);
                seen();
            }
        };

        IntentFilter intFilt2 = new IntentFilter(BROADCAST_ACTION_SEEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().registerReceiver(br_seen, intFilt2, Context.RECEIVER_EXPORTED);
            } else {
                requireActivity().registerReceiver(br_seen, intFilt2);
            }
        }

        br = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);
                int lastMessageId = intent.getIntExtra("lastMessageId", 0);
                long fromUserId = intent.getLongExtra("fromUserId", 0);
                int fromUserState = intent.getIntExtra("fromUserState", 0);
                String fromUserUsername = intent.getStringExtra("fromUserUsername");
                String fromUserFullname = intent.getStringExtra("fromUserFullname");
                String fromUserPhotoUrl = intent.getStringExtra("fromUserPhotoUrl");
                String msgMessage = intent.getStringExtra("msgMessage");
                String msgImageUrl = intent.getStringExtra("msgImageUrl");
                int msgCreateAt = intent.getIntExtra("msgCreateAt", 0);
                String msgDate = intent.getStringExtra("msgDate");
                String msgTimeAgo = intent.getStringExtra("msgTimeAgo");

                ChatItem c = new ChatItem();
                c.setId(lastMessageId);
                c.setFromUserId(fromUserId);

                if (fromUserId == App.getInstance().getId()) {
                    c.setFromUserState(App.getInstance().getState());
                    c.setFromUserUsername(App.getInstance().getUsername());
                    c.setFromUserFullname(App.getInstance().getFullname());
                    c.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());
                } else {
                    c.setFromUserState(level);
                    c.setFromUserUsername(with_user_username);
                    c.setFromUserFullname(with_user_fullname);
                    c.setFromUserPhotoUrl(with_user_photo_url);
                }

                c.setMessage(msgMessage);
                c.setImageUrl(msgImageUrl);
                c.setCreateAt(msgCreateAt);
                c.setDate(msgDate);
                c.setTimeAgo(msgTimeAgo);

                Log.e(LOG_TAG, "onReceive: task = " + task + ", status = " + status + " " + c.getMessage() + " " + c.getId());

                final ChatItem lastItem = (ChatItem) listView.getAdapter().getItem(listView.getAdapter().getCount() - 1);
                messagesCount = messagesCount + 1;
                chatList.add(c);
                if (!visible) {

                    try {

                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(requireActivity(), notification);
                        r.play();

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                chatAdapter.notifyDataSetChanged();

                scrollListViewToBottom();

                if (inboxTyping) typing_end();

                seen();

                sendNotify(GCM_NOTIFY_SEEN);
            }
        };

        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().registerReceiver(br, intFilt, Context.RECEIVER_EXPORTED);
            } else {
                requireActivity().registerReceiver(br, intFilt);
            }
        }

        if (loading) {

            showpDialog();
        }

        mLoadingScreen = rootView.findViewById(R.id.loadingScreen);
        mErrorScreen = rootView.findViewById(R.id.errorScreen);

        mContentScreen = rootView.findViewById(R.id.contentScreen);

        mSendMessage = rootView.findViewById(R.id.sendMessage);
        mMessageText = rootView.findViewById(R.id.messageText);

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMessage();
            }
        });

        listView = rootView.findViewById(R.id.listView);

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        mListViewHeader = requireActivity().getLayoutInflater().inflate(R.layout.chat_listview_header, null);
        mChatListViewHeaderContainer = mListViewHeader.findViewById(R.id.chatListViewHeaderContainer);

        listView.addHeaderView(mListViewHeader);

        mListViewHeader.setVisibility(View.GONE);

        listView.setAdapter(chatAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0 && mListViewHeader.getVisibility() == View.VISIBLE) {
                getPreviousMessages();
            }
        });

        mActionContainerImg = rootView.findViewById(R.id.actionContainerImg);

        mTypingContainer = rootView.findViewById(R.id.container_typing);

        mTypingContainer.setVisibility(View.GONE);

        mDeleteImg = rootView.findViewById(R.id.deleteImg);
        mPreviewImg = rootView.findViewById(R.id.previewImg);

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        mContainerImg = rootView.findViewById(R.id.container_img);
        mContainerImg.setVisibility(View.GONE);

        mDeleteImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                selectedImage = null;
                selectedImagePath = "";
                hideImageContainer();
            }
        });

        mActionContainerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreDialog();
            }
        });

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            mPreviewImg.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));
            showImageContainer();
        }

        mMessageText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String txt = mMessageText.getText().toString();
                if (txt.isEmpty() && outboxTyping) {
                    outboxTyping = false;
                    sendNotify(GCM_NOTIFY_TYPING_END);
                } else {
                    if (!outboxTyping && !txt.isEmpty()) {
                        outboxTyping = true;
                        sendNotify(GCM_NOTIFY_TYPING_START);
                    }
                }

                Log.e("", "afterTextChanged");
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.e("", "beforeTextChanged");
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.e("", "onTextChanged");
            }
        });

        if (inboxTyping) {

            mTypingContainer.setVisibility(View.VISIBLE);

        } else {

            mTypingContainer.setVisibility(View.GONE);
        }

        if (!restore) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getChat();

            } else {

                showErrorScreen();
            }

        } else {
            if (App.getInstance().isConnected()) {
                if (!preload) {
                    showContentScreen();
                } else {
                    showLoadingScreen();
                }
            } else {
                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void initiatePopupWindow(String popup) {
        Intent intent = new Intent(requireActivity(), UpgradeActivity.class);
        intent.putExtra("popup_string", popup);
        startActivity(intent);
    }

    public void typing_start() {

        inboxTyping = true;

        mTypingContainer.setVisibility(View.VISIBLE);
    }

    public void typing_end() {

        mTypingContainer.setVisibility(View.GONE);

        inboxTyping = false;
    }

    public void seen() {

        if (chatAdapter.getCount() > 0) {

            for (int i = 0; i < chatAdapter.getCount(); i++) {

                ChatItem item = chatList.get(i);

                if (item.getFromUserId() == App.getInstance().getId()) {

                    chatList.get(i).setSeenAt(1);
                }
            }
        }

        chatAdapter.notifyDataSetChanged();
    }

    public void sendNotify(final int notifyId) {

        if (App.getInstance().getSeenTyping() != 1) {

            return;
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_NOTIFY, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || requireActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("send fcm", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded()) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                } else {
                    requireActivity();
                }

                Log.e("send fcm error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("chatId", String.valueOf(chatId));
                params.put("notifyId", String.valueOf(notifyId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }


    public void onDestroyView() {

        super.onDestroyView();

        requireActivity().unregisterReceiver(br);

        requireActivity().unregisterReceiver(br_seen);

        requireActivity().unregisterReceiver(br_typing_start);

        requireActivity().unregisterReceiver(br_typing_end);

        hidepDialog();
    }

    @Override
    public void onResume() {

        super.onResume();

        visible = true;
    }

    @Override
    public void onPause() {

        super.onPause();

        visible = false;
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

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putBoolean("img_container_visible", img_container_visible);
    }

    private void scrollListViewToBottom() {

        listView.smoothScrollToPosition(chatAdapter.getCount());

        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(chatAdapter.getCount() - 1);
            }
        });
    }

    public void updateChat() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_UPDATE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || requireActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("TAG", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded()) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                } else {
                    requireActivity();
                }

                preload = false;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("chatId", String.valueOf(chatId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getChat() {

        preload = true;

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_GET, null,
                response -> {

                    if (!isAdded()) {

                        Log.e("ERROR", "ChatFragment Not Added to Activity");

                        return;
                    } else {
                        requireActivity();
                    }

                    try {

                        if (!response.getBoolean("error")) {
                            lastMessageId = response.getInt("lastMessageId");
                            chatId = response.getInt("chatId");
                            messagesCount = response.getInt("messagesCount");
                            App.getInstance().setCurrentChatId(chatId);
                            fromUserId = response.getLong("chatFromUserId");
                            toUserId = response.getLong("chatToUserId");

                            if (messagesCount > 20) {
                                mListViewHeader.setVisibility(View.VISIBLE);
                            }

                            if (response.has("messages")) {
                                JSONArray messagesArray = response.getJSONArray("messages");
                                arrayLength = messagesArray.length();
                                if (arrayLength > 0) {
                                    for (int i = messagesArray.length() - 1; i > -1; i--) {
                                        JSONObject msgObj = (JSONObject) messagesArray.get(i);
                                        ChatItem item = new ChatItem(msgObj);
                                        chatList.add(item);
                                        chatAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        showContentScreen();
                        scrollListViewToBottom();
                        updateChat();
                    }
                }, error -> {
                    if (!isAdded() || requireActivity() == null) {
                        Log.e("ERROR", "ChatFragment Not Added to Activity");
                        return;
                    }
                    preload = false;
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));
                params.put("lastMessageId", String.valueOf(lastMessageId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonReq.setRetryPolicy(policy);
        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getPreviousMessages() {
        loading = true;
        showpDialog();
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_GET_PREVIOUS, null,
                response -> {
                    if (!isAdded() || requireActivity() == null) {
                        Log.e("ERROR", "ChatFragment Not Added to Activity");
                        return;
                    }

                    try {

                        if (!response.getBoolean("error")) {

                            lastMessageId = response.getInt("lastMessageId");
                            chatId = response.getInt("chatId");

                            if (response.has("messages")) {

                                JSONArray messagesArray = response.getJSONArray("messages");

                                arrayLength = messagesArray.length();

                                if (arrayLength > 0) {

                                    for (int i = 0; i < messagesArray.length(); i++) {

                                        JSONObject msgObj = (JSONObject) messagesArray.get(i);

                                        ChatItem item = new ChatItem(msgObj);

                                        chatList.add(0, item);
                                        chatAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }

                    } catch (JSONException e) {

                        e.printStackTrace();

                    } finally {

                        loading = false;

                        hidepDialog();


                        if (messagesCount <= listView.getAdapter().getCount() - 1) {

                            mListViewHeader.setVisibility(View.GONE);

                        } else {

                            mListViewHeader.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || requireActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));
                params.put("chatId", String.valueOf(chatId));
                params.put("lastMessageId", String.valueOf(lastMessageId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void newMessage() {
        if (App.getInstance().isConnected()) {
            messageText = mMessageText.getText().toString();
            messageText = messageText.trim();
            if (selectedImagePath.length() != 0) {
                loading = true;
                showpDialog();
                File f = new File(selectedImagePath);
                uploadFile(METHOD_MSG_UPLOAD_IMG, f);
            } else {
                if (messageText.length() > 0) {
                    loading = true;
                    send();
                } else {
                    toastWindow.makeText(getText(R.string.msg_enter_msg), 2000);
                }
            }
        } else {
            toastWindow.makeText(getText(R.string.msg_network_error), 2000);
        }
    }

    public void send() {
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_MSG_NEW, null,
                response -> {
                    try {
                        if (!response.getBoolean("error")) {
                            chatId = response.getInt("chatId");
                            App.getInstance().setCurrentChatId(chatId);
                            if (response.has("message")) {
                                JSONObject msgObj = response.getJSONObject("message");
                                ChatItem item = new ChatItem(msgObj);
                                item.setListId(response.getInt("listId"));
                                chatList.add(item);
                                chatAdapter.notifyDataSetChanged();
                                scrollListViewToBottom();
                            }
                        } else {
                            if (response.getInt("error_code") == 402) {
                                initiatePopupWindow("Subscribe to send more messages.");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        loading = false;
                        hidepDialog();
                        messageText = "";
                        messageImg = "";
                        Log.e("Chat", response.toString());
                    }
                }, error -> {
                    messageText = "";
                    messageImg = "";
                    loading = false;
                    hidepDialog();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));
                params.put("chatId", String.valueOf(chatId));
                params.put("messageText", lMessage);
                params.put("listId", String.valueOf(listView.getAdapter().getCount()));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                return params;
            }
        };


        lMessage = messageText;

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);
        App.getInstance().addToRequestQueue(jsonReq);
        outboxTyping = false;

        mContainerImg.setVisibility(View.GONE);
        selectedImagePath = "";
        selectedImage = null;
        messageImg = "";
        mMessageText.setText("");
        messagesCount++;

        hideImageContainer();
    }


    public Boolean uploadFile(String serverURL, File file) {
        lMessage = messageText;

        outboxTyping = false;

        mContainerImg.setVisibility(View.GONE);
        selectedImagePath = "";
        selectedImage = null;
        messageImg = "";
        mMessageText.setText("");
        messagesCount++;

        hideImageContainer();

        final OkHttpClient client = new OkHttpClient();
        client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
        try {
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .addFormDataPart("profileId", Long.toString(profileId))
                    .addFormDataPart("chatId", String.valueOf(chatId))
                    .addFormDataPart("messageText", lMessage)
                    .addFormDataPart("listId", String.valueOf(listView.getAdapter().getCount()))
                    .addFormDataPart("chatFromUserId", Long.toString(fromUserId))
                    .addFormDataPart("chatToUserId", Long.toString(toUserId))
                    .build();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                    messageText = "";
                    messageImg = "";
                    loading = false;
                    hidepDialog();
                    Log.e("failure", request.toString());
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                    String jsonData = response.body().string();
                    Log.e("response", jsonData);
                    try {
                        JSONObject result = new JSONObject(jsonData);
                        if (!result.getBoolean("error")) {
                            chatId = result.getInt("chatId");
                            App.getInstance().setCurrentChatId(chatId);
                            if (result.has("message")) {
                                JSONObject msgObj = result.getJSONObject("message");
                                ChatItem item = new ChatItem(msgObj);
                                item.setListId(result.getInt("listId"));
                                chatList.add(item);
                                chatAdapter.notifyDataSetChanged();
                                scrollListViewToBottom();
                            }
                        } else {
                            if (result.getInt("error_code") == 402) {
                                initiatePopupWindow("Subscribe to send more messages.");
                            }
                        }
                        Log.d("My App", response.toString());
                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");
                    } finally {
                        loading = false;
                        hidepDialog();
                        messageText = "";
                        messageImg = "";
                    }
                }
            });

            return true;
        } catch (Exception ex) {
            // Handle the error
            loading = false;
            hidepDialog();
        }

        return false;
    }

    public void deleteChat() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_REMOVE, null,
                new Response.Listener<>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded()) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        } else {
                            requireActivity();
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                Intent i = new Intent();
                                i.putExtra("action", "Delete");
                                i.putExtra("position", position);
                                i.putExtra("chatId", chatId);
                                requireActivity().setResult(RESULT_OK, i);

                                requireActivity().finish();

//                                toastWindow.makeText(getString(R.string.msg_send_msg_error), 2000);
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || requireActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));
                params.put("chatId", String.valueOf(chatId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void showLoadingScreen() {

        mContentScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    public void showErrorScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);

        mErrorScreen.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mContentScreen.setVisibility(View.VISIBLE);

        preload = false;

        requireActivity().invalidateOptionsMenu();
    }

    private void showMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (App.getInstance().isConnected()) {

            if (!preload) {

                requireActivity().setTitle(withProfile);

                showMenuItems(menu, true);

            } else {

                showMenuItems(menu, false);
            }

        } else {

            showMenuItems(menu, false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_chat, menu);
        MainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_chat_delete) {
            deleteChat();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        updateChat();
        if (outboxTyping) {
            sendNotify(GCM_NOTIFY_TYPING_END);
        }
    }


    private void showMoreDialog() {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.chat_sheet_list, null);

        LinearLayout mGalleryButton = view.findViewById(R.id.gallery_button);

        
        mGalleryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                if (!checkPermission(READ_EXTERNAL_STORAGE)) {
                    ActivityBase activity = (ActivityBase) getActivity();
                    activity.requestStoragePermission(storagePermissionLauncher);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    imgFromGalleryActivityResultLauncher.launch(intent);
                }
            }
        });


        mBottomSheetDialog = new BottomSheetDialog(requireActivity());
        mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mBottomSheetDialog.show();

        doKeepDialog(mBottomSheetDialog);

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mBottomSheetDialog = null;
            }
        });
    }

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog){

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    public void showImageContainer() {

        img_container_visible = true;

        mContainerImg.setVisibility(View.VISIBLE);

        mActionContainerImg.setVisibility(View.GONE);
    }

    public void hideImageContainer() {

        img_container_visible = false;

        mContainerImg.setVisibility(View.GONE);

        mActionContainerImg.setVisibility(View.VISIBLE);

        mActionContainerImg.setBackgroundResource(R.drawable.ic_action_new);
    }

    private boolean checkPermission(String permission) {

        if (ContextCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_GRANTED) {

            return true;
        }

        return false;
    }

}