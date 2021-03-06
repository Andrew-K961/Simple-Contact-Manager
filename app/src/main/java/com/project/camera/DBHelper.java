package com.project.camera;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table people " +
                "(id integer primary key, cryptographic_id int, name varchar(32)," +
                " phone varchar(20), imagePath string)"
        );
        db.execSQL(
                "create table items " +
                "(id integer primary key, cryptographic_id int, name varchar(100)," +
                " description varchar(402), imagePath string, quantity integer, location int)"
        );
        db.execSQL("create table locations (id integer primary key, location string)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS people");
        db.execSQL("DROP TABLE IF EXISTS items");
        db.execSQL("DROP TABLE IF EXISTS locations");
        onCreate(db);
    }

//************ People

    public boolean insertPerson(int cg_id, String name, String phone, String imagePath){
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

    public boolean updatePerson(Integer id, String name, String phone, String imagePath){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("imagePath", imagePath);
        db.update("people", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        db.close();
        return true;
    }

    public void deletePerson(Integer id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("people", "id = ? ", new String[] { Integer.toString(id) });
        db.close();
    }

    public Cursor getPersonRow(int id){
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

    public ArrayList<Person> getAllPeople() {
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

    public ArrayList<Person> searchPeople(String query) {
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

//************ Items

    public boolean insertItem(int cg_id, String name, String desc, String imagePath, int quantity, int location){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("cryptographic_id", cg_id);
        contentValues.put("name", name);
        contentValues.put("description", desc);
        contentValues.put("imagePath", imagePath);
        contentValues.put("quantity", quantity);
        contentValues.put("location", location);
        db.insert("items", null, contentValues);
        db.close();
        return true;
    }

    public boolean updateItem(Integer id, String name, String desc, String imagePath, int quantity, int location){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("description", desc);
        contentValues.put("imagePath", imagePath);
        contentValues.put("quantity", quantity);
        contentValues.put("location", location);
        db.update("items", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        db.close();
        return true;
    }

    public void deleteItem(Integer id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("items", "id = ? ", new String[] { Integer.toString(id) });
        db.close();
    }

    public Cursor getItemRow(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] arg = {String.valueOf(id)};
        return db.rawQuery( "select id, cryptographic_id, name, description, imagePath, quantity, location " +
                "from items " +
                "where id=?", arg);
    }

    public ArrayList<Item> getAllItems() {
        ArrayList<Item> array_list = new ArrayList<>();
        String loc;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select id, name, quantity, location from items", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            if (res.getInt(3) != -1){
                loc = getLocation(res.getInt(3));
            } else {
                loc = "-1";
            }
            Item item = new Item(res.getInt(0), res.getString(1), res.getInt(2), loc);
            array_list.add(item);
            res.moveToNext();
        }
        res.close();
        db.close();
        return array_list;
    }

    public ArrayList<Item> searchItem(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String loc;
        String[] params = {"%" + query + "%"};
        Cursor result = db.rawQuery("select id, name, quantity, location from items where name like ?", params);
        result.moveToFirst();
        ArrayList<Item> array_list = new ArrayList<>();

        while(!result.isAfterLast()){
            if (result.getInt(3) != -1){
                loc = getLocation(result.getInt(3));
            } else {
                loc = "-1";
            }
            Item item = new Item(result.getInt(0), result.getString(1), result.getInt(2), loc);
            array_list.add(item);
            result.moveToNext();
        }
        result.close();
        db.close();
        return array_list;
    }

//************ Generic

    public boolean checkForCollision(int cryptoId, boolean item){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] arg = {String.valueOf(cryptoId)};
        String table = " people ";

        if (item){
            table = " items ";
        }

        Cursor search = db.rawQuery("SELECT cryptographic_id " +
                "FROM" + table +
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

    public int nfcSearch(int cryptoId, boolean item){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] arg = {String.valueOf(cryptoId)};
        int result;
        String table = " people ";

        if (item){
            table = " items ";
        }

        Cursor search = db.rawQuery("SELECT id FROM"+table+"WHERE cryptographic_id=?",arg);
        search.moveToFirst();
        if (search.getCount() > 0){
            result = search.getInt(0);
        } else {
            result = -1;
        }
        search.close();
        db.close();
        return result;
    }

    public int getCryptoId (int id, boolean item){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] arg = {String.valueOf(id)};
        String table = " people ";

        if (item){
            table = " items ";
        }

        Cursor search = db.rawQuery("SELECT cryptographic_id FROM"+table+"WHERE id=?",arg);
        search.moveToFirst();
        int result = search.getInt(0);
        search.close();
        db.close();
        return result;
    }

    public boolean checkLocationInUse (int Id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select id from items where location = ?", new String[] { String.valueOf(Id) });

        if (cursor.getCount() > 0){
            cursor.close();
            db.close();
            return true;
        } else {
            cursor.close();
            db.close();
            return false;
        }
    }

    //**************** Location

    public void insertLocation (String location){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("location", location);
        db.insert("locations", null, values);
        db.close();
    }

    public void updateLocation (int id, String location){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("location", location);
        db.update("locations", values, "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public void deleteLocation (int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("locations", "id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public ArrayList<Item> getAllLocations () {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Item> all = new ArrayList<>();

        Cursor cursor = db.rawQuery("select id, location from locations", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            all.add(new Item(cursor.getInt(0), cursor.getString(1), -1, "-1"));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return all;
    }

    public ArrayList<String> getLocationsString () {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> all = new ArrayList<>();

        Cursor cursor = db.rawQuery("select location from locations", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            all.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return all;
    }

    public int getIdFromLocation (String location){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select id from locations where location = ?", new String[] {location});
        cursor.moveToFirst();
        int result = cursor.getInt(0);

        cursor.close();
        db.close();
        return result;
    }

    public String getLocation (int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select location from locations where id = ?", new String[] { String.valueOf(id) });
        cursor.moveToFirst();
        String result = cursor.getString(0);

        cursor.close();
        db.close();
        return result;
    }
}