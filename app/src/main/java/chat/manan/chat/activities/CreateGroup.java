package chat.manan.chat.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import chat.manan.chat.R;
import chat.manan.chat.helper.AppController;
import chat.manan.chat.helper.Group_info_sqlite;
import chat.manan.chat.helper.Groups_sqlite;
import chat.manan.chat.helper.Message;
import chat.manan.chat.helper.Messages_sqlite;
import chat.manan.chat.helper.URLs;
import chat.manan.chat.helper.Users_sqlite;

public class CreateGroup extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    int width;
    ArrayList<user> cht, cht1;
    ProgressDialog dialog;
    ListView l;
    Button create;
    String group_name;
    String img;
    int ch;
    chatadapter adapter;
    SharedPreferences share;
    public Users_sqlite s;
    public SQLiteDatabase db;
    public File localFile;
    public String im;
    public ProgressDialog progressDialog;
    public String group_pic_path;
    public ArrayList<Integer> positions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Drawable myDrawable = getResources().getDrawable(R.drawable.gallery);
       // Bitmap myLogo = ((BitmapDrawable) myDrawable).getBitmap();
        //img = getStringImage(myLogo);
        s=new Users_sqlite(getApplicationContext());
        db=s.getWritableDatabase();
        positions=new ArrayList<Integer>();
        create = (Button) findViewById(R.id.create);
        l = (ListView) findViewById(R.id.users);
        create.setOnClickListener(this);
        l.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        progressDialog = new ProgressDialog(this);
        share=getSharedPreferences("group_details",MODE_PRIVATE);
        group_name = share.getString("group_name","Group");
        img=share.getString("group_pic","Image");
        group_pic_path=share.getString("group_pic_path","null");
        getSupportActionBar().setTitle("Add People..");
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        width = width / 6;
        progressDialog.setMessage("Fetching Users..");
        progressDialog.show();

        cht = new ArrayList<user>();
        cht1 = new ArrayList<user>();
        fetchUsers();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Drawable d = view.getBackground();
        int c = ((ColorDrawable) d).getColor();
        Log.i("bc", "bc");

        int color = Color.parseColor("#44000000");
        int color1 = Color.parseColor("#80ffffff");
        if (c == color) {
            view.setBackgroundColor(color1);
            cht1.remove(cht.get(position));
            positions.remove(Integer.valueOf(position));
            ch = ch - 1;
            Log.i("color",color+" "+ch);
        } else {
            if (ch == 3) {
                Toast.makeText(this, "Only 3 members can be added in a group..", Toast.LENGTH_LONG).show();
            } else {
                view.setBackgroundColor(color);
                cht1.add(cht.get(position));
                positions.add(position);
                ch++;
                Log.i("color1",color1+" "+ch);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == create) {
            progressDialog.setMessage("Creating Group..");
            progressDialog.show();
            if (img.equals("Image")) {
                createGroup("null");
            } else {
                uploadFile();
            }
        }
    }

    private void fetchUsers() {

        Cursor c = db.query(true, s.TB_name, null, null, null, null, null, null, null);
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex("id"));
            String name = c.getString(c.getColumnIndex("user_name"));
            String img = c.getString(c.getColumnIndex("img_base_64"));
            String img_server_path=c.getString(c.getColumnIndex("img_server_uri"));
            Bitmap thumbnail = null;
            if (img.equals("Image")) {
                Drawable myDrawable = ContextCompat.getDrawable(CreateGroup.this, R.drawable.gallery);
                thumbnail = ((BitmapDrawable) myDrawable).getBitmap();
            } else {
                byte[] decodedString = Base64.decode(img, Base64.DEFAULT);
                thumbnail = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }

            user u = new user(id, name, thumbnail,img_server_path);

            cht.add(u);

        }
        db.close();
        adapter = new chatadapter(CreateGroup.this, cht);
        progressDialog.dismiss();

        l.setAdapter(adapter);
        l.setOnItemClickListener(CreateGroup.this);
        adapter.notifyDataSetChanged();

    }

    private void uploadFile() {

        //if there is a file to upload
        if (group_pic_path != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference= storage.getReferenceFromUrl("gs://chat-764b5.appspot.com");


            //displaying a progress dialog while upload is going on
            progressDialog.setTitle("Please wait!..");
            progressDialog.show();
            char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 20; i++) {
                char c = chars[random.nextInt(chars.length)];
                sb.append(c);
            }
            final String output = sb.toString();

            StorageReference riversRef=null;

            riversRef = storageReference.child("Group_pics"+"/images/"+output+".jpg");

            riversRef.putFile(Uri.parse("file://"+group_pic_path))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            createGroup(output);
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
        //if there is not any file
        else {
            createGroup("null");
        }
    }

    public void createGroup(final String img_server_path) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_CREATE_GROUP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.i("chch3", "chch3");
                        JSONObject j = null;
                        try {
                            j = new JSONObject(response);
                            String i = j.getString("result");
                            if (i.equals("0")) {
                                Toast.makeText(CreateGroup.this, "Please try again! Group not created..", Toast.LENGTH_LONG).show();
                            } else {

                                Log.i("responsee", response.toString());
                                Intent in = new Intent(CreateGroup.this, ChatRoomActivity.class);
                                share=getSharedPreferences("group_details",MODE_PRIVATE);
                                SharedPreferences.Editor editor=share.edit();
                                editor.putString("group_id",i);
                                editor.putString("group_pic_server_path",img_server_path);
                                editor.commit();
                                int index=share.getInt("index",0);
                                String name=share.getString("group_name","Group");
                                String pic=share.getString("group_pic","Image");

                                Group_info_sqlite gg=new Group_info_sqlite(getApplicationContext());
                                SQLiteDatabase db=gg.getWritableDatabase();
                                ContentValues c1=new ContentValues();
                                c1.put("group_id",i);
                                c1.put("group_name",name);
                                c1.put("img_base_64",pic);
                                c1.put("last_message","Message");
                                c1.put("img_server_uri",img_server_path);

                                Bitmap y=null;
                                if(pic.equals("Image")){
                                    Drawable d = ContextCompat.getDrawable(CreateGroup.this, R.drawable.gallery);
                                    y = ((BitmapDrawable) d).getBitmap();
                                }
                                else{
                                    byte[] decodedString = Base64.decode(pic, Base64.DEFAULT);
                                    y= BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                }
                                ArrayList<Integer> r=new ArrayList<Integer>();
                                int z=0;
                                for(z=0;z<cht1.size();z++){
                                    r.add(cht1.get(z).id);
                                    c1.put("user"+(z+1),cht1.get(z).id);
                                }
                                for(;z<3;z++){
                                    r.add(0);
                                    c1.put("user"+(z+1),0);
                                }
                                c1.put("user"+(z+1),AppController.getInstance().getUserId());
                                db.insert(gg.TB_name,null,c1);
                                Chats.chatgrp g=new Chats.chatgrp(Integer.parseInt(i),name,"Message",y,r,img_server_path);
                                Chats.chat_groups_info.add(g);
                                in.putExtra("user_ids", r);
                                CreateGroup.this.finish();
                                startActivity(in);
                            }
                        } catch (JSONException e) {
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
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                Log.i("cr1","cr1");
                params.put("group_name", group_name);
                params.put("user1", String.valueOf(AppController.getInstance().getUserId()));
                params.put("img_base_64", img);
                params.put("img_server_uri",img_server_path);
                int i = 0;
                while (i < cht1.size()) {
                    params.put("user" + (i+2), String.valueOf(cht1.get(i).id));
                    Log.i("cr2","cr2");
                    i++;
                }
               while(i<3){
                    params.put("user" + (i+2), String.valueOf(0));
                   i++;
                }
                return params;
            }
        };
        Log.i("cr","cr");
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);


        stringRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    class viewHolder {
        ImageView img;
        TextView grp_name;
        TextView lst_msg;

        viewHolder(View v) {
            img = (ImageView) v.findViewById(R.id.chat_img);
            grp_name = (TextView) v.findViewById(R.id.grp_nam);
            lst_msg = (TextView) v.findViewById(R.id.lst_msg);
        }
    }

    class user {
        String user_name;
        String last_message;
        Bitmap b;
        int id;
        String img_server_uri;

        user(int id, String name, Bitmap bi,String ii) {
            user_name = name;
            last_message = "";
            b = bi;
            this.id = id;
            this.img_server_uri=ii;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        //Adding our menu to toolbar
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuLogout) {
            Messages_sqlite m=new Messages_sqlite(getApplicationContext());
            SQLiteDatabase sf=m.getWritableDatabase();
            sf.delete(m.TB_name,null,null);
            sf.close();
            Groups_sqlite g=new Groups_sqlite(getApplicationContext());
            SQLiteDatabase sh=g.getWritableDatabase();
            Users_sqlite sts= new Users_sqlite(getApplicationContext());
            db=sts.getWritableDatabase();
            sh.delete(g.TB_name,null,null);
            db.delete(s.TB_name,null,null);
            sh.close();
            db.close();
            Group_info_sqlite gg=new Group_info_sqlite(getApplicationContext());
            sh=gg.getWritableDatabase();
            sh.delete(gg.TB_name,null,null);
            sh.close();
            AppController.getInstance().logout();
            finish();
            startActivity(new Intent(CreateGroup.this, MainActivity.class));
        }
        if (id == R.id.Profile_pic) {
            Intent i=new Intent(CreateGroup.this,chk.class);
            i.putExtra("called_for","pro_pic");
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public class chatadapter extends BaseAdapter {
        ArrayList<user> chat_users_info;
        Context context;

        chatadapter(Context c, ArrayList<user> t) {
            this.context = c;
            this.chat_users_info = t;
            Log.i("adap", "adap");
        }

        @Override
        public int getCount() {
            return chat_users_info.size();
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
            View row = convertView;
            viewHolder holder = null;
            if (row == null) {
                LayoutInflater l = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                row = l.inflate(R.layout.single_row, null);
                holder = new viewHolder(row);
                row.setTag(holder);
            } else {
                holder = (viewHolder) row.getTag();
            }
            Drawable d = row.getBackground();
            int c = ((ColorDrawable) d).getColor();
            int color = Color.parseColor("#44000000");
            int color1 = Color.parseColor("#80ffffff");
            int u=0;
            for(int kk=0;kk<positions.size();kk++){
                if(position==positions.get(kk)){
                    u=1;
                    row.setBackgroundColor(color);
                }
            }
            if(u==1){
                row.setBackgroundColor(color);
            }
            else{
                row.setBackgroundColor(color1);
            }
            holder.grp_name.setText(chat_users_info.get(position).user_name);
            holder.lst_msg.setText(chat_users_info.get(position).last_message);
            Bitmap y=getRoundedShape(chat_users_info.get(position).b);
            holder.img.setImageBitmap(y);
            holder.img.setTag(chat_users_info.get(position).img_server_uri);
            holder.img.setMaxWidth(width);
            holder.img.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ImageView i=(ImageView)v;
                    String server_uri = (String)i.getTag();
                    if(server_uri.equals("null")){
                        Drawable myDrawable = ContextCompat.getDrawable(CreateGroup.this, R.drawable.gallery);
                        Bitmap thumbnail = ((BitmapDrawable) myDrawable).getBitmap();
                        im = getStringImage(thumbnail);
                        Intent u=new Intent(CreateGroup.this,chk.class);
                        u.putExtra("called_for","chat_pic");
                        u.putExtra("image",im);
                        startActivity(u);
                    }else{
                        download(server_uri);
                    }
                }
            });
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
            Intent u=new Intent(CreateGroup.this,chk.class);
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

            islandRef = storageRef.child("Profile_pics" + "/images/" + path + ".jpg");
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
                    Intent u=new Intent(CreateGroup.this,chk.class);
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
}