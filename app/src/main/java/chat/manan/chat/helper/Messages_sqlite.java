package chat.manan.chat.helper;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by root on 4/3/17.
 */
public class Messages_sqlite extends SQLiteOpenHelper {

    public static String DB_name="Messages";
    public static String TB_name="Messages";
    public static int version=1;
    public static Context context;
    public static String create_query="CREATE TABLE "+TB_name+" (group_id int,user_id int,message_id int primary key," +
            "message varchar(100) NOT NULL,name varchar(50),sentat varchar(20),img_base_64 mediumtext default 'null'," +
            "img_server_uri varchar(50) default 'null',audio_server_uri varchar(50) default 'null'," +
            "video_server_uri varchar(50) default 'null',file_size varchar(20) default 'null'," +
            "audio_local_uri varchar(100) default 'null'," +
            "video_local_uri varchar(100) default 'null'," +
            "img_local_uri varchar(100) default 'null');";
    public static String drop="Drop table if exists "+TB_name;
    public Messages_sqlite(Context c){
        super(c,DB_name,null,version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(create_query);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(drop);
            onCreate(db);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
