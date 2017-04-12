package chat.manan.chat.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import chat.manan.chat.R;
import chat.manan.chat.gcm.GCMPushReceiverService;
import chat.manan.chat.gcm.GCMRegistrationIntentService;
import chat.manan.chat.gcm.MyFirebaseInstanceIDService;
import chat.manan.chat.helper.AppController;
import chat.manan.chat.helper.Constants;
import chat.manan.chat.helper.Group_info_sqlite;
import chat.manan.chat.helper.Groups_sqlite;
import chat.manan.chat.helper.Message;
import chat.manan.chat.helper.Messages_sqlite;
import chat.manan.chat.helper.ThreadAdapter;
import chat.manan.chat.helper.URLs;
import chat.manan.chat.helper.Users_sqlite;

public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener {

    //Broadcast receiver to receive broadcasts
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static boolean active = false;

    //Progress dialog
    private ProgressDialog dialog;
    public ProgressDialog progressDialog;
    public int update_id;
    public File f;
    public String filepath;
    public String file_size;
    public String last_message;
    String video_base_64;
    public String type;
    public File localFile;
    public Messages_sqlite msql;
    public SQLiteDatabase msqld;
    //Recyclerview objects
    public static RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    public  static RecyclerView.Adapter adapter;
    public static Activity activity;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //ArrayList of messages to store the thread messages
    public static ArrayList<Message> messages;

    //Button to send new message on the thread
    private Button buttonSend;
    public String videol;

    //EditText to send new message on the thread
    private EditText editTextMessage;
    public TextView chat_name;
    public ImageView attach;
    public int width,attach_fragment_isopen,back_pressed_count,logout_pressed_count;
    public Bitmap y;
    public String z;
    public String nam;
    public ImageView img;
    public String q;
    public String group_pic_path;
    public int index;
    public ArrayList<Integer> user_ids;
    public Toolbar toolbar;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        last_message="Message";
        verifyStoragePermissions(ChatRoomActivity.this);
        Intent in=getIntent();
        msql=new Messages_sqlite(getApplicationContext());
        msqld=msql.getWritableDatabase();
        user_ids=(ArrayList<Integer>) in.getSerializableExtra("user_ids");
        SharedPreferences share=getSharedPreferences("group_details",Context.MODE_PRIVATE);
        q=share.getString("group_pic","Image");
        z=share.getString("group_id","0");
        file_size="null";
        activity=this;
        nam=share.getString("group_name","Group");
        index=share.getInt("index",0);
        group_pic_path=share.getString("group_pic_server_path","null");
        if(q.equals("Image")){
            Drawable d = ContextCompat.getDrawable(this, R.drawable.gallery);
            y = ((BitmapDrawable) d).getBitmap();
        }
        else{
            byte[] decodedString = Base64.decode(q, Base64.DEFAULT);
            y= BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }

        Groups_sqlite gsq=new Groups_sqlite(getApplicationContext());
        SQLiteDatabase gsqd=gsq.getWritableDatabase();
        gsqd.delete(gsq.TB_name,"Id=?",new String[]{String.valueOf(z)});
        gsqd.close();
        Chats.chat_groups_info.get(index).count = 0;

        //Adding toolbar to activity
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        chat_name=(TextView)findViewById(R.id.chat_name);
        chat_name.setText(nam);
        chat_name.setOnClickListener(this);
        img=(ImageView)toolbar.findViewById(R.id.chat_icon);
        img.setOnClickListener(this);
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        width=width/5;
        img.setMaxWidth(width);
        img.setImageBitmap(y);
        width=display.getHeight();
        width = width / 6;

        ThreadAdapter.width = width;

        attach_fragment_isopen = 0;


        //Initializing recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Initializing message arraylist
        messages = new ArrayList<Message>();

        //Calling function to fetch the existing messages on the thread
        fetchMessages();

        //initializing button and edittext
        buttonSend = (Button) findViewById(R.id.buttonSend);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        attach = (ImageView) findViewById(R.id.image_attach);

        //Adding listener to butto
        buttonSend.setOnClickListener(this);
        attach.setOnClickListener(this);

        //Creating broadcast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {

                    //When gcm registration is success do something here if you need

                } else if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_TOKEN_SENT)) {

                    //When the registration token is sent to ther server displaying a toast
                    Toast.makeText(getApplicationContext(), "Chatroom Ready...", Toast.LENGTH_SHORT).show();

                    //When we received a notification when the app is in foreground
                } else if (intent.getAction().equals(Constants.PUSH_NOTIFICATION)) {
                    //Getting message data
                    String name = intent.getStringExtra("name");
                    String message = intent.getStringExtra("message");
                    String id = intent.getStringExtra("id");

                    //processing the message to add it in current thread
                    processMessage(name, message, id);
                }
            }
        };

        //if the google play service is not in the device app won't work
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (ConnectionResult.SUCCESS != resultCode) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());

            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }
        } else {
            Intent itent = new Intent(this, GCMRegistrationIntentService.class);
            startService(itent);
          }
       isMyServiceRunning(MyFirebaseInstanceIDService.class);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("service","running");
                return true;
            }
        }
        Log.i("service","not running");
        return false;
    }

   @Override
    public void onBackPressed() {
       if (messages.size() == 0) {
           Chats.chat_groups_info.get(index).last_message = "Message";
           Group_info_sqlite gre=new Group_info_sqlite(getApplicationContext());
           SQLiteDatabase gred=gre.getWritableDatabase();
           ContentValues ce=new ContentValues();
           ce.put("last_message","Message");
           gred.update(gre.TB_name,ce,"group_id=?",new String[]{String.valueOf(z)});
           gred.close();
       }
       else{
           Chats.chat_groups_info.get(index).last_message=messages.get(messages.size()-1).getMessage();
           Group_info_sqlite gre=new Group_info_sqlite(getApplicationContext());
           SQLiteDatabase gred=gre.getWritableDatabase();
           ContentValues ce=new ContentValues();
           ce.put("last_message",messages.get(messages.size()-1).getMessage());
           gred.update(gre.TB_name,ce,"group_id=?",new String[]{String.valueOf(z)});
           gred.close();
       }
       Chats.adapter.notifyDataSetChanged();
       super.onBackPressed();
    }

    public void copyFile(String local_Path, String server_Path) {

        InputStream in = null;
        OutputStream out = null;
        try {

            in = new FileInputStream(local_Path);
            out = new FileOutputStream(server_Path);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public void update_last_msg(final int i){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_LAST_MESSAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        Log.i("respo", response);
                        Intent intent=null;
                        if(i==1) {
                             intent = new Intent(ChatRoomActivity.this, Chats.class);
                        }else{
                            intent=new Intent(ChatRoomActivity.this, MainActivity.class);
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id",z);
                params.put("last_msg", last_message);
                return params;
            }
        };

        //Disabling retry to prevent duplicate messages
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        Log.i("end0", stringRequest.toString());
        stringRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(stringRequest);
}




    //This method will fetch all the messages of the thread
    private void fetchMessages() {

        Cursor c=msqld.query(msql.TB_name,null,"group_id=?",new String[]{String.valueOf(z)},null,null,"message_id");
        if(c.getCount()!=0){
            c.moveToFirst();
            while(!c.isAfterLast()){
                int id = c.getInt(c.getColumnIndex("message_id"));
                int userId = c.getInt(c.getColumnIndex("user_id"));
                String message = c.getString(c.getColumnIndex("message"));
                String name = c.getString(c.getColumnIndex("name"));
                String sentAt = c.getString(c.getColumnIndex("sentat"));
                String img_base_64 = c.getString(c.getColumnIndex("img_base_64"));
                String video_uri = c.getString(c.getColumnIndex("video_server_uri"));
                String file_size = c.getString(c.getColumnIndex("file_size"));
                String audio_uri = c.getString(c.getColumnIndex("audio_server_uri"));
                String img_uri = c.getString(c.getColumnIndex("img_server_uri"));
                String img_local_uri = c.getString(c.getColumnIndex("img_local_uri"));
                String audio_local_uri = c.getString(c.getColumnIndex("audio_local_uri"));
                String video_local_uri = c.getString(c.getColumnIndex("video_local_uri"));

                Message messagObject = new Message(userId, message, sentAt, name, img_base_64);
                messagObject.setId(id);
                messagObject.setVideo_server_uri(video_uri);
                messagObject.setAudio_server_uri(audio_uri);
                messagObject.setfile_size(file_size);
                messagObject.setImage_server_uri(img_uri);
                messagObject.setImage_local_uri(img_local_uri);
                messagObject.setAudio_local_uri(audio_local_uri);
                messagObject.setVideo_local_uri(video_local_uri);

                messages.add(messagObject);
                c.moveToNext();
            }
            msqld.close();
            adapter = new ThreadAdapter(ChatRoomActivity.this, messages, AppController.getInstance().getUserId());
            recyclerView.setAdapter(adapter);
            scrollToBottom();
        }
        else {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Opening chat room");
            dialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_FETCH_MESSAGES,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            dialog.dismiss();


                            try {
                                JSONObject res = new JSONObject(response);
                                JSONArray thread = res.getJSONArray("messages");
                                for (int i = 0; i < thread.length(); i++) {
                                    JSONObject obj = thread.getJSONObject(i);
                                    int id = obj.getInt("id");
                                    int userId = obj.getInt("userid");
                                    String message = obj.getString("message");
                                    String name = obj.getString("name");
                                    String sentAt = obj.getString("sentat");
                                    String img_base_64 = obj.getString("img_base_64");
                                    Log.i("chker", (img_base_64.equals("null")) + " " + i);
                                    String video_uri = obj.getString("video_uri");
                                    String file_size = obj.getString("file_size");
                                    String audio_uri = obj.getString("audio_uri");
                                    String img_uri = obj.getString("img_server_uri");


                                    Message messagObject = new Message(userId, message, sentAt, name, img_base_64);
                                    messagObject.setId(id);
                                    messagObject.setVideo_server_uri(video_uri);
                                    messagObject.setAudio_server_uri(audio_uri);
                                    messagObject.setfile_size(file_size);
                                    messagObject.setImage_server_uri(img_uri);

                                    messages.add(messagObject);

                                    ContentValues c=new ContentValues();
                                    c.put("group_id",String.valueOf(z));
                                    c.put("user_id",userId);
                                    c.put("message",message);
                                    c.put("sentat",sentAt);
                                    c.put("img_base_64",img_base_64);
                                    c.put("file_size",file_size);
                                    c.put("img_server_uri",img_uri);
                                    c.put("video_server_uri",video_uri);
                                    c.put("audio_server_uri",audio_uri);
                                    c.put("name",name);
                                    msqld.insert(msql.TB_name,null,c);
                                }
                                msqld.close();
                                adapter = new ThreadAdapter(ChatRoomActivity.this, messages, AppController.getInstance().getUserId());
                                recyclerView.setAdapter(adapter);
                                scrollToBottom();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", z);
                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(stringRequest);
        }
    }

    //Processing message to add on the thread
    private void processMessage(String name, String message, String id) {
        Message m = new Message(Integer.parseInt(id), message, getTimeStamp(), name, null);
        messages.add(m);
        scrollToBottom();
    }

    //This method will send the new message to the thread
    private void sendMessage() {
        final String message = editTextMessage.getText().toString().trim();
        if (message.equalsIgnoreCase(""))
            return;
        int userId = AppController.getInstance().getUserId();
        final String name = AppController.getInstance().getUserName();
        final String sentAt = getTimeStamp();

        Message m = new Message(userId, message, sentAt, name, "null");
        messages.add(m);
        adapter.notifyDataSetChanged();
        scrollToBottom();

        editTextMessage.setText("");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_SEND_MESSAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("respo", response);
                        msqld=msql.getWritableDatabase();
                        ContentValues c=new ContentValues();
                        c.put("group_id",String.valueOf(z));
                        c.put("user_id",AppController.getInstance().getUserId());
                        c.put("message",message);
                        c.put("sentat",sentAt);
                        c.put("name",name);
                        msqld.insert(msql.TB_name,null,c);
                        msqld.close();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("group_id",z);
                params.put("id", String.valueOf(AppController.getInstance().getUserId()));
                params.put("message", message);
                params.put("name", AppController.getInstance().getUserName());
                params.put("img_base_64", "null");
                params.put("file_size", "null");
                params.put("audio_uri", "null");
                params.put("video_uri","null");
                params.put("img_server_uri", "null");
                return params;
            }
        };

        //Disabling retry to prevent duplicate messages
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        Log.i("end0", stringRequest.toString());
        stringRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(stringRequest);
    }


    public void sendVideo(final String video_base_64, final String video_uri) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_SEND_MESSAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("chch3", "chch3");
                        if (response.toLowerCase().indexOf("false".toLowerCase()) != -1) {
                            Log.i("responsee", response.toString());

                            int userId = AppController.getInstance().getUserId();
                            String name = AppController.getInstance().getUserName();
                            String sentAt = getTimeStamp();


                            Message m = new Message(userId, "", sentAt, name, "null");
                            Log.i("chch2", "chch2");

                            m.setVideo_local_uri(video_uri);

                            Log.i("actual_video", video_base_64);
                            messages.add(m);
                            adapter.notifyDataSetChanged();

                            scrollToBottom();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(AppController.getInstance().getUserId()));

                params.put("message", "Video");
                params.put("name", AppController.getInstance().getUserName());


                params.put("video_base_64", video_base_64);
                params.put("video_uri", video_uri);
                Log.i("chch1", "chch1");

                return params;
            }
        };

        //Disabling retry to prevent duplicate messages
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(stringRequest);

    }

    //method to scroll the recyclerview to bottom
    public static void scrollToBottom() {
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() > 1)
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
    }

    //This method will return current timestamp
    public static String getTimeStamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    //Registering broadcast receivers
    @Override
    protected void onResume() {
        super.onResume();
        Log.w("MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_TOKEN_SENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.PUSH_NOTIFICATION));
    }


    //Unregistering receivers
    @Override
    protected void onPause() {
        super.onPause();
        Log.w("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }


    //Sending message onclick
    @Override
    public void onClick(View v) {
        if (v == buttonSend)
            sendMessage();
        else if (v == attach) {
            if (attach_fragment_isopen == 0) {
                Fragment p = new popup();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                transaction.add(R.id.pop_up, p, "attachment");
                transaction.commit();
                attach_fragment_isopen = 1;
            }
            else {

                Fragment fragment = getSupportFragmentManager().findFragmentByTag("attachment");
                if(fragment != null)
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                getSupportFragmentManager().popBackStack();
                attach_fragment_isopen = 0;

            }

        }

        else if(v==img){
            if(group_pic_path.equals("null")){
                Drawable myDrawable = ContextCompat.getDrawable(ChatRoomActivity.this, R.drawable.gallery);
                Bitmap thumbnail = ((BitmapDrawable) myDrawable).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String im = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                SharedPreferences share=getSharedPreferences("group_details",Context.MODE_PRIVATE);
                SharedPreferences.Editor e=share.edit();
                e.putString("group_pic",im);
                e.commit();
                Intent u=new Intent(ChatRoomActivity.this,chk.class);
                u.putExtra("called_for","group_pic");
                startActivity(u);
            }
            else {
                download(group_pic_path, 0);
            }
        }
        else if(v==chat_name){
            if(group_pic_path.equals("null")){
                Drawable myDrawable = ContextCompat.getDrawable(ChatRoomActivity.this, R.drawable.gallery);
                Bitmap thumbnail = ((BitmapDrawable) myDrawable).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String im = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                Intent u=new Intent(ChatRoomActivity.this,group_inf.class);
                Log.i("user_ids",user_ids+"");
                u.putExtra("user_ids",user_ids);
                Log.i("usre_ids",user_ids+"");
                u.putExtra("group_pic",im);
                Log.i("tlb","tlb");
                startActivity(u);
            }
            else {
                download(group_pic_path, 1);
            }
        }
    }

    public void download(final String path,final int i) {
        String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
        File myDir = new File(root);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        localFile = new File(myDir, path + ".jpg");
        if (localFile.exists()) {
            Bitmap b = BitmapFactory.decodeFile(localFile.getPath());
            Log.i("impath_bimtmap",b+" "+localFile.getPath());
           String im = getStringImage(b);
            if(i==0){
                Intent u=new Intent(ChatRoomActivity.this,chk.class);
                SharedPreferences share=getSharedPreferences("group_details",Context.MODE_PRIVATE);
                SharedPreferences.Editor e=share.edit();
                e.putString("group_pic",im);
                e.commit();
                u.putExtra("called_for","group_pic");
                startActivity(u);
            }
            else if(i==1){
                Intent u=new Intent(ChatRoomActivity.this,group_inf.class);
                Log.i("user_ids",user_ids+"");
                u.putExtra("user_ids",user_ids);
                Log.i("usre_ids",user_ids+"");
                u.putExtra("group_pic",im);
                Log.i("tlb","tlb");
                this.startActivity(u);
            }

        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Downloading");
            progressDialog.show();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://chat-764b5.appspot.com");
            StorageReference islandRef = null;

            islandRef = storageRef.child("Group_pics" + "/images/" + path + ".jpg");
            root = Environment.getExternalStoragePublicDirectory("Chat").toString();
            myDir = new File(root);
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            localFile = new File(myDir, path + ".jpg");
            try {
                localFile.createNewFile();
                Log.i("ff", localFile + "");
            } catch (IOException e) {
                Log.i("ff", e + "");
                e.printStackTrace();
            }

            islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    //and displaying a success toast
                    Toast.makeText(getApplicationContext(), "Download successful.. ", Toast.LENGTH_LONG).show();
                    // Local temp file has been created
                    Bitmap b = BitmapFactory.decodeFile(localFile.getPath());
                   String im = getStringImage(b);
                    progressDialog.dismiss();
                    if(i==0){
                        Intent u=new Intent(ChatRoomActivity.this,chk.class);
                        SharedPreferences share=getSharedPreferences("group_details",Context.MODE_PRIVATE);
                        SharedPreferences.Editor e=share.edit();
                        e.putString("group_pic",im);
                        e.commit();
                        u.putExtra("called_for","group_pic");
                        startActivity(u);
                    }
                    else if(i==1){
                        Intent u=new Intent(ChatRoomActivity.this,group_inf.class);
                        Log.i("user_ids",user_ids+"");
                        u.putExtra("user_ids",user_ids);
                        Log.i("usre_ids",user_ids+"");
                        u.putExtra("group_pic",im);
                        Log.i("tlb","tlb");
                        startActivity(u);
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //if the upload is not successfull
                    //hiding the progress dialog
                    //and displaying error message
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    //calculating progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    //displaying percentage in progress dialog
                    progressDialog.setMessage("Downloaded " + ((int) progress) + "%...");
                }
            });
        }
    }



    //Creating option menu to add logout feature
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Adding our menu to toolbar
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //Adding logout option here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuLogout) {

          /*  logout_pressed_count++;
            Log.i("logout_pressed_count",logout_pressed_count+"");
            if(logout_pressed_count==1||logout_pressed_count==2){
                dialog = new ProgressDialog(this);
                dialog.setMessage("Please wait..");
                dialog.show();
                update_last_msg(0);
            }*/
           // else{
            Messages_sqlite m=new Messages_sqlite(getApplicationContext());
            SQLiteDatabase s=m.getWritableDatabase();
            s.delete(m.TB_name,null,null);
            s.close();
            Groups_sqlite g=new Groups_sqlite(getApplicationContext());
            s=g.getWritableDatabase();
            s.delete(g.TB_name,null,null);
            s.close();
            Users_sqlite gi=new Users_sqlite(getApplicationContext());
            SQLiteDatabase si=gi.getWritableDatabase();
            si.delete(gi.TB_name,null,null);
            si.close();
            Group_info_sqlite gg=new Group_info_sqlite(getApplicationContext());
            s=gg.getWritableDatabase();
            s.delete(gg.TB_name,null,null);
            s.close();
            AppController.getInstance().logout();
            Intent intent= new Intent(ChatRoomActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //}
        }
        if (id == R.id.Profile_pic) {
            Intent i=new Intent(ChatRoomActivity.this,chk.class);
            i.putExtra("called_for","pro_pic");
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // When an Image is picked
        if (resultCode == RESULT_OK) {
            // Get the Image from data
            if(requestCode==0){
            try {
                Log.i("statuss", requestCode + " " + requestCode + " " + data.toString());

                if (data != null) {

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    String uri = null;

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    filepath = cursor.getString(columnIndex);
                    // String selectedImagePath = selectedImageUri.toString();
                    cursor.close();
                    f = new File(filepath);
                    long sizeInBytes = f.length();
//transform in MB
                    Float sizeInMb = (float)(sizeInBytes / (1024.0 * 1024.0));
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    file_size=df.format(sizeInMb)+"MB";

                    uploadFile(0);
                    /* Intent i = new Intent(ChatRoomActivity.this, chk.class);
                    Bundle b=new Bundle();
                    b.putString("imgDecodableString", imgDecodableString);
                    i.putExtra("bundle",b);
                    startActivity(i);*/
                } else {
                    Toast.makeText(this, "You haven't picked Image",
                            Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.i("exc", e + "");
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                        .show();
            }
            }

           else if (requestCode == 1) {
                Uri selectedImageUri = data.getData();

                String[] filePathColumn = {MediaStore.Video.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImageUri,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                String uri = null;

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                filepath = cursor.getString(columnIndex);
                // String selectedImagePath = selectedImageUri.toString();
                cursor.close();

                f = new File(filepath);
                long sizeInBytes = f.length();
//transform in MB
                Float sizeInMb = (float)(sizeInBytes / (1024.0 * 1024.0));
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                file_size=df.format(sizeInMb)+"MB";
                uploadFile(1);


            }

            else if (requestCode == 2) {
                Uri selectedImageUri = data.getData();

                String[] filePathColumn = {MediaStore.Audio.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImageUri,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                String uri = null;

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                filepath = cursor.getString(columnIndex);
                f = new File(filepath);
                long sizeInBytes = f.length();
//transform in MB
                Float sizeInMb = (float)(sizeInBytes / (1024.0 * 1024.0));
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                file_size=df.format(sizeInMb)+"MB";
                // String selectedImagePath = selectedImageUri.toString();
                cursor.close();

                uploadFile(2);
            }
        }

    }

    private void uploadFile(final int in) {

        //if there is a file to upload
        if (filepath != null) {

            //displaying a progress dialog while upload is going on
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            progressDialog.setMessage("Please wait..");
            msqld = msql.getWritableDatabase();
            Cursor cc = msqld.query(true, msql.TB_name, null, "group_id=? and (img_local_uri=?" +
                            " or audio_local_uri=? or video_local_uri=?)", new String[]{String.valueOf(z), filepath, filepath, filepath}
                    , null, null, null, null);
            if (cc.getCount() != 0) {
                cc.moveToFirst();
                String img_server_uri = cc.getString(cc.getColumnIndex("img_server_uri"));
                String audio_server_uri = cc.getString(cc.getColumnIndex("audio_server_uri"));
                String video_server_uri = cc.getString(cc.getColumnIndex("video_server_uri"));
                if (!video_server_uri.equals("null")) {
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(filepath,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    String imgDecodableString = getStringImage(thumbnail);
                    Send_Video(video_server_uri, imgDecodableString, filepath);
                } else if (!audio_server_uri.equals("null")) {
                    Send_Audio(audio_server_uri, filepath);
                } else {
                    Bitmap thumbnail = BitmapFactory.decodeFile(filepath);
                    String imgDecodableString = getStringImage(thumbnail);
                    Send_Photo(img_server_uri, imgDecodableString, filepath);
                }
            }
            else {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReferenceFromUrl("gs://chat-764b5.appspot.com");
                char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
                StringBuilder sb = new StringBuilder();
                Random random = new Random();
                for (int i = 0; i < 20; i++) {
                    char c = chars[random.nextInt(chars.length)];
                    sb.append(c);
                }
                final String output = sb.toString();

                StorageReference riversRef = null;
                if (in == 1) {
                    riversRef = storageReference.child(z + "/videos/" + output + ".mp4");
                } else if (in == 2) {
                    riversRef = storageReference.child(z + "/audios/" + output + ".mp3");
                } else {
                    riversRef = storageReference.child(z + "/images/" + output + ".jpg");
                }

                riversRef.putFile(Uri.parse("file://" + filepath))
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //if the upload is successfull
                                //hiding the progress dialog

                                if (in == 1) {
                                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(filepath,
                                            MediaStore.Images.Thumbnails.MINI_KIND);
                                    String imgDecodableString = getStringImage(thumbnail);
                                    Send_Video(output, imgDecodableString, filepath);
                                } else if (in == 2) {
                                    Send_Audio(output, filepath);
                                } else {
                                    Bitmap thumbnail = BitmapFactory.decodeFile(filepath);
                                    String imgDecodableString = getStringImage(thumbnail);
                                    Send_Photo(output, imgDecodableString, filepath);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();

                                //and displaying error message
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //calculating progress percentage
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                //displaying percentage in progress dialog
                                progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                            }
                        });
            }
        }
            //if there is not any file
        else{
            Toast.makeText(this, "File Does Not Exists..", Toast.LENGTH_LONG).show();
        }

    }
  /*  public void sendPhoto(final String imgDecodable, final String img_base_64) {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_SEND_MESSAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.toLowerCase().indexOf("false".toLowerCase()) != -1) {

                            int userId = AppController.getInstance().getUserId();
                            String name = AppController.getInstance().getUserName();
                            String sentAt = getTimeStamp();
                            last_message="Photo";

                            Message m = new Message(userId, "", sentAt, name, img_base_64);
                            m.setImageuri(imgDecodable);
                            messages.add(m);
                            adapter.notifyDataSetChanged();

                            scrollToBottom();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("group_id",z);
                params.put("id", String.valueOf(AppController.getInstance().getUserId()));

                params.put("message", "Photo");
                params.put("name", AppController.getInstance().getUserName());


                params.put("img_base_64", img_base_64);

                return params;
            }
        };

        //Disabling retry to prevent duplicate messages
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        Log.i("end", stringRequest.toString());

        stringRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(stringRequest);
        Log.i("end1", stringRequest.toString());
    }*/

    public String Send_Photo(final String server_path, final String img_bitmap_base_64,final String local_path) {

        String url;
        int ID;
        url = URLs.URL_SEND_MESSAGE;


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("i", response);
                        progressDialog.dismiss();
                        try {
                            if (response.toLowerCase().indexOf("false".toLowerCase()) != -1) {
                                Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                                int userId = AppController.getInstance().getUserId();
                                String name = AppController.getInstance().getUserName();
                                String sentAt = getTimeStamp();
                                last_message="Image";
                                Message m = new Message(userId,"Image", sentAt, name, img_bitmap_base_64);
                                m.setImage_local_uri(local_path);
                                m.setImage_server_uri(server_path);
                                msqld=msql.getWritableDatabase();
                                ContentValues c=new ContentValues();
                                c.put("group_id",String.valueOf(z));
                                c.put("user_id",userId);
                                c.put("message","Image");
                                c.put("sentat",sentAt);
                                c.put("img_base_64",img_bitmap_base_64);
                                c.put("img_server_uri",server_path);
                                c.put("name",name);
                                c.put("img_local_uri",local_path);
                                msqld.insert(msql.TB_name,null,c);
                                msqld.close();
                                String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
                                File myDir = new File(root);
                                if (!myDir.exists()) {
                                    myDir.mkdirs();
                                }
                                File localfile = new File(myDir, server_path + ".jpg");
                                if(localfile.exists()){

                                }
                                else{
                                    localfile.createNewFile();
                                    copyFile(local_path,localfile.getPath());
                                }
                                messages.add(m);
                                adapter.notifyDataSetChanged();


                                scrollToBottom();
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                })


        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params1 = new HashMap<>();
                params1.put("id", String.valueOf(AppController.getInstance().getUserId()));
                params1.put("message", "Image");
                params1.put("name", AppController.getInstance().getUserName());
                params1.put("img_base_64", img_bitmap_base_64);
                params1.put("img_server_uri", server_path);
                params1.put("group_id",z);
                params1.put("audio_uri","null");
                params1.put("video_uri","null");
                params1.put("file_size","null");
                return params1;
            }
        };

        //Disabling retry to prevent duplicate messages
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(stringRequest);
        Log.i("chch5", "chch5");
        return null;

    }

    public String Send_Video(final String server_path, final String video_bitmap_base_64,final String local_path) {

        String url;
        int ID;
            url = URLs.URL_SEND_MESSAGE;


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("i", response);
                        progressDialog.dismiss();
                        try {
                            if (response.toLowerCase().indexOf("false".toLowerCase()) != -1) {
                                Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                                int userId = AppController.getInstance().getUserId();
                                String name = AppController.getInstance().getUserName();
                                String sentAt = getTimeStamp();
                                last_message="Video";
                                Message m = new Message(userId,"Video", sentAt, name, "null");
                                Log.i("chch2", "chch2");

                                m.setVideo_local_uri(local_path);
                                m.setVideo_server_uri(server_path);
                                m.setfile_size(file_size);

                                msqld=msql.getWritableDatabase();
                                ContentValues c=new ContentValues();
                                c.put("group_id",String.valueOf(z));
                                c.put("user_id",userId);
                                c.put("message","Video");
                                c.put("sentat",sentAt);
                                c.put("video_server_uri",server_path);
                                c.put("name",name);
                                c.put("video_local_uri",local_path);
                                c.put("file_size",file_size);
                                msqld.insert(msql.TB_name,null,c);
                                msqld.close();
                                String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
                                File myDir = new File(root);
                                if (!myDir.exists()) {
                                    myDir.mkdirs();
                                }
                                File localfile = new File(myDir, server_path + ".mp4");
                                if(localfile.exists()){

                                }
                                else{
                                    localfile.createNewFile();
                                    copyFile(local_path,localfile.getPath());
                                }
                                messages.add(m);
                                adapter.notifyDataSetChanged();

                                scrollToBottom();
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                })


        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params1 = new HashMap<>();
                params1.put("id", String.valueOf(AppController.getInstance().getUserId()));
                params1.put("message", "Video");
                params1.put("name", AppController.getInstance().getUserName());
                params1.put("img_base_64", video_bitmap_base_64);
                params1.put("file_size", file_size);
                params1.put("video_uri", server_path);
                params1.put("group_id",z);
                params1.put("audio_uri","null");
                params1.put("img_server_uri","null");
                return params1;
            }
        };

        //Disabling retry to prevent duplicate messages
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(stringRequest);
        Log.i("chch5", "chch5");
        return null;

    }

    public String Send_Audio(final String server_path, final String local_path) {

        String url;
        int ID;
        url = URLs.URL_SEND_MESSAGE;


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("i", response);
                        progressDialog.dismiss();
                        try {
                            if (response.toLowerCase().indexOf("false".toLowerCase()) != -1) {
                                Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                                int userId = AppController.getInstance().getUserId();
                                String name = AppController.getInstance().getUserName();
                                String sentAt = getTimeStamp();
                                last_message="Audio";
                                Message m = new Message(userId,"Audio", sentAt, name, "null");
                                Log.i("chch2", "chch2");

                                m.setAudio_local_uri(local_path);
                                m.setAudio_server_uri(server_path);
                                m.setfile_size(file_size);

                                msqld=msql.getWritableDatabase();
                                ContentValues c=new ContentValues();
                                c.put("group_id",String.valueOf(z));
                                c.put("user_id",userId);
                                c.put("message","Audio");
                                c.put("sentat",sentAt);
                                c.put("audio_server_uri",server_path);
                                c.put("name",name);
                                c.put("audio_local_uri",local_path);
                                c.put("file_size",file_size);
                                msqld.insert(msql.TB_name,null,c);
                                msqld.close();
                                String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
                                File myDir = new File(root);
                                if (!myDir.exists()) {
                                    myDir.mkdirs();
                                }
                                File localfile = new File(myDir, server_path + ".mp3");
                                if(localfile.exists()){

                                }
                                else{
                                    localfile.createNewFile();
                                    copyFile(local_path,localfile.getPath());
                                }
                                messages.add(m);
                                adapter.notifyDataSetChanged();

                                scrollToBottom();

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                })


        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params1 = new HashMap<>();
                params1.put("id", String.valueOf(AppController.getInstance().getUserId()));
                params1.put("message", "Audio");
                params1.put("name", AppController.getInstance().getUserName());
                params1.put("file_size", file_size);
                params1.put("audio_uri", server_path);
                params1.put("group_id",z);
                params1.put("img_base_64","null");
                params1.put("img_server_uri","null");
                params1.put("video_uri","null");
                return params1;
            }
        };

        //Disabling retry to prevent duplicate messages
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(stringRequest);
        Log.i("chch5", "chch5");
        return null;

    }


    public void snd(int i, int j, int k, String selectedPath) {

        // StringBuilder str = new StringBuilder(videol);
        //String videe;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(f);
        } catch (Exception e) {
            // TODO: handle exception
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String encodedString = null;
        long bytes_read = 0;
        byte[] bytes = new byte[500000];

        try {
            if ((k + 1) != i) {

                while (bytes_read != 500000) {
                    inputStream.skip(j);
                    bytes_read += inputStream.read(bytes, 0, 500000);
                    Log.i("status",f.length()+" "+j);

                }
            } else {
                while (bytes_read != (-1)) {
                    inputStream.skip(j);
                    Log.i("status",(f.length()-j)+" "+j+bytes_read);
                    //bytes=new byte[(int)f.length()-j];
                    bytes_read = inputStream.read(bytes,0,(int)(f.length()-j));

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
        Log.i("status","snd ending send_video called ");


    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp=Bitmap.createScaledBitmap(bmp,100,100,false);
        bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }



    @Override
    public void onStart() {
        super.onStart();
        active = true;
        SharedPreferences share=getSharedPreferences("chatbox_info",MODE_PRIVATE);
        SharedPreferences.Editor edit=share.edit();
        edit.putString("group_id",z);
        edit.commit();

    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    public static Activity currentActivity() {
        return activity;
    }


}




