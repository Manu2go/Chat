package chat.manan.chat.helper;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by root on 4/3/17.
 */
public class Users_sqlite extends SQLiteOpenHelper {

    public static String DB_name="Users";
    public static String TB_name="Users_info";
    public static int version=1;
    public static Context context;
    public static String create_query="CREATE TABLE "+TB_name+" (id int,user_name varchar(100),img_base_64 mediumtext,img_server_uri varchar(100));";
    public static String drop="Drop table if exists "+TB_name;
    public Users_sqlite(Context c){
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
