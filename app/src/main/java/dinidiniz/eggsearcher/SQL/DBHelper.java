package dinidiniz.eggsearcher.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by leon on 13/11/15.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DBEggSamples.db";
    public static final String SAMPLES_TABLE_NAME = "samples";
    public static final String SAMPLES_COLUMN_ID = "id";
    public static final String SAMPLES_COLUMN_CODE = "code";
    public static final String SAMPLES_COLUMN_EGGS = "eggs";
    public static final String SAMPLES_COLUMN_DESCRIPTION = "description";
    private HashMap hp;

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
}