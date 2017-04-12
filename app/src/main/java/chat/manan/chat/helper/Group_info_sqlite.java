package chat.manan.chat.helper;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by root on 23/3/17.
 */
public class Group_info_sqlite extends SQLiteOpenHelper {
    public static String DB_name="Group_info";
    public static String TB_name="Groups";
    public static int version=1;
    public static Context context;
    public static String create_query="CREATE TABLE "+TB_name+" (group_id int,group_name varchar(100),last_message varchar(500)," +
            "img_base_64 mediumtext,user1 int,user2 int,user3 int,user4 int,img_server_uri varhcar(100));" ;
    public static String drop="Drop table if exists "+TB_name;
    public Group_info_sqlite(Context c){
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

