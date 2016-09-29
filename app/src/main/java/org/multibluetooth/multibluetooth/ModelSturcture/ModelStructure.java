package org.multibluetooth.multibluetooth.ModelSturcture;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by YS on 2016-09-29.
 */
public class ModelStructure extends SQLiteOpenHelper {
    protected static final int DB_VERSION = 4;

    public ModelStructure(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);

        Log.d("ModelStructure", "생성");
        db.execSQL("CREATE TABLE DriveInfo ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "drive_id INTEGER NOT NULL, " +
                "vehicle_speed INTEGER DEFAULT 0, " +
                "front_distance INTEGER DEFAULT 0, " +
                "back_distance INTEGER DEFAULT 0, " +
                "measure_time DATETIME DEFAULT CURRENT_TIMESTAMP);");

        db.execSQL("CREATE TABLE SafeScore ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + // foreign key to drive ID
                "safe_distance INTEGER DEFAULT 0, " +
                "speeding_count INTEGER DEFAULT 0, " +
                "fast_acc_count INTEGER DEFAULT 0, " +
                "fast_break_count INTEGER DEFAULT 0, " +
                "sudden_start_count INTEGER DEFAULT 0, " +
                "sudden_stop_count INTEGER DEFAULT 0 );");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("ModelStructure", oldVersion + " => " +newVersion);
        db.execSQL("DROP TABLE IF EXISTS DriveInfo");
        db.execSQL("DROP TABLE IF EXISTS SafeScore");
        onCreate(db);
    }
}
