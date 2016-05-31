package dinidiniz.eggsearcher.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by leon on 13/11/15.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getName();


    public static final String DATABASE_NAME = "DBEggSamples.db";


    public static final String SAMPLES_TABLE_NAME = "samples";
    public static final String SAMPLES_COLUMN_ID = "id";
    public static final String SAMPLES_COLUMN_CODE = "code";
    public static final String SAMPLES_COLUMN_EGGS = "eggs";
    public static final String SAMPLES_COLUMN_DESCRIPTION = "description";

    public static final String CONTOURS_TABLE_NAME = "contours";
    public static final String CONTOURS_COLUMN_ID = "id";
    public static final String CONTOURS_COLUMN_CONVEX = "convex";
    public static final String CONTOURS_COLUMN_VERTICES = "vertices";
    public static final String CONTOURS_COLUMN_AREA = "area";
    public static final String CONTOURS_COLUMN_APROXPOLY = "aproxpoly";
    public static final String CONTOURS_COLUMN_ISEGG = "isegg";


    public static final String PIXEL_TABLE_NAME = "pixel";
    public static final String PIXEL_COLUMN_ID = "id";
    public static final String PIXEL_COLUMN_R = "r";
    public static final String PIXEL_COLUMN_G = "g";
    public static final String PIXEL_COLUMN_B = "b";
    public static final String PIXEL_COLUMN_ISEGG = "isegg";


    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // TODO Auto-generated method stub
        db.execSQL(
                "create table if not exists " + SAMPLES_TABLE_NAME + "(" + SAMPLES_COLUMN_ID
                + " integer primary key autoincrement, " + SAMPLES_COLUMN_CODE + " text, "
                + SAMPLES_COLUMN_EGGS + " integer not null, " + SAMPLES_COLUMN_DESCRIPTION + " text)"
        );

        db.execSQL(
                "create table if not exists " + CONTOURS_TABLE_NAME + "(" + CONTOURS_COLUMN_ID
                        + " integer primary key autoincrement, " + CONTOURS_COLUMN_CONVEX + " integer not null, "
                        + CONTOURS_COLUMN_VERTICES + " integer not null, " + CONTOURS_COLUMN_AREA
                        + " integer not null, " + CONTOURS_COLUMN_APROXPOLY + " integer not null, "
                        + CONTOURS_COLUMN_ISEGG + " integer not null)"
        );

        db.execSQL(
                "create table if not exists " + PIXEL_TABLE_NAME + "(" + PIXEL_COLUMN_ID
                        + " integer primary key autoincrement, "
                        + PIXEL_COLUMN_R + " integer not null, " + PIXEL_COLUMN_G
                        + " integer not null, " + PIXEL_COLUMN_B + " integer not null, "
                        + PIXEL_COLUMN_ISEGG + " integer not null)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS SAMPLES");
        onCreate(db);
    }

    public boolean insertSample  (String code, int eggs, String description)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("code", code);
        contentValues.put("eggs", eggs);
        contentValues.put("description", description);
        db.insert("SAMPLES", null, contentValues);
        db.close();
        return true;
    }

    public boolean insertContour  (int convex, int vertices, int area, int aproxPoly, int isegg)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTOURS_COLUMN_CONVEX, convex);
        contentValues.put(CONTOURS_COLUMN_VERTICES, vertices);
        contentValues.put(CONTOURS_COLUMN_AREA, area);
        contentValues.put(CONTOURS_COLUMN_ISEGG, isegg);
        contentValues.put(CONTOURS_COLUMN_APROXPOLY, aproxPoly);
        db.insert(CONTOURS_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public boolean insertPixel  (SQLiteDatabase db, int r, int g, int b, int isegg)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PIXEL_COLUMN_R, r);
        contentValues.put(PIXEL_COLUMN_G, g);
        contentValues.put(PIXEL_COLUMN_B, b);
        contentValues.put(PIXEL_COLUMN_ISEGG, isegg);
        db.insert(PIXEL_TABLE_NAME, null, contentValues);
        return true;
    }


    public boolean insertAllPixels(List<List<Integer>> listPixel){
        SQLiteDatabase db = this.getWritableDatabase();

        for (List<Integer> a : listPixel){
            insertPixel(db, a.get(0), a.get(1), a.get(2), a.get(3));
        }

        db.close();
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from SAMPLES where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, SAMPLES_TABLE_NAME);
        return numRows;
    }

    public boolean updateSample (Integer id, String code, int eggs, String description)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("code", code);
        contentValues.put("eggs", eggs);
        contentValues.put("description", description);
        db.update("SAMPLES", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteSample (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("SAMPLES",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<ArrayList> getAllSamples()
    {
        ArrayList<ArrayList> finalArrayList = new ArrayList<ArrayList>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from SAMPLES", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(res.getString(res.getColumnIndex(SAMPLES_COLUMN_ID)));
            arrayList.add(res.getString(res.getColumnIndex(SAMPLES_COLUMN_CODE)));
            arrayList.add(res.getString(res.getColumnIndex(SAMPLES_COLUMN_EGGS)));
            arrayList.add(res.getString(res.getColumnIndex(SAMPLES_COLUMN_DESCRIPTION)));

            finalArrayList.add(arrayList);
            res.moveToNext();
        }
        return finalArrayList;
    }

    public List<List<Integer>> getAllPixeis()
    {
        List<List<Integer>> finalArrayList = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + PIXEL_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            arrayList.add(res.getInt(res.getColumnIndex(PIXEL_COLUMN_R)));
            arrayList.add(res.getInt(res.getColumnIndex(PIXEL_COLUMN_G)));
            arrayList.add(res.getInt(res.getColumnIndex(PIXEL_COLUMN_B)));
            arrayList.add(res.getInt(res.getColumnIndex(PIXEL_COLUMN_ISEGG)));
            finalArrayList.add(arrayList);
            res.moveToNext();
        }

        res.close();
        return finalArrayList;
    }

    public List<List<Integer>> getAllContour()
    {
        List<List<Integer>> finalArrayList = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + CONTOURS_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            arrayList.add(res.getInt(res.getColumnIndex(CONTOURS_COLUMN_VERTICES)));
            arrayList.add(res.getInt(res.getColumnIndex(CONTOURS_COLUMN_AREA)));
            arrayList.add(res.getInt(res.getColumnIndex(CONTOURS_COLUMN_APROXPOLY)));
            arrayList.add(res.getInt(res.getColumnIndex(CONTOURS_COLUMN_ISEGG)));
            finalArrayList.add(arrayList);
            res.moveToNext();
        }

        res.close();
        return finalArrayList;
    }

}