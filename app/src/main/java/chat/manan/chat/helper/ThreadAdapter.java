package chat.manan.chat.helper;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import chat.manan.chat.R;
import chat.manan.chat.activities.ChatRoomActivity;
import chat.manan.chat.activities.chk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Created by Belal on 5/29/2016.
 */
//Class extending RecyclerviewAdapter
public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> implements View.OnClickListener {

    //user id
    private int userId;
    public File localFile;
    private Context context;
    public static int width;
    public String group_id;

    //Tag for tracking self message
    private int SELF = 786;

    //ArrayList of messages object containing all the messages in the thread
    private ArrayList<Message> messages;

    public Context getContext(){return this.context;}

    //Constructor
    public ThreadAdapter(Context context, ArrayList<Message> messages, int userId) {
        this.userId = userId;
        this.messages = messages;
        this.context = context;
        SharedPreferences share=getContext().getSharedPreferences("group_details",Context.MODE_PRIVATE);
        group_id=share.getString("group_id","0");
    }

    public void setwidth(int width){
        this.width=width;
    }
    //IN this method we are tracking the self message
    @Override
    public int getItemViewType(int position) {
        //getting message object of current position
        Message message = messages.get(position);

        //If its owner  id is  equals to the logged in user id
        if (message.getUsersId() == userId) {
            //Returning self
            return SELF;
        }
        //else returning position
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Creating view
        View itemView;
        //if view type is self
        if (viewType == SELF) {
            //Inflating the layout self
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_thread, parent, false);
        } else {
            //else inflating the layout others
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_thread_other, parent, false);
        }
        //returing the view
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Adding messages to the views

        holder.img.setImageDrawable(null);
        holder.img1.setImageDrawable(null);
        holder.img2.setImageDrawable(null);
        holder.download.setVisibility(View.GONE);
        Message message = messages.get(position);
        holder.textViewMessage.setText(message.getMessage());
        holder.textViewTime.setText(message.getName() + ", " + message.getSentAt());
        Log.i("print_message",message.getMessage()+" "+message.getImage()+" "+message.getAudio_local_uri()+" "+message.getAudio_server_uri()+" "+position+" "+message.getVideo_server_uri()+" ");


        if(!message.getVideo_local_uri().equals("null")) {


            String videoFile = message.getVideo_local_uri();
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFile,
                    MediaStore.Images.Thumbnails.MINI_KIND);
            holder.img1.setMaxWidth(width);
            holder.img1.setImageBitmap(thumbnail);
            holder.img1.setTag(message.getVideo_local_uri());
            holder.img1.setOnClickListener(this);
            return;

        } else if (!message.getVideo_server_uri().equals("null")) {
            String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
            File myDir = new File(root);
            if (!myDir.exists()) {
                myDir.mkdirs();
            }


            localFile = new File(myDir,message.getVideo_server_uri()+".mp4");
            if (localFile.exists()){
                message.setVideo_local_uri(localFile.getPath());

                String videoFile = message.getVideo_local_uri();
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFile,
                        MediaStore.Images.Thumbnails.MINI_KIND);
                holder.img1.setMaxWidth(width);
                holder.img1.setImageBitmap(thumbnail);
                holder.img1.setTag(message.getVideo_local_uri());
                holder.img1.setOnClickListener(this);
                holder.download.setVisibility(View.GONE);
                return;
            }
            byte[] decodedString = Base64.decode(message.getImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            Bitmap blurred = fastblur(decodedByte,1f, 13);

            holder.img1.setMaxWidth(width);
            holder.img1.setImageBitmap(blurred);
            holder.img1.setTag(message);
            holder.download.setText("Download  "+message.getfile_size());
            holder.download.setOnClickListener(this);
            holder.download.setVisibility(View.VISIBLE);
            holder.download.setTag(holder);
            return;

        }

        if(!message.getAudio_local_uri().equals("null")) {

            holder.img2.setMaxWidth(width);
            Bitmap d = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.play);
            holder.img2.setImageBitmap(d);
            holder.img2.setTag(message.getAudio_local_uri());
            holder.img2.setOnClickListener(this);
            return;

        } else if (!message.getAudio_server_uri().equals("null")) {
            String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
            File myDir = new File(root);
            if (!myDir.exists()) {
                myDir.mkdirs();
            }


            localFile = new File(myDir,message.getAudio_server_uri()+".mp3");
            if (localFile.exists()){
                message.setAudio_local_uri(localFile.getPath());

                holder.img2.setMaxWidth(width);
                Bitmap d = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.play);
                holder.img2.setImageBitmap(d);
                holder.img2.setTag(message.getAudio_local_uri());
                holder.img2.setOnClickListener(this);
                holder.download.setVisibility(View.GONE);
                return;
            }

            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.audio);



            holder.img2.setMaxWidth(width);
            holder.img2.setImageBitmap(bmp);
            holder.img2.setTag(message);
            holder.download.setText("Download  "+message.getfile_size());
            holder.download.setOnClickListener(this);
            holder.download.setVisibility(View.VISIBLE);
            holder.download.setTag(holder);
            return;

        }


        if (!message.getImage().equals("null")) {

            byte[] decodedString = Base64.decode(message.getImage(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.img.setMaxWidth(width);
            holder.img.setImageBitmap(decodedByte);
            holder.img.setOnClickListener(this);
            holder.img.setTag(message);
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public void onClick(View v){

        if(v.getId()==R.id.image_self) {
            String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
            File myDir = new File(root);
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            ImageView img = (ImageView) v;
            Message m = (Message) img.getTag();
            if(!m.getImage_local_uri().equals("null")){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + m.getImage_local_uri()), "image/*");
                Log.i("m", Uri.parse("file://" + m).toString());
                context.startActivity(intent);
            }
            else if(!m.getImage_server_uri().equals("null")) {
                localFile = new File(myDir, m.getImage_server_uri() + ".jpg");
                if (localFile.exists()) {
                    m.setImage_local_uri(localFile.getPath());
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + m.getImage_local_uri()), "image/*");
                    Log.i("m", Uri.parse("file://" + m).toString());
                    context.startActivity(intent);
                } else {
                    download(null, m);
                }
            }
        }
        if(v.getId()==R.id.image_video){
            ImageView img = (ImageView) v;
            String m = (String) img.getTag();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse( "file://"+m), "video/*");
            Log.i("m", Uri.parse("file://" + m).toString());
            context.startActivity(intent);
        }
        if(v.getId()==R.id.image_audio){
            ImageView img = (ImageView) v;
            String m = (String) img.getTag();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse( "file://"+m), "audio/*");
            Log.i("m", Uri.parse("file://" + m).toString());
            context.startActivity(intent);
        }
        if(v.getId()==R.id.download){

            Button download=(Button)v;
            ViewHolder holder=(ViewHolder)download.getTag();
            Message m= (Message) holder.img1.getTag();
            Message m1= (Message) holder.img2.getTag();
            if(m!=null){
                download(holder,m);
            }
            else if(m1!=null){
                download(holder,m1);
            }

        }
    }

    public void download( final ViewHolder holder,final Message m){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef= storage.getReferenceFromUrl("gs://chat-764b5.appspot.com");
        StorageReference islandRef=null;
        if(m.getMessage().equals("Audio")) {
            islandRef = storageRef.child(group_id + "/audios/" + m.getAudio_server_uri() + ".mp3");
        }
        else if(m.getMessage().equals("Video")) {
            islandRef = storageRef.child(group_id + "/videos/" + m.getVideo_server_uri() + ".mp4");
        }
        else{
            islandRef = storageRef.child(group_id + "/images/" + m.getImage_server_uri() + ".jpg");
        }
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Downloading");
        progressDialog.show();
        String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
        File myDir = new File(root);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        if(m.getMessage().equals("Audio")) {
            localFile = new File(myDir,m.getAudio_server_uri()+".mp3");
        }
        else if(m.getMessage().equals("Video")) {
            localFile = new File(myDir,m.getVideo_server_uri()+".mp4");
        }
        else {
            localFile = new File(myDir,m.getImage_server_uri()+".jpg");
        }


        try {
           localFile.createNewFile();
            Log.i("ff",localFile+"");
        } catch (IOException e) {
            Log.i("ff",e+"");
            e.printStackTrace();
        }

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();

                //and displaying a success toast
                if(m.getMessage().equals("Audio")) {
                    Toast.makeText(getContext().getApplicationContext(), "File downloaded ", Toast.LENGTH_LONG).show();

                    m.setAudio_local_uri(localFile.getPath());

                    holder.img2.setMaxWidth(width);
                    Bitmap d = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.play);
                    holder.img2.setImageBitmap(d);
                    holder.img2.setTag(m.getAudio_local_uri());
                    holder.img2.setOnClickListener(ThreadAdapter.this);
                    holder.download.setVisibility(View.GONE);
                    MediaScannerConnection.scanFile(getContext(), new String[] { localFile.getPath() }, new String[] { "audio/mp3" }, null);
                }
                else if(m.getMessage().equals("Video")) {
                    Toast.makeText(getContext().getApplicationContext(), "File downloaded ", Toast.LENGTH_LONG).show();

                    m.setVideo_local_uri(localFile.getPath());

                    String videoFile = m.getVideo_local_uri();
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFile,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    holder.img1.setMaxWidth(width);
                    holder.img1.setImageBitmap(thumbnail);
                    holder.img1.setTag(m.getVideo_local_uri());
                    holder.img1.setOnClickListener(ThreadAdapter.this);
                    holder.download.setVisibility(View.GONE);
                    MediaScannerConnection.scanFile(getContext(), new String[] { localFile.getPath() }, new String[] { "video/mp4" }, null);
                }
                else{
                    Toast.makeText(getContext().getApplicationContext(), "File downloaded ", Toast.LENGTH_LONG).show();
                    m.setImage_local_uri(localFile.getPath());
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + m.getImage_local_uri()), "image/*");
                    Log.i("m", Uri.parse("file://" + m).toString());
                    context.startActivity(intent);
                }


                // Local temp file has been created
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //if the upload is not successfull
                //hiding the progress dialog
                progressDialog.dismiss();

                //and displaying error message
                Toast.makeText(getContext().getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
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

    //Initializing views
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewMessage;
        public TextView textViewTime;
        public ImageView img;
        public ImageView img1;
        public ImageView img2;
        public Button download;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewMessage = (TextView) itemView.findViewById(R.id.textViewMessage);
            textViewTime = (TextView) itemView.findViewById(R.id.textViewTime);
            img=(ImageView)itemView.findViewById(R.id.image_self);
            img1=(ImageView)itemView.findViewById(R.id.image_video);
            img2=(ImageView)itemView.findViewById(R.id.image_audio);
            download=(Button)itemView.findViewById(R.id.download);
        }
    }

    private String saveImage(Bitmap finalBitmap,Message m) {
        String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
        System.out.println(root +" Root value in saveImage Function");
        File myDir = new File(root);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }


        String iname = m.getId()+".jpg";
        File file = new File(myDir, iname);
        Log.i("iname",iname);
        if (file.exists()){
            return file.getPath();
        }
        try {

            FileOutputStream out = new FileOutputStream(file);

            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();

            out.close();
            Log.i("file","created");

        }

        catch (Exception e) {

            e.printStackTrace();
                    }
        MediaScannerConnection.scanFile(getContext(), new String[] { file.getPath() }, new String[] { "image/jpeg" }, null);
        return file.getPath();

    }
    private String saveVideo(String video_base_64,Message m) {
        String root = Environment.getExternalStoragePublicDirectory("Chat").toString();
        System.out.println(root +" Root value in saveImage Function");
        File myDir = new File(root);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }


        String vname = m.getId()+".mp4";
        File file = new File(myDir, vname);
        Log.i("iname",vname);
        if (file.exists()){
            return file.getPath();
        }
        try {
            byte[] decodedString1 = Base64.decode(video_base_64, Base64.DEFAULT);
            InputStream input = new ByteArrayInputStream(decodedString1);
            OutputStream output =  new FileOutputStream(file);
            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);

                Log.i("file",decodedString1.toString());
            }
            output.flush();

            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaScannerConnection.scanFile(getContext(), new String[] { file.getPath() }, new String[] { "video/mp4" }, null);
        return file.getPath();

    }
    public Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
}