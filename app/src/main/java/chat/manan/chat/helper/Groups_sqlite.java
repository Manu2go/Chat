package chat.manan.chat.helper;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by root on 4/3/17.
 */
public class Groups_sqlite extends SQLiteOpenHelper {

    public static String DB_name="Groups";
    public static String TB_name="Groups_msgs";
    public static int version=1;
    public static Context context;
    public static String create_query="CREATE TABLE "+TB_name+" (Id int,userId int,messageId int," +
            "message varchar(100) NOT NULL,name varchar(50));";
    public static String drop="Drop table if exists "+TB_name;
    public Groups_sqlite(Context c){
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
