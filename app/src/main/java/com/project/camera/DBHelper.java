package com.project.camera;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "MyDBName.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table people " +
                "(id integer primary key, cryptographic_id int, name varchar(255)," +
                " phone varchar(20), imagePath varchar(255))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS people");
        onCreate(db);
    }

    public boolean insert(int cg_id, String name, String phone, String imagePath){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("cryptographic_id", cg_id);
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("imagePath", imagePath);
        db.insert("people", null, contentValues);
        db.close();
        return true;
    }

    public boolean update(Integer id, String name, String phone, String imagePath){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("imagePath", imagePath);
        db.update("people", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        db.close();
        return true;
    }

    public Integer delete(Integer id){
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("people",
                "id = ? ",
                new String[] { Integer.toString(id) });
        db.close();
        return rows;
    }

    public Cursor getRow(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] arg = {String.valueOf(id)};
        return db.rawQuery( "select id, cryptographic_id, name, phone, imagePath " +
                "from people " +
                "where id=?", arg);
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, "people");
    }

    public boolean checkForCollision(int cryptoId){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] arg = {String.valueOf(cryptoId)};

        Cursor search = db.rawQuery("SELECT cryptographic_id " +
                "FROM people " +
                "WHERE cryptographic_id=?",arg);
        if(search.getCount() >= 1){
            search.close();
            db.close();
            return true;
        } else {
            search.close();
            db.close();
            return false;
        }
    }

    public int nfcSearch(int cryptoId){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] arg = {String.valueOf(cryptoId)};
        int result;

        Cursor search = db.rawQuery("SELECT id FROM people WHERE cryptographic_id=?",arg);
        search.moveToFirst();
        if (!search.isNull(0)){
            result = search.getInt(0);
        } else {
            result = -1;
        }
        search.close();
        db.close();
        return result;
    }

    public int getCryptoId (int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] arg = {String.valueOf(id)};

        Cursor search = db.rawQuery("SELECT cryptographic_id FROM people WHERE id=?",arg);
        search.moveToFirst();
        int result = search.getInt(0);
        search.close();
        db.close();
        return result;
    }

    public ArrayList<Person> getAll() {
        ArrayList<Person> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select id, name, phone from people", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            Person person = new Person(res.getInt(0), res.getString(1), res.getString(2));
            array_list.add(person);
            res.moveToNext();
        }
        res.close();
        db.close();
        return array_list;
    }

    public ArrayList<Person> search(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = {"%" + query + "%", "%" + query + "%"};
        Cursor result = db.rawQuery("select id, name, phone from people where name like ? or phone like ?", params);
        result.moveToFirst();
        ArrayList<Person> array_list = new ArrayList<>();
        while(!result.isAfterLast()){
            Person person = new Person(result.getInt(0), result.getString(1), result.getString(2));
            array_list.add(person);
            result.moveToNext();
        }
        result.close();
        db.close();
        return array_list;
    }
}