package chat.manan.chat.gcm;


import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chat.manan.chat.activities.ChatRoomActivity;
import chat.manan.chat.activities.Chats;
import chat.manan.chat.activities.MainActivity;
import chat.manan.chat.helper.AppController;
import chat.manan.chat.helper.Groups_sqlite;
import chat.manan.chat.helper.Message;
import chat.manan.chat.helper.Messages_sqlite;

/**
 * Created by Belal on 03/11/16.
 */

public class GCMPushReceiverService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static Groups_sqlite g;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom()+ " "+remoteMessage.getData().size());
        if (remoteMessage.getData().size() >= 0) {
            try {
                JSONObject json = new JSONObject(remoteMessage.getData());
                Log.i("JSON",json+"");
                sendPushNotification(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception (onMessageReceived): " + e.getMessage());
            }
        }
    }
    //this method will display the notification
    //We are passing the JSONObject that is received from
    //firebase cloud messaging
    private void sendPushNotification(JSONObject json) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON " + json.toString());
        try {
            //getting the json data


            //parsing json data
            final String title = json.getString("title");
            final String message = json.getString("message");
            String imageUrl = "null";
            int id = json.getInt("id");
            final int userId = json.getInt("userid");
            final String sentAt = json.getString("sentat");
            final String img_base_64 = json.getString("img_base_64");
            final String video_uri = json.getString("video_uri");
            final String file_size = json.getString("file_size");
            final String audio_uri = json.getString("audio_uri");
            final String group_id = json.getString("group_id");
            final String img_server_uri = json.getString("img_server_uri");


           final Message messagObject = new Message(userId, message, sentAt, title, img_base_64);
            messagObject.setId(id);
            messagObject.setVideo_server_uri(video_uri);
            messagObject.setAudio_server_uri(audio_uri);
            messagObject.setfile_size(file_size);
            messagObject.setImage_server_uri(img_server_uri);

            Log.i("inf", "title= " + title + " message= " + message);
            SharedPreferences share = getSharedPreferences("chatbox_info", MODE_PRIVATE);
            final String grp_id = share.getString("group_id", "0");
            int usr_id= AppController.getInstance().getUserId();
            if (group_id.equals(grp_id) && ChatRoomActivity.active == true && (usr_id != userId)) {
                ChatRoomActivity.currentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Messages_sqlite m=new Messages_sqlite(getApplicationContext());
                        SQLiteDatabase s=m.getWritableDatabase();
                        ContentValues c=new ContentValues();
                        c.put("group_id",grp_id);
                        c.put("user_id",userId);
                        c.put("message",message);
                        c.put("file_size",file_size);
                        c.put("img_server_uri",img_server_uri);
                        c.put("audio_server_uri",audio_uri);
                        c.put("video_server_uri",video_uri);
                        c.put("name",title);
                        c.put("img_base_64",img_base_64);
                        c.put("sentat",sentAt);
                        s.insert(m.TB_name,null,c);
                        s.close();
                        ChatRoomActivity.messages.add(messagObject);
                        ChatRoomActivity.adapter.notifyDataSetChanged();
                        ChatRoomActivity.scrollToBottom();
                    }
                });
            }
            else if(ChatRoomActivity.active == true && group_id.equals(grp_id) && (usr_id == userId)){}
            else {

                g = new Groups_sqlite(getApplicationContext());
                SQLiteDatabase db = g.getWritableDatabase();
                ContentValues c = new ContentValues();
                c.put("Id", group_id);
                c.put("userId", userId);
                c.put("messageId", id);
                c.put("name", title);
                c.put("message", message);
                db.insert(g.TB_name, null, c);
                db.close();
                //creating MyNotificationManager object
                MyNotificationManager mNotificationManager = new MyNotificationManager(getApplicationContext());

                //creating an intent for the notification
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                //if there is no image
                if (imageUrl.equals("null")) {
                    //displaying small notification
                    mNotificationManager.showSmallNotification(title, message, intent);
                } else {
                    //if there is an image
                    //displaying a big notification
                    mNotificationManager.showBigNotification(title, message, imageUrl, intent);
                }
                if (Chats.active == true) {
                    Chats.currentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int q = 0; q < Chats.chat_groups_info.size(); q++) {
                                Chats.chatgrp u = Chats.chat_groups_info.get(q);
                                if (u.id == Integer.parseInt(group_id)) {
                                    u.count++;
                                    u.last_message=message;
                                    Chats.adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception (sendPushNotification): " + e.getMessage());
        }
    }

}
