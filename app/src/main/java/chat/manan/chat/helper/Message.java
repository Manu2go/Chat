package chat.manan.chat.helper;

import android.widget.ImageView;

import java.io.Serializable;

/**
 * Created by Belal on 5/29/2016.
 */
public class Message {
    private int usersId;
    private String message;
    private String sentAt;
    private String name;
    private String image;
    private int id;
    private String file_size;

    private String image_local_uri;
    private String image_server_uri;
    private String video_server_uri;
    private String video_local_uri;
    private String audio_server_uri;
    private String audio_local_uri;

    public Message(int usersId, String message, String sentAt, String name, String img) {
        this.usersId = usersId;
        this.message = message;
        this.sentAt = sentAt;
        this.name = name;
        this.image=img;
        image_local_uri="null";
        image_server_uri="null";
        video_local_uri="null";
        video_server_uri="null";
        audio_local_uri="null";
        audio_server_uri="null";
        file_size="0";
    }

    public int getUsersId() {
        return usersId;
    }

    public String getMessage() {
        return message;
    }

    public String getSentAt() {
        return sentAt;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }
    public String getImage_server_uri() {
        return image_server_uri;
    }
    public void setImage_server_uri(String s) {
        this.image_server_uri=s;
    }
    public String getImage_local_uri() {
        return image_local_uri;
    }
    public void setImage_local_uri(String s) {
        this.image_local_uri=s;
    }
    public void setId(int id){this.id=id;}
    public int getId(){return  this.id;}
    public void setVideo_server_uri(String s) {
        this.video_server_uri=s;
    }
    public String getVideo_server_uri(){return  this.video_server_uri;}
    public void setAudio_server_uri(String s) {
        this.audio_server_uri=s;
    }
    public String getAudio_server_uri(){return  this.audio_server_uri;}
    public void setVideo_local_uri(String s) {
        this.video_local_uri=s;
    }
    public String getVideo_local_uri(){return  this.video_local_uri;}
    public void setAudio_local_uri(String s) {
        this.audio_local_uri=s;
    }
    public String getAudio_local_uri(){return  this.audio_local_uri;}
    public void setfile_size(String s) {
        this.file_size=s;
    }
    public String getfile_size(){return  this.file_size;}
}