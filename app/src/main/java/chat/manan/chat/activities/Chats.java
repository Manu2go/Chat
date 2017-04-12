package chat.manan.chat.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.manan.chat.R;
import chat.manan.chat.gcm.MyFirebaseInstanceIDService;
import chat.manan.chat.helper.AppController;
import chat.manan.chat.helper.Group_info_sqlite;
import chat.manan.chat.helper.Groups_sqlite;
import chat.manan.chat.helper.Message;
import chat.manan.chat.helper.Messages_sqlite;
import chat.manan.chat.helper.ThreadAdapter;
import chat.manan.chat.helper.URLs;
import chat.manan.chat.helper.Users_sqlite;

public class Chats extends AppCompatActivity implements AdapterView.OnItemClickListener{

    int width;
    ArrayList<chatgrp> cht;
    ProgressDialog dialog;
    ListView l;
    public static chatadapter adapter;
    String s;
    Bitmap p;
    public static boolean active;
    public static Groups_sqlite g ;
    public static SQLiteDatabase db;
    public File localFile;
    public String im;
    public static Activity activity;
    public ProgressDialog progressDialog;
    public static ArrayList<chatgrp> chat_groups_info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        l=(ListView)findViewById(R.id.chats);
        activity=this;
        getSupportActionBar().setTitle("Your Chats..");
        SharedPreferences share=getSharedPreferences("profile_details",Context.MODE_PRIVATE);
        s=share.getString("pro_pic","Image");
        if(s.equals("Image")){
            Log.i("ff",s.equals("Image")+"");
            Drawable myDrawable = ContextCompat.getDrawable(this, R.drawable.gallery);
            p = ((BitmapDrawable) myDrawable).getBitmap();
        }
        else{
            byte[] decodedString = Base64.decode(s, Base64.DEFAULT);
            p= BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        chat_groups_info=new ArrayList<chatgrp>();
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        width = width / 6;
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
        g=new Groups_sqlite(getApplicationContext());
        db=g.getWritableDatabase();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Opening chats");
        dialog.show();

        cht = new ArrayList<chatgrp>();
        isMyServiceRunning(MyFirebaseInstanceIDService.class);
        fetchGroups();
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

    private void fetchGroups() {

        Group_info_sqlite gi=new Group_info_sqlite(getApplicationContext());
        SQLiteDatabase db1=gi.getWritableDatabase();
        Cursor c = db1.query(true, gi.TB_name, null, null, null, null, null, null, null);
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex("group_id"));
            String name = c.getString(c.getColumnIndex("group_name"));
            String img = c.getString(c.getColumnIndex("img_base_64"));
            String last_message = c.getString(c.getColumnIndex("last_message"));
            String img_server_uri=c.getString(c.getColumnIndex("img_server_uri"));
            ArrayList<Integer> user_ids=new ArrayList<Integer>();
            for(int z=0;z<4;z++) {
                int w = c.getInt(c.getColumnIndex("user"+(z+1)));
                if (AppController.getInstance().getUserId() != w) {
                    user_ids.add(w);
                }
            }
            Bitmap thumbnail = null;
            if (img.equals("Image")) {
                Drawable myDrawable = ContextCompat.getDrawable(Chats.this, R.drawable.gallery);
                thumbnail = ((BitmapDrawable) myDrawable).getBitmap();
            } else {
                byte[] decodedString = Base64.decode(img, Base64.DEFAULT);
                thumbnail = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }

            chatgrp group = new chatgrp(id,name,last_message,thumbnail,user_ids,img_server_uri);
            group.count=0;
            Cursor c1=db.query(g.TB_name,null,"Id=?",new String[]{String.valueOf(id)},null,null,"messageID DESC");

            if(c1.getCount()!=0){
                group.count=c1.getCount();
                c1.moveToFirst();

                String lst_msg=c1.getString(c1.getColumnIndex("message"));
                group.last_message=lst_msg;
            }
            cht.add(group);
        }
        db1.close();
        db.close();
        adapter = new chatadapter(Chats.this, cht);
        dialog.dismiss();

        adapter=new chatadapter(Chats.this,cht);
        l.setAdapter(adapter);
        l.setOnItemClickListener(Chats.this);

    }


  /*  private void fetchGroups() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_FETCH_GROUPS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        Log.i("r",response);

                        try {
                            JSONObject res = new JSONObject(response);
                            JSONArray thread = res.getJSONArray("groups");
                            for (int i = 0; i < thread.length(); i++) {
                                JSONObject obj = thread.getJSONObject(i);
                                int id = obj.getInt("id");
                                String group_name= obj.getString("group_name");
                               String last_message=obj.getString("last_message");
                                String img_base_64 = obj.getString("img_base_64");
                                String img_server_uri = obj.getString("img_server_uri");

                                ArrayList<Integer> user_ids=new ArrayList<Integer>();
                                for(int z=0;z<4;z++) {
                                    int w = obj.getInt("user" + (z + 1));
                                    if (AppController.getInstance().getUserId() != w && w!=0) {
                                        user_ids.add(w);
                                    }
                                }
                                Bitmap thumbnail=null;
                                if(img_base_64.equals("Image")){
                                    Drawable myDrawable = ContextCompat.getDrawable(Chats.this, R.drawable.gallery);
                                    thumbnail = ((BitmapDrawable) myDrawable).getBitmap();
                                    Log.i("Image","Image");
                                }
                                else{
                                    byte[] decodedString = Base64.decode(img_base_64, Base64.DEFAULT);
                                    thumbnail = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                }
                                chatgrp group = new chatgrp(id,group_name,last_message,thumbnail,user_ids,img_server_uri);
                                group.count=0;
                                Cursor c=db.query(g.TB_name,new String[]{"Id"},"Id=?",new String[]{String.valueOf(id)},null,null,"messageID DESC");
                                if(c.getCount()!=0){
                                    group.count=c.getCount();
                                    c.moveToFirst();
                                    String lst_msg=c.getString(c.getColumnIndex("message"));
                                    group.last_message=lst_msg;
                                }
                                cht.add(group);
                            }
                            db.close();
                            adapter=new chatadapter(Chats.this,cht);
                            l.setAdapter(adapter);
                            l.setOnItemClickListener(Chats.this);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(AppController.getInstance().getUserId()));
                return params;
            }
        };
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        Log.i("end0", stringRequest.toString());
        stringRequest.setRetryPolicy(policy);

        AppController.getInstance().addToRequestQueue(stringRequest);
    }*/



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        chatgrp f=cht.get(position);
        String d=String.valueOf(f.id);
        Bitmap ig=f.b;
        String nam=f.grp_name;
        SharedPreferences share=getSharedPreferences("group_details",MODE_PRIVATE);
        SharedPreferences.Editor editor =share.edit();
        editor.putString("group_id",d);
        editor.putString("group_name",nam);
        editor.putInt("index",position);
        editor.putString("group_pic",getStringImage(ig));
        editor.putString("group_pic_server_path",f.uri);
        editor.commit();
        Intent i=new Intent(this,ChatRoomActivity.class);
        i.putExtra("user_ids",chat_groups_info.get(position).user_ids);
        Log.i("usre",chat_groups_info.get(position).user_ids+"");
        startActivity(i);
    }

    public class viewHolder{
        ImageView img;
        TextView grp_name;
        TextView lst_msg;
        TextView unread_msg_count;
        viewHolder(View v){
            img=(ImageView)v.findViewById(R.id.chat_img);
            grp_name=(TextView)v.findViewById(R.id.grp_nam);
            lst_msg=(TextView)v.findViewById(R.id.lst_msg);
            unread_msg_count=(TextView)v.findViewById(R.id.unread_msg_count);
        }
    }

    public static class chatgrp{
        public String grp_name;
        public String last_message;
        public Bitmap b;
        public int count;
        public int id;
        public String uri;
        public ArrayList<Integer> user_ids;
        chatgrp(int id,String name,String message,Bitmap bi,ArrayList<Integer> user_ids,String uri){
            grp_name=name;
            last_message=message;
            b=bi;
            this.id=id;
            this.user_ids=user_ids;
            this.uri=uri;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        //Adding our menu to toolbar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu1, menu);

        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Logout) {
            Messages_sqlite m=new Messages_sqlite(getApplicationContext());
            SQLiteDatabase s=m.getWritableDatabase();
            s.delete(m.TB_name,null,null);
            s.close();
            Groups_sqlite gt=new Groups_sqlite(getApplicationContext());
            db=gt.getWritableDatabase();
            db.delete(g.TB_name,null,null);
            db.close();
            Users_sqlite g=new Users_sqlite(getApplicationContext());
            s=g.getWritableDatabase();
            s.delete(g.TB_name,null,null);
            s.close();
            Group_info_sqlite gg=new Group_info_sqlite(getApplicationContext());
            s=gg.getWritableDatabase();
            s.delete(gg.TB_name,null,null);
            s.close();
            AppController.getInstance().logout();
            finish();
            startActivity(new Intent(Chats.this,MainActivity.class));
        }
        if (id == R.id.create_group) {
            SharedPreferences share=getSharedPreferences("group_details",MODE_PRIVATE);
            SharedPreferences.Editor editor =share.edit();
            editor.putInt("index",chat_groups_info.size());
            editor.commit();
            startActivity(new Intent(Chats.this,Groupname.class));
        }
        if (id == R.id.Profile_pic) {
            Intent i=new Intent(Chats.this,chk.class);
            i.putExtra("called_for","pro_pic");
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public class chatadapter extends BaseAdapter {
        Context context;
        chatadapter(Context c,ArrayList<chatgrp> t){
            this.context=c;
            chat_groups_info=t;
            Log.i("HH",c.toString()+" # "+t.toString());
        }

        @Override
        public int getCount() {
            return chat_groups_info.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row=convertView;
            viewHolder holder=null;
            if(row==null){
                LayoutInflater l= (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                row=l.inflate(R.layout.single_row,null);
                holder=new viewHolder(row);
                row.setTag(holder);
            }
            else{
                holder= (viewHolder) row.getTag();
            }
            holder.grp_name.setText(chat_groups_info.get(position).grp_name);
           // holder.lst_msg.setText(chat_groups_info.get(position).last_message);
            holder.lst_msg.setText(chat_groups_info.get(position).last_message);
            Bitmap y=getRoundedShape(chat_groups_info.get(position).b);
            holder.img.setImageBitmap(y);
            holder.img.setTag(chat_groups_info.get(position).uri);
            holder.img.setMaxWidth(width);
            holder.img.setOnClickListener(new View.OnClickListener()
            {

                    @Override
                    public void onClick(View v)
                    {
                        ImageView i=(ImageView)v;
                        String server_uri = (String)i.getTag();
                        if(server_uri.equals("null")){
                            Drawable myDrawable = ContextCompat.getDrawable(Chats.this, R.drawable.gallery);
                            Bitmap thumbnail = ((BitmapDrawable) myDrawable).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageBytes = baos.toByteArray();
                            im = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                            Intent u=new Intent(Chats.this,chk.class);
                            u.putExtra("called_for","chat_pic");
                            u.putExtra("image",im);
                            startActivity(u);
                        }else{
                            download(server_uri);
                        }
                    }

            });
            int c=chat_groups_info.get(position).count;
            if(c!=0){
                holder.unread_msg_count.setText(c+"");
                holder.unread_msg_count.setVisibility(View.VISIBLE);
            }else{
                holder.unread_msg_count.setVisibility(View.INVISIBLE);
            }

            return row;
        }
    }

    public void download(final String path) {
        String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
        File myDir = new File(root);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        localFile = new File(myDir, path + ".jpg");
        if (localFile.exists()) {
            Bitmap b = BitmapFactory.decodeFile(localFile.getPath());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            im = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Intent u=new Intent(Chats.this,chk.class);
            u.putExtra("called_for","chat_pic");
            u.putExtra("image",im);
            startActivity(u);
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
                    Toast.makeText(getApplicationContext(), "Login successful.. ", Toast.LENGTH_LONG).show();
                    // Local temp file has been created
                    Bitmap b = BitmapFactory.decodeFile(localFile.getPath());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();
                    im = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    progressDialog.dismiss();
                    Intent u=new Intent(Chats.this,chk.class);
                    u.putExtra("called_for","chat_pic");
                    u.putExtra("image",im);
                    startActivity(u);
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


    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp=Bitmap.createScaledBitmap(bmp,100,100,false);
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = width+20;
        int targetHeight = width+20;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }
    @Override
    public void onStart() {
        super.onStart();
        active = true;
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