package dinidiniz.eggsearcher.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dinidiniz.eggsearcher.helper.Coordinates;

/**
 * Created by leon on 13/11/15.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "DBEggSamples.db";
    public static final String SAMPLES_TABLE_NAME = "samples";
    public static final String SAMPLES_COLUMN_ID = "id";
    public static final String SAMPLES_COLUMN_CODE = "code";
    public static final String SAMPLES_COLUMN_EGGS = "eggs";
    public static final String SAMPLES_COLUMN_CREATEDAT = "createdat";
    public static final String SAMPLES_COLUMN_DATEONFIELD = "dateonfield";
    public static final String SAMPLES_COLUMN_LAT = "latitude";
    public static final String SAMPLES_COLUMN_LNG = "longitude";
    public static final String SAMPLES_COLUMN_DESCRIPTION = "description";
    public static final String SAMPLES_COLUMN_TOTALAREA = "totalarea";
    public static final String SAMPLES_COLUMN_HEIGHT = "height";
    public static final String SAMPLES_COLUMN_RESOLUTIONWIDTH = "resolutionwidth";
    public static final String SAMPLES_COLUMN_RESOLUTIONHEIGHT = "resolutionheight";
    public static final String SAMPLES_COLUMN_AREABIGGER = "areabigger";
    public static final String SAMPLES_COLUMN_USERID = "userid";
    public static final String SAMPLES_COLUMN_USEREMAIL = "useremail";

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
    public static final String PIXEL_COLUMN_GRAYSCALE = "grayscale";
    public static final String PIXEL_COLUMN_MEANB = "meanb";
    public static final String PIXEL_COLUMN_ISEGG = "isegg";
    public static final String EGGS_IN_PIXEL_TABLE = "egg";
    public static final String OTHER_IN_PIXEL_TABLE = "other";
    private static final String TAG = DBHelper.class.getName();
    private Context context;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        StringBuilder areasStringBuilder = new StringBuilder();
        for(int thresholdArea=3; thresholdArea <= 30; thresholdArea +=3){
            areasStringBuilder.append(SAMPLES_COLUMN_AREABIGGER);
            areasStringBuilder.append(thresholdArea + " integer not null, ");
        }


        // TODO Auto-generated method stub
        db.execSQL(
                "create table if not exists " + SAMPLES_TABLE_NAME + "(" + SAMPLES_COLUMN_ID
                        + " integer primary key autoincrement, "
                        + SAMPLES_COLUMN_USERID+ " text, "
                        + SAMPLES_COLUMN_USEREMAIL + " text, "
                        + SAMPLES_COLUMN_CODE + " text, "
                        + SAMPLES_COLUMN_EGGS + " integer not null, "
                        + SAMPLES_COLUMN_RESOLUTIONWIDTH + " integer not null, "
                        + SAMPLES_COLUMN_RESOLUTIONHEIGHT + " integer not null, "
                        + SAMPLES_COLUMN_HEIGHT + " integer not null, "
                        + areasStringBuilder.toString()
                        + SAMPLES_COLUMN_DESCRIPTION + " text, "
                        + SAMPLES_COLUMN_DATEONFIELD + " datetime, " + SAMPLES_COLUMN_LAT
                        + " decimal(9,6), " + SAMPLES_COLUMN_LNG + " decimal(9,6), " + SAMPLES_COLUMN_TOTALAREA
                        + " integer not null, " + SAMPLES_COLUMN_CREATEDAT
                        + " datetime default current_timestamp)"
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
                        + PIXEL_COLUMN_GRAYSCALE + " integer not null, " + PIXEL_COLUMN_MEANB
                        + " integer not null, " + PIXEL_COLUMN_ISEGG + " integer not null)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + SAMPLES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CONTOURS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PIXEL_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertSample(String code, int eggs, String description, double lng, double lat,
                                long dateOnField, int totalarea, int resolutionHeight,
                                int resolutionWidth, int height, int[] areas, String userId, String userEmail) {

        Log.i(TAG, resolutionWidth + " " + resolutionHeight + "  "  + height);

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(SAMPLES_COLUMN_CODE, code);
            contentValues.put(SAMPLES_COLUMN_USERID, userId);
            contentValues.put(SAMPLES_COLUMN_USEREMAIL, userEmail);
            contentValues.put(SAMPLES_COLUMN_EGGS, eggs);
            contentValues.put(SAMPLES_COLUMN_DESCRIPTION, description);
            contentValues.put(SAMPLES_COLUMN_LAT, lat);
            contentValues.put(SAMPLES_COLUMN_LNG, lng);
            contentValues.put(SAMPLES_COLUMN_DATEONFIELD, dateOnField);
            contentValues.put(SAMPLES_COLUMN_TOTALAREA, totalarea);
            contentValues.put(SAMPLES_COLUMN_HEIGHT, height);
            contentValues.put(SAMPLES_COLUMN_RESOLUTIONHEIGHT, resolutionHeight);
            contentValues.put(SAMPLES_COLUMN_RESOLUTIONWIDTH, resolutionWidth);
            for(int thresholdArea=3; thresholdArea <= 30; thresholdArea +=3){
                String areasString = SAMPLES_COLUMN_AREABIGGER + thresholdArea;
                contentValues.put(areasString, areas[thresholdArea/3 - 1]);
            }

            db.insertOrThrow("SAMPLES", null, contentValues);
            db.close();
        } catch (SQLiteException e){
            Log.i(TAG, "Error in SQLite");
            this.onUpgrade(this.getReadableDatabase(), 1,1);
            this.insertSample(code, eggs, description, lng, lat, dateOnField, totalarea,
                    resolutionHeight, resolutionWidth, height, areas, userId, userEmail);
        }
        return true;
    }

    public boolean insertContour(int convex, int vertices, int area, int aproxPoly, int isegg) {
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

    public void deleteAllPixelsFromTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + tableName);
        db.close();
    }


    public boolean insertAllPixels(List<List<Integer>> listPixel) {
        SQLiteDatabase db = this.getWritableDatabase();


        for (List<Integer> a : listPixel) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PIXEL_COLUMN_R, a.get(0));
            contentValues.put(PIXEL_COLUMN_G, a.get(1));
            contentValues.put(PIXEL_COLUMN_B, a.get(2));
            contentValues.put(PIXEL_COLUMN_GRAYSCALE, a.get(3));
            contentValues.put(PIXEL_COLUMN_MEANB, a.get(4));
            contentValues.put(PIXEL_COLUMN_ISEGG, a.get(5));
            db.insert(PIXEL_TABLE_NAME, null, contentValues);
        }


        db.close();
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from SAMPLES where id=" + id + "", null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, SAMPLES_TABLE_NAME);
        return numRows;
    }

    public boolean updateSample(Integer id, String code, int eggs, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("code", code);
        contentValues.put("eggs", eggs);
        contentValues.put("description", description);
        db.update("SAMPLES", contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteSample(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("SAMPLES",
                "id = ? ",
                new String[]{Integer.toString(id)});
    }

    public ArrayList<ArrayList<String>> getAllSamples() {
        ArrayList<ArrayList<String>> finalArrayList = new ArrayList<ArrayList<String>>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from SAMPLES", null);
        res.moveToFirst();

        if (res.getCount() > 0) {
            while (res.isAfterLast() == false) {
                ArrayList<String> arrayList = new ArrayList<String>();
                arrayList.add(res.getString(res.getColumnIndex(SAMPLES_COLUMN_ID)));
                arrayList.add(res.getString(res.getColumnIndex(SAMPLES_COLUMN_CODE)));
                arrayList.add(res.getString(res.getColumnIndex(SAMPLES_COLUMN_EGGS)));
                arrayList.add(res.getString(res.getColumnIndex(SAMPLES_COLUMN_DESCRIPTION)));
                double lat = res.getDouble(res.getColumnIndex(SAMPLES_COLUMN_LAT));
                double lng = res.getDouble(res.getColumnIndex(SAMPLES_COLUMN_LNG));
                arrayList.add("latitude: " + lat + " ;longitude: " + lng);
                String dateString = new SimpleDateFormat("MM/dd/yyyy").format(res.getLong(res.getColumnIndex(SAMPLES_COLUMN_DATEONFIELD)));
                arrayList.add(dateString);

                finalArrayList.add(arrayList);
                res.moveToNext();
            }
        }

        res.close();
        db.close();
        return finalArrayList;
    }

    public HashMap<String, List<List<Integer>>> getAllPixeis() {
        List<List<Integer>> finalArrayListNotEgg = new ArrayList<>();
        List<List<Integer>> finalArrayListEgg = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + PIXEL_TABLE_NAME, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            arrayList.add(res.getInt(res.getColumnIndex(PIXEL_COLUMN_R)));
            arrayList.add(res.getInt(res.getColumnIndex(PIXEL_COLUMN_G)));
            arrayList.add(res.getInt(res.getColumnIndex(PIXEL_COLUMN_B)));
            arrayList.add(res.getInt(res.getColumnIndex(PIXEL_COLUMN_GRAYSCALE)));
            arrayList.add(res.getInt(res.getColumnIndex(PIXEL_COLUMN_MEANB)));
            if (res.getInt(res.getColumnIndex(PIXEL_COLUMN_ISEGG)) == 1) {
                finalArrayListEgg.add(arrayList);
            } else {
                finalArrayListNotEgg.add(arrayList);
            }
            res.moveToNext();
        }
        HashMap finalHashMap = new HashMap();
        finalHashMap.put(EGGS_IN_PIXEL_TABLE, finalArrayListEgg);
        finalHashMap.put(OTHER_IN_PIXEL_TABLE, finalArrayListNotEgg);

        res.close();
        db.close();
        return finalHashMap;
    }

    public int getNumberOfPixels() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + PIXEL_TABLE_NAME, null);
        res.moveToFirst();
        int n = 0;
        while (res.isAfterLast() == false) {
            n += 1;
        }

        res.close();
        return n;
    }

    public List<List<Integer>> getAllContour() {
        List<List<Integer>> finalArrayList = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + CONTOURS_TABLE_NAME, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
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

    public File exportDB() {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "csv_eggsearcher.csv");
        try {
            file.delete();
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT * FROM " + DBHelper.SAMPLES_TABLE_NAME, null);
            csvWrite.writeNext(curCSV.getColumnNames());
            int numberColumns = curCSV.getColumnCount();
            while (curCSV.moveToNext()) {
                //Which column you want to exprort
                ArrayList<String> listStr = new ArrayList<String>();
                for (int i = 0; i < numberColumns; i++) {
                    listStr.add(curCSV.getString(i));
                }
                String arrStr[] = new String[listStr.size()];
                arrStr = listStr.toArray(arrStr);
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }

        return file;
    }


}