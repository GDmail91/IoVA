package org.multibluetooth.multibluetooth.SafeScore.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by YS on 2016-09-28.
 */
public class SafeScoreModel extends SQLiteOpenHelper {
    private static final String TAG = "SafeScoreModel";

    protected static final int DB_VERSION = 5;

    SQLiteDatabase dbR = getReadableDatabase();
    SQLiteDatabase dbW = getWritableDatabase();


    public SafeScoreModel(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);

        Log.d(TAG, "생성");
        db.execSQL("CREATE TABLE SafeScore ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + // foreign key to drive ID
                "safe_distance_count INTEGER DEFAULT 0, " +
                "speeding_count INTEGER DEFAULT 0, " +
                "fast_acc_count INTEGER DEFAULT 0, " +
                "fast_break_count INTEGER DEFAULT 0, " +
                "sudden_start_count INTEGER DEFAULT 0, " +
                "sudden_stop_count INTEGER DEFAULT 0 );");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, oldVersion + " => " +newVersion);
        db.execSQL("DROP TABLE IF EXISTS SafeScore");
        onCreate(db);
    }

    /** 삽입 SQL
     *
     * @param safeScore
     * @return topNumber
     */
    public int insert(SafeScore safeScore) {
        String sql = "INSERT INTO SafeScore (_id, safe_distance_count, speeding_count, fast_acc_count, fast_break_count, sudden_start_count, sudden_stop_count) " +
                "VALUES(" +
                "'" + safeScore.getDriveId() + "', " +
                "'" + safeScore.getSafeDistanceCount() + "', " +
                "'" + safeScore.getSpeedingCount() + "', " +
                "'" + safeScore.getFastAccCount() + "', " +
                "'" + safeScore.getFastBreakCount() + "', " +
                "'" + safeScore.getSuddenStartCount() + "', " +
                "'" + safeScore.getSuddenStopCount() + "') ;";


        // DB 작업 실행
        dbW.beginTransaction();
        try {
            dbW.execSQL(sql);
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction(); //트랜잭션을 끝내는 메소드.
        }
        return safeScore.getDriveId();
    }

    /** 수정 SQL
     *
     * @param safeScore
     * @return id
     */
    public int update(SafeScore safeScore) {
        String sql = "UPDATE SafeScore SET " +
                "speeding_count='" + safeScore.getSpeedingCount() + "', " +
                "fast_acc_count='" + safeScore.getFastAccCount() + "', " +
                "fast_break_count='" + safeScore.getFastBreakCount() + "'," +
                "sudden_start_count='" + safeScore.getSuddenStartCount() + "'," +
                "sudden_stop_count='" + safeScore.getSuddenStopCount() + "' " +
                "WHERE _id='"+ safeScore.getDriveId() +"' ;";

        // DB 작업 실행
        dbW.beginTransaction();
        try {
            dbW.execSQL(sql);
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction(); //트랜잭션을 끝내는 메소드.
        }

        return safeScore.getDriveId();
    }

    public int updateDistance(SafeScore safeScore) {
        String sql = "UPDATE SafeScore SET " +
                "safe_distance_count='" + safeScore.getSafeDistanceCount() + "' " +
                "WHERE _id='"+ safeScore.getDriveId() +"' ;";

        // DB 작업 실행
        dbW.beginTransaction();
        try {
            dbW.execSQL(sql);
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction(); //트랜잭션을 끝내는 메소드.
        }

        return safeScore.getDriveId();
    }

    public void update(String _query) {
        dbW.execSQL(_query);
    }

    public void delete(int ids) {
        dbW.beginTransaction();
        try {
            dbW.execSQL("DELETE FROM SafeScore WHERE _id='" + ids + "'");
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction();
        }
    }

    public void deleteAll() {
        dbW.beginTransaction();
        try {
            dbW.execSQL("DELETE FROM SafeScore");
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction();
        }
    }

    public int printCountOfData() {
        int count=0;

        Cursor cursor = dbR.rawQuery("SELECT * FROM SafeScore ORDER BY _id DESC", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                count += cursor.getInt(0);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return count;
    }

    public ArrayList<SafeScore> getAllData() {
        ArrayList<SafeScore> allData = new ArrayList<>();
        int i =0;
        Cursor cursor = dbR.rawQuery("SELECT * FROM SafeScore ORDER BY _id DESC", null);

        while(cursor.moveToNext()) {
            Log.d(TAG, ""+cursor.getInt(0));
            SafeScore tempData = new SafeScore(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6));

            allData.add(i++, tempData);
        }

        return allData;
    }

    public SafeScore getData(int id) {
        SafeScore data = null;

        Cursor cursor = dbR.rawQuery("SELECT * FROM SafeScore WHERE _id='"+id+"' ORDER BY _id DESC", null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            data = new SafeScore(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6)
            );
        }

        return data;
    }

    public void close() {
        dbR.close();
    }

}

