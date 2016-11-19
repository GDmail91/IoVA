package org.multibluetooth.multibluetooth.Driving.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by YS on 2016-09-20.
 */
public class DriveInfoModel extends SQLiteOpenHelper {
    private static final String TAG = "DriveInfoModel";

    protected static final int DB_VERSION = 16;
    public static final String DB_NAME = "DriveInfo.db";

    SQLiteDatabase dbR = getReadableDatabase();
    SQLiteDatabase dbW = getWritableDatabase();


    public DriveInfoModel(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);

        Log.d("MEMOMODEL", "생성");
        db.execSQL("CREATE TABLE DriveInfo ( " +
                "_id INTEGER NOT NULL, " +
                "drive_id INTEGER NOT NULL, " +
                "vehicle_speed INTEGER DEFAULT 0, " +
                "rpm INTEGER DEFAULT 0, " +
                "front_distance INTEGER DEFAULT 0, " +
                "back_distance INTEGER DEFAULT 0, " +
                "side_distance INTEGER DEFAULT 0, " +
                "gps_latitude REAL DEFAULT 0, " +
                "gps_longitude REAL DEFAULT 0, " +
                "measure_time TEXT, " +
                "PRIMARY KEY(_id, drive_id) );");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("MEMOMODEL", oldVersion + " => " +newVersion);
        db.execSQL("DROP TABLE IF EXISTS DriveInfo");
        onCreate(db);
    }

    /** 삽입 SQL
     *
     * @param driveInfo
     * @return topNumber
     */
    public int insert(DriveInfo driveInfo) {
        int topNumber = 0;

        Cursor cursor = dbR.rawQuery("SELECT _id FROM DriveInfo ORDER BY _id DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                topNumber = cursor.getInt(0);
            } while (cursor.moveToNext());
            cursor.close();
        }

        topNumber = topNumber+1;

        String sql = "INSERT INTO DriveInfo (_id, vehicle_speed, rpm, front_distance, back_distance) " +
                "VALUES(" +
                "'" + topNumber + "', " +
                "'" + driveInfo.getVehicleSpeed() + "', " +
                "'" + driveInfo.getRpm() + "', " +
                "'" + driveInfo.getFrontDistance() + "', " +
                "'" + driveInfo.getBackDistance() + "');";


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
        return topNumber;
    }

    //public int

    /** 삽입 SQL
     *
     * @return topNumber
     */
    public int createIndex(int topDriveNumber) {
        int topNumber = 0;

        Cursor cursor = dbR.rawQuery("SELECT _id FROM DriveInfo WHERE drive_id >= '"+topDriveNumber+"' ORDER BY _id DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                topNumber = cursor.getInt(0);
            } while (cursor.moveToNext());
            cursor.close();
        }

        topNumber = topNumber+1;

        String sql = "INSERT INTO DriveInfo (_id, drive_id, measure_time) " +
                "VALUES('"+ topNumber+"', '"+ topDriveNumber +"', '"+System.currentTimeMillis()+"');";


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
        return topNumber;
    }

    /** 읽기 SQL
     *
     * @return topNumber
     */
    public int getTopNumber() {
        int topDriveNumber = 0;

        Cursor cursor = dbR.rawQuery("SELECT drive_id FROM DriveInfo ORDER BY drive_id DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                topDriveNumber = cursor.getInt(0);
            } while (cursor.moveToNext());
            cursor.close();
        }
        topDriveNumber = topDriveNumber +1;

        return topDriveNumber;
    }

    /** 수정 SQL
     *
     * @param driveInfo
     * @return id
     */
    public int update(DriveInfo driveInfo) {
        String sql = "UPDATE DriveInfo SET " +
                "vehicle_speed='" + driveInfo.getVehicleSpeed() + "', " +
                "rpm='" + driveInfo.getRpm() + "', " +
                "front_distance='" + driveInfo.getFrontDistance() + "', " +
                "back_distance='" + driveInfo.getBackDistance() + "' " +
                "WHERE _id='"+driveInfo.getId()+"' ;";

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

        return driveInfo.getId();
    }

    /** 수정 SQL
     *
     * @param driveInfo
     * @return id
     */
    public int updateOBD(DriveInfo driveInfo) {
        String sql = "UPDATE DriveInfo SET " +
                "vehicle_speed='" + driveInfo.getVehicleSpeed() + "', " +
                "rpm='" + driveInfo.getRpm() + "' " +
                "WHERE _id='"+driveInfo.getId()+"' ;";

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

        return driveInfo.getId();
    }

    /** 수정 SQL
     *
     * @param driveInfo
     * @return id
     */
    public int updateLaser(DriveInfo driveInfo) {
        String sql = "UPDATE DriveInfo SET " +
                "front_distance='" + driveInfo.getFrontDistance() + "', " +
                "back_distance='" + driveInfo.getBackDistance() + "' " +
                "WHERE _id='"+driveInfo.getId()+"' ;";

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

        return driveInfo.getId();
    }

    public int updateFrontLaser(DriveInfo driveInfo) {
        String sql = "UPDATE DriveInfo SET " +
                "front_distance='" + driveInfo.getFrontDistance() + "' " +
                "WHERE _id='"+driveInfo.getId()+"' ;";

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

        return driveInfo.getId();
    }

    public void updateGps(int id, Location location) {
        String sql = "UPDATE DriveInfo SET " +
                "gps_latitude = '" + location.getLatitude() + "', " +
                "gps_longitude = '" + location.getLongitude() + "' " +
                "WHERE _id='"+ id +"' ;";

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
    }

    public void update(String _query) {
        dbW.execSQL(_query);
    }

    public void delete(int ids) {
        dbW.beginTransaction();
        try {
            dbW.execSQL("DELETE FROM DriveInfo WHERE _id='" + ids + "'");
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction();
        }
    }

    public void deleteByDriveIds(ArrayList<Integer> ids) {
        String idList = "";

        for (Integer id : ids) {
            idList += id + ",";
        }
        // 마지막 쉼표 짜름
        idList = idList.substring(0, idList.length()-1);
        Log.d(TAG, idList);

        dbW.beginTransaction();
        try {
            dbW.execSQL("DELETE FROM DriveInfo WHERE drive_id IN (" + idList + ")");
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
            dbW.execSQL("DELETE FROM DriveInfo");
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction();
        }
    }

    public int printCountOfData() {
        int count=0;

        Cursor cursor = dbR.rawQuery("SELECT * FROM DriveInfo ORDER BY _id DESC", null);
        while(cursor.moveToNext()) {
            count += cursor.getInt(0);
        }
        return count;
    }

    public ArrayList<DriveInfo> getAllData() {
        ArrayList<DriveInfo> allData = new ArrayList<>();
        int i =0;
        Cursor cursor = dbR.rawQuery("SELECT * FROM DriveInfo ORDER BY _id DESC", null);

        while(cursor.moveToNext()) {
            DriveInfo tempData = new DriveInfo(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getFloat(4),
                    cursor.getFloat(5),
                    cursor.getFloat(6),
                    cursor.getDouble(7),
                    cursor.getDouble(8),
                    cursor.getString(9));

            allData.add(i++, tempData);
        }

        return allData;
    }

    public DriveInfoList getAfterData(int id, int reqId) {
        DriveInfoList allData = new DriveInfoList();
        Cursor cursor = dbR.rawQuery("SELECT * FROM DriveInfo WHERE _id > '"+ reqId +"' AND drive_id >= '"+id+"' ORDER BY drive_id DESC, _id DESC", null);

        if (cursor.getCount() == 0) {
            cursor = dbR.rawQuery("SELECT * FROM DriveInfo WHERE drive_id > '"+id+"' ORDER BY drive_id DESC, _id DESC", null);
        }
        while(cursor.moveToNext()) {
            Log.d(TAG, cursor.getInt(0) +" "+cursor.getInt(1));
            DriveInfo tempData = new DriveInfo(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getFloat(4),
                    cursor.getFloat(5),
                    cursor.getFloat(6),
                    cursor.getDouble(7),
                    cursor.getDouble(8),
                    cursor.getString(9));

            allData.push(tempData);
        }

        return allData;
    }

    public DriveInfo getData(int id) {
        DriveInfo data = null;

        Cursor cursor = dbR.rawQuery("SELECT * FROM DriveInfo WHERE _id='"+id+"' ORDER BY _id DESC", null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getInt(2));

            data = new DriveInfo(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getFloat(4),
                    cursor.getFloat(5),
                    cursor.getFloat(6),
                    cursor.getDouble(7),
                    cursor.getDouble(8),
                    cursor.getString(9));
        }

        return data;
    }

    public void close() {
        dbR.close();
    }

    public int randomDataInsert() {
        int topNumber = 0;
        for (int i=0; i<20; i++) {

            Cursor cursor = dbR.rawQuery("SELECT _id FROM DriveInfo ORDER BY _id DESC LIMIT 1", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    topNumber = cursor.getInt(0);
                } while (cursor.moveToNext());
                cursor.close();
            }

            topNumber = topNumber + 1;

            String sql = "INSERT INTO DriveInfo (_id, vehicle_speed, front_distance, back_distance) " +
                    "VALUES(" +
                    "'" + topNumber + "', " +
                    "'60', " +
                    "'80', " +
                    "'60');";


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
        }
        return topNumber;
    }
}

