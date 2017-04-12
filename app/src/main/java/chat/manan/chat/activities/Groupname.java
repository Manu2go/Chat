package chat.manan.chat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import chat.manan.chat.R;

public class Groupname extends AppCompatActivity implements View.OnClickListener{
    public Button proceed,group_img_button;
    public EditText group_name;
    public String name,img;
    public Bitmap b;
    public SharedPreferences share;
    public ImageView group_img;
    public String filepath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Group Details");
        setContentView(R.layout.activity_groupname);
        proceed=(Button)findViewById(R.id.proceed);
        filepath="null";
        group_img_button=(Button)findViewById(R.id.group_img_button);
        group_img=(ImageView)findViewById(R.id.group_img);
        group_name=(EditText)findViewById(R.id.group_name);
        proceed.setOnClickListener(this);
        group_img_button.setOnClickListener(this);
        img="Image";
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        width=width/2;
        Drawable myDrawable = ContextCompat.getDrawable(this, R.drawable.gallery);
        b=((BitmapDrawable) myDrawable).getBitmap();
        group_img.setImageBitmap(b);
        group_img.setMaxWidth(width);
    }

    @Override
    public void onClick(View v) {
        if(v==proceed){
            Editable e=group_name.getText();
            if(e==null){
                Toast.makeText(this,"Please Enter Group Name..",Toast.LENGTH_LONG).show();
            }
            else{
                name=e.toString();
                Intent i=new Intent(this,CreateGroup.class);
                share=getSharedPreferences("group_details",MODE_PRIVATE);
                SharedPreferences.Editor editor=share.edit();
                editor.putString("group_name",name);
                editor.putString("group_pic",img);
                editor.putString("group_pic_path",filepath);
                editor.commit();
                Groupname.this.finish();
                startActivity(i);
            }
        }
        if(v==group_img_button){
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
                    group_img.setImageBitmap(bitmap);
                    img= getStringImage(bitmap);
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
