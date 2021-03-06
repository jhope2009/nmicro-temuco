package inf.uct.nmicro.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import inf.uct.nmicro.model.Instruction;
import inf.uct.nmicro.model.Route;
import inf.uct.nmicro.model.Point;
import inf.uct.nmicro.model.Stop;
import inf.uct.nmicro.model.Travel;
import inf.uct.nmicro.sqlite.ITablesDB.Tables;
import inf.uct.nmicro.sqlite.ITablesDB.Routes;
import inf.uct.nmicro.sqlite.ITablesDB.Companies;
import inf.uct.nmicro.sqlite.ITablesDB.Points;
import inf.uct.nmicro.sqlite.ITablesDB.StopRoute;
import inf.uct.nmicro.sqlite.ITablesDB.Stops;
import inf.uct.nmicro.sqlite.ITablesDB.Travels;
import inf.uct.nmicro.sqlite.ITablesDB.TravelRoutes;
import inf.uct.nmicro.sqlite.ITablesDB.Instructions;
import inf.uct.nmicro.model.Company;

public class DataBaseHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/inf.uct.nmicro/databases/";
    private static String DB_NAME = "MicroDB.db";
    private SQLiteDatabase myDataBase;
    private final Context myContext;

   public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;

    }

    public void NoCheckCreateDataBase() throws IOException {

        try {
            this.getReadableDatabase();
            copyDataBase();
        } catch (IOException e) {
            throw new Error("Error copying database");
        }

    }

    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteCantOpenDatabaseException e) {
            throw new Error("database does't exist yet.");
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;

    }

    private void copyDataBase() throws IOException {
        System.out.println("Creating database");
        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
        System.out.println("Database Created");

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if (myDataBase != null)
            myDataBase.close();
        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public List<Route> findRoutesByCompany(int idCompany) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s=?", Routes.ID_ROUTE, Routes.NAME,
                Routes.ICON, Tables.ROUTE, Routes.ID_COMPANY);
        String[] selectionArgs = {Integer.toString(idCompany)};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        List<Route> routes = new ArrayList<Route>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            routes.add(new Route(cursor.getInt(0), cursor.getString(1), findStopByidRoute(0), findPointsByRoute(cursor.getInt(0)), cursor.getString(2)));
            cursor.moveToNext();
        }
        return routes;
    }

    /*
        Obtiene los puntos asociados a un recorrido
     */
    public List<Point> findPointsByRoute(int idRoute) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s=?", Points.ID_POINT,
                Points.LATITUDE, Points.LONGITUDE, Tables.POINT, Points.ID_ROUTE);
        String[] selectionArgs = {Integer.toString(idRoute)};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        List<Point> points = new ArrayList<Point>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            points.add(new Point(cursor.getInt(0), cursor.getDouble(1), cursor.getDouble(2)));
            cursor.moveToNext();
        }
        return points;
    }

    /*
        Obtiene todos las lineas (companies)
    */
    public List<Company> findCompanies() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s", Tables.COMPANY);
        Cursor cursor = db.rawQuery(sql, null);
        List<Company> companies = new ArrayList<Company>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            companies.add(new Company(cursor.getInt(0), cursor.getString(1), cursor.getString(2), findRoutesByCompany(cursor.getInt(0))));
            cursor.moveToNext();
        }
        return companies;
    }

    /*
        Obtiene todos las lineas (companies)
    */
    public List<Company> findCompaniesById(int idCompany) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s WHERE %s=?", Tables.COMPANY, Companies.ID_COMPANY);
        String[] selectionArgs = {Integer.toString(idCompany)};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        List<Company> companies = new ArrayList<Company>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            companies.add(new Company(cursor.getInt(0), cursor.getString(1), cursor.getString(2), findRoutesByCompany(cursor.getInt(0))));
            cursor.moveToNext();
        }
        return companies;
    }

    public ArrayList<Stop> findAllStops() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s", Tables.STOP);
        Cursor cursor = db.rawQuery(sql, null);
        ArrayList<Stop> stops = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            stops.add(new Stop(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3)));
            cursor.moveToNext();
        }
        return stops;
    }

    public Route findRouteById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql= String.format("SELECT * FROM %s WHERE %s=?",Tables.ROUTE, Routes.ID_ROUTE);
        String[] selectionargs = {Integer.toString(id)};
        Cursor cursor= db.rawQuery(sql, selectionargs);
        Route ruta= new Route(cursor.getInt(0),cursor.getString(2),findStopByidRoute(cursor.getInt(0)),findPointsByRoute(cursor.getInt(0)),cursor.getString(3));
    return ruta;
    }


    public List<Stop> findStopByidRoute(int idroute){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s INNER JOIN %s ON %s=%s WHERE %s=?",
                Tables.STOP_ROUTE,Tables.STOP, Tables.STOP_ROUTE+"."+StopRoute.ID_STOP, Tables.STOP+"."+
                        Stops.ID_STOP, StopRoute.ID_ROUTE );
        String[] selectionArgs = {Integer.toString(idroute)};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        ArrayList<Stop> stops = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            stops.add(new Stop(cursor.getInt(3), cursor.getString(4), cursor.getDouble(5), cursor.getDouble(6)));
            cursor.moveToNext();

        }
        return stops;

    }


    public List<Route> findRoutesByStop(int idStop) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s INNER JOIN %s ON %s = %s WHERE %s = ?",
                Tables.STOP_ROUTE, Tables.ROUTE, Tables.STOP_ROUTE+"."+StopRoute.ID_ROUTE, Tables.ROUTE+"."+
                        Routes.ID_ROUTE, StopRoute.ID_STOP);
        String[] selectionArgs = {Integer.toString(idStop)};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        List<Route> routes = new ArrayList<Route>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            routes.add(new Route(cursor.getInt(3), cursor.getString(5), findPointsByRoute(cursor.getInt(3)), cursor.getString(6)));
            cursor.moveToNext();
        }
        return routes;
    }

    public boolean removeAllTravels(){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return false;
        }
        db.delete(Tables.TRAVEL, null, null);
        db.delete(Tables.TRAVEL_ROUTE, null, null);
        db.delete(Tables.INSTRUCTION, null, null);
        return true;
    }

    public Travel findTravelById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql= String.format("select * from %s where %s = ?",Tables.TRAVEL, Travels.ID_TRAVEL);
        String[] selectionargs = {Integer.toString(id)};
        Cursor cursor= db.rawQuery(sql, selectionargs);
        Travel travel= new Travel(cursor.getInt(0), cursor.getString(1), findRoutesByTravel(cursor.getInt(0)),
                findStopById(cursor.getInt(3)), findStopById(cursor.getInt(4)), cursor.getInt(2), findAllInstruction());
        return travel;
    }

    public Stop findStopById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql= String.format("select * from %s where %s = ?",Tables.STOP, Stops.ID_STOP);
        String[] selectionargs = {Integer.toString(id)};
        Cursor cursor= db.rawQuery(sql, selectionargs);
        Stop stop = new Stop(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3));
        return stop;
    }

    public List<Route> findRoutesByTravel(int idTravel) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s INNER JOIN %s ON %s = %s WHERE %s = ?",
                Tables.TRAVEL_ROUTE, Tables.ROUTE, Tables.TRAVEL_ROUTE+"."+ TravelRoutes.ID_ROUTE, Tables.ROUTE+"."+
                        Routes.ID_ROUTE, TravelRoutes.ID_TRAVEL);
        String[] selectionArgs = {Integer.toString(idTravel)};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        List<Route> routes = new ArrayList<Route>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            routes.add(new Route(cursor.getInt(1), cursor.getString(4), findStopByidRoute(cursor.getInt(1)),
                    findPointsByRoute(cursor.getInt(1)), cursor.getString(5)));
            cursor.moveToNext();
        }
        return routes;
    }

    public List<Instruction> findAllInstruction() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s", Tables.INSTRUCTION);
        Cursor cursor = db.rawQuery(sql, null);
        List<Instruction> instructions = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            instructions.add(new Instruction(cursor.getString(0), findStopById(cursor.getInt(0))));
            cursor.moveToNext();
        }
        return instructions;
    }

    public boolean saveTravel(Travel travel) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return false;
        }
        //Creamos el registro a insertar como objeto ContentValues
        ContentValues nuevoRegistro = new ContentValues();
        nuevoRegistro.put(Travels.ID_TRAVEL, travel.getIdTravel());
        nuevoRegistro.put(Travels.NAME, travel.getname());
        nuevoRegistro.put(Travels.PRICE,travel.getPrice());
        nuevoRegistro.put(Travels.START_STOP,travel.getStartStop().getIdStop());
        nuevoRegistro.put(Travels.END_STOP,travel.getEndStop().getIdStop());

        //Insertamos el registro en la base de datos
        Long id = db.insert(Tables.TRAVEL, null, nuevoRegistro);

        if(id!=-1){
            for(Route r : travel.getRoutes()){
                saveTravelRoute(travel.getIdTravel(), r.getIdRoute());
            }
            if(saveInstruction(travel.getIdTravel(), travel.getInstructions())) return true;
            else return false;
        }else return false;
    }

    public boolean saveTravelRoute(int idTravel, int idRoute){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return false;
        }
        //Creamos el registro a insertar como objeto ContentValues
        ContentValues nuevoRegistro = new ContentValues();
        nuevoRegistro.put(TravelRoutes.ID_TRAVEL, idTravel);
        nuevoRegistro.put(TravelRoutes.ID_ROUTE,idRoute);

        //Insertamos el registro en la base de datos
        if(db.insert(Tables.TRAVEL_ROUTE, null, nuevoRegistro)!=-1) return true;
        else return false;
    }

    public boolean saveInstruction(int idTravel, List<Instruction> ins) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return false;
        }
        //Creamos el registro a insertar como objeto ContentValues
        for(Instruction i : ins){
            ContentValues nuevoRegistro = new ContentValues();
            nuevoRegistro.put(Instructions.ID_TRAVEL, idTravel);
            nuevoRegistro.put(Instructions.INDICATION, i.getIndication());
            if(i.getStop()!=null) nuevoRegistro.put(Instructions.STOP, i.getStop().getIdStop());
            db.insert(Tables.INSTRUCTION, null, nuevoRegistro);
        }
        return true;
    }



}