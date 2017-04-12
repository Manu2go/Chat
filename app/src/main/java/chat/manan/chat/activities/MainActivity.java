package chat.manan.chat.activities;


import android.app.ActivityManager;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import chat.manan.chat.R;
import chat.manan.chat.gcm.MyFirebaseInstanceIDService;
import chat.manan.chat.helper.AppController;
import chat.manan.chat.helper.Group_info_sqlite;
import chat.manan.chat.helper.Message;
import chat.manan.chat.helper.URLs;
import chat.manan.chat.helper.Users_sqlite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Views
    private EditText editTextEmail;
    private EditText editTextName;

    private Button buttonEnter;
    public String img_base_64;
    public Button pro_pic_button;
    public ImageView pro_pic;
    public int width;
    public ProgressDialog progressDialog,progressDialog1;
    public SharedPreferences share;
    public String filepath;
    public String img_uri;
    public File localFile;
    public  FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initiailizing views
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextName = (EditText) findViewById(R.id.editTextName);
        share = getSharedPreferences("profile_details", Context.MODE_PRIVATE);
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        width=width/2;
        progressDialog = new ProgressDialog(this);
        progressDialog1 = new ProgressDialog(this);
        buttonEnter = (Button) findViewById(R.id.buttonEnter);
        pro_pic_button=(Button)findViewById(R.id.pro_pic_button);
        pro_pic=(ImageView)findViewById(R.id.pro_pic);
        pro_pic_button.setOnClickListener(this);

        Drawable myDrawable = ContextCompat.getDrawable(this, R.drawable.gallery);
        Bitmap myLogo = ((BitmapDrawable) myDrawable).getBitmap();
        img_base_64="Image";
        pro_pic.setImageBitmap(myLogo);
        pro_pic.setMaxWidth(width);

        buttonEnter.setOnClickListener(this);

        //If the user is already logged in
        //Starting chat room
        if (AppController.getInstance().isLoggedIn()) {
            finish();
            startActivity(new Intent(this, Chats.class));
        }

    }


   /* @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Firebase singin error", "signInAnonymously:FAILURE", exception);
                    }
                });
    }*/

    private void uploadFile() {

        //if there is a file to upload
        if (filepath != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference= storage.getReferenceFromUrl("gs://chat-764b5.appspot.com");


            //displaying a progress dialog while upload is going on
            char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 20; i++) {
                char c = chars[random.nextInt(chars.length)];
                sb.append(c);
            }
            final String output = sb.toString();

            StorageReference riversRef=null;

            riversRef = storageReference.child("Profile_pics"+"/images/"+output+".jpg");

            riversRef.putFile(Uri.parse("file://"+filepath))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            registerUser(output);
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
           registerUser("null");

          //  Toast.makeText(this,"File Does Not Exists..",Toast.LENGTH_LONG).show();
        }
    }


    //Method to register user
    private void registerUser(final String img_server_path) {

        final String name = editTextName.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Users_sqlite s=new Users_sqlite(getApplicationContext());
                            SQLiteDatabase db=s.getWritableDatabase();
                            JSONObject object = new JSONObject(response);
                            int id = object.getInt("id");
                            String name = object.getString("name");
                            String email = object.getString("email");
                            img_base_64=object.getString("img_base_64");
                            img_uri=object.getString("img_server_uri");
                            JSONArray thread = object.getJSONArray("users");
                            ContentValues c=new ContentValues();
                            for (int i = 0; i < thread.length(); i++) {
                                    JSONObject obj = thread.getJSONObject(i);
                                    int ID = obj.getInt("id");
                                    String group_name = obj.getString("user_name");
                                    String img_base_64 = obj.getString("img_base_64");
                                    String img_uri = obj.getString("img_server_uri");
                                    c.put("id",ID+"");
                                    c.put("user_name",group_name);
                                    c.put("img_base_64",img_base_64);
                                    c.put("img_server_uri",img_uri);
                                    db.insert(s.TB_name,null,c);
                            }
                            db.close();

                            Group_info_sqlite g= new Group_info_sqlite(getApplicationContext());
                            db=g.getWritableDatabase();
                            thread = object.getJSONArray("groups");
                            c=new ContentValues();
                            for (int i = 0; i < thread.length(); i++) {
                                JSONObject obj = thread.getJSONObject(i);
                                int ID = obj.getInt("group_id");
                                String group_name = obj.getString("group_name");
                                String img_base_64 = obj.getString("img_base_64");
                                String img_uri = obj.getString("img_server_uri");
                                String last_message = obj.getString("last_message");
                                int user1=obj.getInt("user1");
                                int user2=obj.getInt("user2");
                                int user3=obj.getInt("user3");
                                int user4=obj.getInt("user4");
                                c.put("group_id",ID+"");
                                c.put("group_name",group_name);
                                c.put("img_base_64",img_base_64);
                                c.put("img_server_uri",img_uri);
                                c.put("img_server_uri",img_uri);
                                c.put("last_message",last_message);
                                c.put("user1",user1);
                                c.put("user2",user2);
                                c.put("user3",user3);
                                c.put("user4",user4);
                                db.insert(g.TB_name,null,c);
                            }
                            db.close();


                            //Login user
                            AppController.getInstance().loginUser(id, name, email);
                            if(!img_base_64.equals("Image")){
                                download(img_uri);
                            }
                            else{


                                Drawable myDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.gallery);
                                Bitmap thumbnail = ((BitmapDrawable) myDrawable).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] imageBytes = baos.toByteArray();
                                img_base_64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                                Intent i=new Intent(MainActivity.this, Chats.class);
                                SharedPreferences.Editor editor=share.edit();
                                editor.putString("pro_pic",img_base_64);
                                editor.commit();
                                progressDialog.dismiss();
                                //Starting chat room we need to create this activity
                                startActivity(new Intent(MainActivity.this, Chats.class));
                            }
                        } catch (JSONException e) {
                            Log.i("eee",e+"");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("eeer",error+"");
                        progressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("img_base_64",img_base_64);
                params.put("img_server_uri",img_server_path);
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
    }

    public void download(final String path){
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
            img_base_64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Intent i=new Intent(MainActivity.this, Chats.class);
            SharedPreferences.Editor editor=share.edit();
            editor.putString("pro_pic",img_base_64);
            editor.commit();
            progressDialog.dismiss();
            //Starting chat room we need to create this activity
            startActivity(new Intent(MainActivity.this, Chats.class));
        } else {
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
                    img_base_64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    progressDialog.dismiss();
                    Intent i=new Intent(MainActivity.this, Chats.class);
                    SharedPreferences.Editor editor=share.edit();
                    editor.putString("pro_pic",img_base_64);
                    editor.commit();
                    //Starting chat room we need to create this activity
                    startActivity(new Intent(MainActivity.this, Chats.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //if the upload is not successfull
                    //hiding the progress dialog
                    //and displaying error message
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        //Checking if user is logged in
        if (AppController.getInstance().isLoggedIn()) {
            finish();
            startActivity(new Intent(this, Chats.class));
        }
    }


    @Override
    public void onClick(View v) {
        if (v == buttonEnter) {
            progressDialog.setTitle("Please wait!..");
            progressDialog.show();
            if (img_base_64.equals("Image")) {
                registerUser("null");
            } else {
                uploadFile();
            }
        }
        if (v == pro_pic_button) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);


            this.startActivityForResult(galleryIntent, 0);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // When an Image is picked
        if (requestCode == 0 && resultCode == RESULT_OK) {
            // Get the Image from data
            try {

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
                    cursor.close();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
                    pro_pic.setImageBitmap(bitmap);
                    img_base_64 = getStringImage(bitmap);

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
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp=Bitmap.createScaledBitmap(bmp,100,100,false);
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


}