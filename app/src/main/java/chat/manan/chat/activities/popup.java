package chat.manan.chat.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import chat.manan.chat.R;

/**
 * Created by root on 17/1/17.
 */
public class popup extends Fragment implements AdapterView.OnItemClickListener{

    View v;
    GridView g;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.popup, container, false);

        g=(GridView)v.findViewById(R.id.popup_items);
        g.setAdapter(new adapter1(getContext()));
        g.setOnItemClickListener(this);



        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView text= (TextView) view.findViewById(R.id.popupitem_text);
        String t=text.getText().toString();
        Log.i("frg","gi");
        if(t.equals("Gallery")){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

// Start the Intent
           getActivity().startActivityForResult(galleryIntent, 0);
        }

        if(t.equals("Audio")){
           /* Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getActivity().startActivityForResult(Intent.createChooser(intent,"Select Video"),1);*/
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            getActivity().startActivityForResult(galleryIntent, 2);
        }

        if(t.equals("Video")){
           /* Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getActivity().startActivityForResult(Intent.createChooser(intent,"Select Video"),1);*/
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            getActivity().startActivityForResult(galleryIntent, 1);
        }


    }

    class popup_item{
        int id;
        String text;
    }

    class ViewHolder {
        ImageView myproduct;
        TextView nam;
        TextView price;

        ViewHolder(View v) {
            myproduct = (ImageView) v.findViewById(R.id.popupitem_image);
            nam = (TextView) v.findViewById(R.id.popupitem_text);
        }
    }

    class adapter1 extends BaseAdapter{

        ArrayList<popup_item> a;
        Context c;
        ImageView i;
        TextView t;

        adapter1(Context c){
            this.c=c;
            a=new ArrayList<popup_item>();
            Resources res= c.getResources();
            String[] item= res.getStringArray(R.array.popup_items_text);
            int[] id={R.drawable.gallery,R.drawable.audio,R.drawable.video};
            int i=0;

            for(i=0;i<id.length;i++){
                popup_item p=new popup_item();
                p.id=id[i];
                p.text=item[i];
                a.add(p);

            }
        }

        @Override
        public int getCount() {
            return a.size();
        }

        @Override
        public Object getItem(int position) {
            return a.get(position);
        }

        @Override
        public long getItemId(int position) {


            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder=null;
          if (row == null) {
                LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.popupitem, parent, false);
              holder = new ViewHolder(row);
              row.setTag(holder);

            }
          else {
              holder = (ViewHolder) row.getTag();

          }
            popup_item p=a.get(position);
            holder.myproduct.setImageResource(p.id);
            holder.nam.setText(p.text);
            return row;
        }
    }
}


