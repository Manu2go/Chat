package chat.manan.chat.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import chat.manan.chat.R;
import chat.manan.chat.helper.video_media_controller;

public class chk extends Activity {

    public Bitmap pic;
    public ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chk);
        Intent i=getIntent();
        String s=i.getStringExtra("called_for");
        String image=null;
        if(s.equals("pro_pic")){
            SharedPreferences share=getSharedPreferences("profile_details",MODE_PRIVATE);
            image=share.getString("pro_pic","Image");
        }
        if(s.equals("group_pic")){
            SharedPreferences share=getSharedPreferences("group_details",MODE_PRIVATE);
            image=share.getString("group_pic","Image");
        }
        if(s.equals("chat_pic")){
            image=i.getStringExtra("image");
        }

        if(image.equals("Image")){
            Drawable d = ContextCompat.getDrawable(this, R.drawable.gallery);
            pic = ((BitmapDrawable) d).getBitmap();
        }
        else{
            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
            pic= BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        img=(ImageView)findViewById(R.id.profile_img);
        img.setImageBitmap(pic);
    }

}
