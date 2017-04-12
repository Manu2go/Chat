package chat.manan.chat.activities;

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
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import chat.manan.chat.R;
import chat.manan.chat.helper.AppController;
import chat.manan.chat.helper.Users_sqlite;

public class group_inf extends AppCompatActivity  {

    public ArrayList<Integer> user_ids;
    public Users_sqlite u;
    public SQLiteDatabase db;
    public String[] users;
    public Cursor c;
    public String b;
    public Bitmap y;
    public int width;
    public Toolbar toolbar;
    public ArrayList<user> usr;
    public CollapsingToolbarLayout collapsingToolbar;
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_inf);
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        width=width/6;
        usr=new ArrayList<user>();
        user uu=new user(0,"Recycler_View_Heading",null);
        usr.add(uu);
        Intent i=getIntent();
        b=i.getStringExtra("group_pic");
        user_ids=(ArrayList<Integer>)i.getSerializableExtra("user_ids");
        u=new Users_sqlite(getApplicationContext());
        Log.i("group_inf","group_inf");
        db=u.getWritableDatabase();
        users=new String[user_ids.size()];
        for(int y=0;y<user_ids.size();y++){
            users[y]=user_ids.get(y).toString();
        }
        c=db.query(true,u.TB_name,new String[]{"id","user_name","img_base_64"},"id in (?,?,?)",users,null,null,null,null);
        Bitmap user_pic;
        Log.i("opeusers",c.getCount()+"");
        while(c.moveToNext()) {
            int id=c.getInt(c.getColumnIndex("id"));
            Log.i("ope",id+"");
            String name=c.getString(c.getColumnIndex("user_name"));
            String img_base_64=c.getString(c.getColumnIndex("img_base_64"));
            if(img_base_64.equals("Image")){
                Drawable myDrawable = ContextCompat.getDrawable(group_inf.this, R.drawable.gallery);
                user_pic = ((BitmapDrawable) myDrawable).getBitmap();
            }
            else{
                byte[] decodedString = Base64.decode(img_base_64, Base64.DEFAULT);
                user_pic = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }
                user u =new user(id,name,user_pic);
                usr.add(u);

        }

        SharedPreferences share=getSharedPreferences("profile_details",MODE_PRIVATE);
        String image=share.getString("pro_pic","Image");
        if(image.equals("Image")){
            Drawable myDrawable = ContextCompat.getDrawable(group_inf.this, R.drawable.gallery);
            user_pic = ((BitmapDrawable) myDrawable).getBitmap();
        }
        else{
            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
            user_pic = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        user u=new user(AppController.getInstance().getUserId(),AppController.getInstance().getUserName(),user_pic);
        usr.add(u);
        Log.i("usr",usr+"");
        share=getSharedPreferences("group_details",Context.MODE_PRIVATE);
        String nam=share.getString("group_name","Group");
        toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(nam);

        ImageView im=(ImageView)findViewById(R.id.header);
        if(b.equals("Image")){
            Drawable d = ContextCompat.getDrawable(this, R.drawable.gallery);
            y = ((BitmapDrawable) d).getBitmap();
        }
        else{
            byte[] decodedString = Base64.decode(b, Base64.DEFAULT);
            y= BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        im.setImageBitmap(y);
        /*int height=display.getHeight();
        height=height/3;
        im.setMaxHeight(height);*/

        chatadapter adapter=new chatadapter(this,usr);
        RecyclerView r=(RecyclerView)findViewById(R.id.scrollableview);
        r.setAdapter(adapter);
        r.setLayoutManager(new LinearLayoutManager(this));
    }


    class viewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView grp_name;
        TextView lst_msg;

        viewHolder(View v) {
            super(v);
            img = (ImageView) v.findViewById(R.id.chat_img);
            grp_name = (TextView) v.findViewById(R.id.grp_nam);
            lst_msg = (TextView) v.findViewById(R.id.lst_msg);
        }
    }

    class header_viewHolder extends RecyclerView.ViewHolder{
        TextView heading;
        header_viewHolder(View v) {
            super(v);
            heading = (TextView) v.findViewById(R.id.recycler_view_header);
        }
    }

    class user {
        String user_name;
        String last_message;
        Bitmap b;
        int id;

        user(int id, String name, Bitmap bi) {
            user_name = name;
            last_message = "";
            b = bi;
            this.id = id;
        }
    }

    public class chatadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<user> chat_users_info;
        Context context;
        LayoutInflater l;

        chatadapter(Context c, ArrayList<user> t) {
            this.context = c;
            this.chat_users_info = t;
            Log.i("adap", "adap");
            l=LayoutInflater.from(context);
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View v = l.inflate(R.layout.recycler_view_header,null);
                header_viewHolder vh=new header_viewHolder(v);
                return vh;
            } else {
                View v = l.inflate(R.layout.single_row, null,false);
                viewHolder vh = new viewHolder(v);
                return vh;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder1, int position) {
            if(holder1 instanceof viewHolder) {
                viewHolder holder=(viewHolder)holder1;
                holder.grp_name.setText(chat_users_info.get(position).user_name);
                holder.lst_msg.setText(chat_users_info.get(position).last_message);
                Bitmap y = getRoundedShape(chat_users_info.get(position).b);
                holder.img.setImageBitmap(y);
                holder.img.setMaxWidth(width);
                /*holder.img.setTag(chat_users_info.get(position).b);
                holder.img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView i = (ImageView) v;
                        Bitmap bitmap = (Bitmap) i.getTag();
                        String s = getStringImage(bitmap);
                        Intent u = new Intent(group_inf.this, chk.class);
                        u.putExtra("called_for", "chat_pic");
                        u.putExtra("image", s);
                        startActivity(u);
                    }
                });*/
            }
            else if(holder1 instanceof header_viewHolder){
                ((header_viewHolder) holder1).heading.setText("Participants");
                Log.i("Participants","pp");
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(isPositionHeader (position)) {
                return TYPE_HEADER;
            } else  {
                return TYPE_ITEM;
            }
        }

        public boolean isPositionHeader (int position) {
            return position == 0;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return chat_users_info.size();
        }

    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
