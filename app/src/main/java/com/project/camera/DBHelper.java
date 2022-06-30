package com.project.camera;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

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

    public boolean updateItem(int id, String name, String desc, String imagePath, int quantity, int location){
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

    public int numberOfItemRows(SQLiteDatabase db){
        return (int) DatabaseUtils.queryNumEntries(db, "items");
    }

    public List<List<Object>> getItemsUpload() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select id, name, description, quantity, location, cryptographic_id, imagePath from items",
                null);

        List<List<Object>> result = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++){
            result.add(new ArrayList<>());
            for (int j = 0; j <= 6; j++){
                String column;
                if (j == 4 && cursor.getInt(4) != -1){
                    column = getLocation(cursor.getInt(4));
                } else if (cursor.getString(j).equals("-1")){
                    column = "";
                } else {
                    column = cursor.getString(j);
                }
                result.get(i).add(column);
            }
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return result;
    }

    public void ReplaceAllItems (List<List<Object>> newList){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM items");

        for (int i = 0; i < newList.size(); i++){
            ContentValues values= new ContentValues();
            values.put("id", Integer.parseInt(newList.get(i).get(0).toString()));
            values.put("name", newList.get(i).get(1).toString());
            values.put("description", newList.get(i).get(2).toString());
            values.put("quantity", newList.get(i).get(3).equals("") ? -1 : Integer.parseInt(newList.get(i).get(3).toString()));
            values.put("location", newList.get(i).get(4).equals("") ? -1 : getIdFromLocation(newList.get(i).get(4).toString()));
            values.put("cryptographic_id", Integer.parseInt(newList.get(i).get(5).toString()));
            if (newList.get(i).size() > 6){
                values.put("imagePath", newList.get(i).get(6).toString());
            } else {
                values.put("imagePath", "");
            }
            db.insert("items", null, values);
        }
        db.close();
    }

    public int getLastId () {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM items", null);

        cursor.moveToFirst();
        int result = cursor.getInt(0);

        cursor.close();
        db.close();
        return result;
    }

    public List<List<Object>> getRowForUpload (int id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select name, description, quantity, location, cryptographic_id, imagePath from items where id = ?",
                new String[] { String.valueOf(id) });

        cursor.moveToFirst();
        List<List<Object>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (int i = 0; i < 6; i++){
            if (i == 3){
                result.get(0).add(getLocation(cursor.getInt(i)));
            } else {
                result.get(0).add(cursor.getString(i));
            }
        }

        cursor.close();
        db.close();
        return result;
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

    //**************** Location

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
        return result;
    }

    public String getLocation (int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        if (id == -1){
            return "";
        }

        Cursor cursor = db.rawQuery("select location from locations where id = ?", new String[] { String.valueOf(id) });
        cursor.moveToFirst();
        String result = cursor.getString(0);

        cursor.close();
        return result;
    }
}