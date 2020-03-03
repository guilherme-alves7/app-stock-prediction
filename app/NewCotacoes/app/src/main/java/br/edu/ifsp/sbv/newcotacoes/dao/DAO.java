package br.edu.ifsp.sbv.newcotacoes.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by guilherme on 30/09/17.
 */

public class DAO <T extends Object> extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sgcp.sqlite3";
    private static final int DATABASE_VERSION = 3;
    public static final String TABLE_EMPRESA = "empresa";

    protected Context context;
    protected String[] campos;
    protected String tableName;

    private static final String CREATE_TABLE_EMPRESA = "CREATE TABLE empresa ( "
            + " _id INTEGER PRIMARY KEY,"
            + " codigo VARCHAR(10),"
            + " latestPrice VARCHAR(50),"
            + " changePercent VARCHAR(50) );";

    private static final String INSERT_EMPRESA_1 = "INSERT INTO empresa (_id, codigo, latestPrice, changePercent) "
            + " VALUES (1, 'fb', '50.00', '5.00')";
    private static final String INSERT_EMPRESA_2 = "INSERT INTO empresa (_id, codigo, latestPrice, changePercent) "
            + " VALUES (2, 'msft', '25.00', '2.00')";

    public DAO(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        System.out.println("onCreate database");
        database.execSQL(CREATE_TABLE_EMPRESA);
        //database.execSQL(INSERT_EMPRESA_1);
        //database.execSQL(INSERT_EMPRESA_2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_EMPRESA);

        onCreate(db);
    }

    protected void closeDatabase(SQLiteDatabase db){
        if(db.isOpen()) {
            db.close();
        }
    }
}
