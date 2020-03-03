package br.edu.ifsp.sbv.newcotacoes.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifsp.sbv.newcotacoes.model.Empresa;

/**
 * Created by guilherme on 30/09/17.
 */

public class EmpresaDAO extends DAO<Empresa>  {

    private SQLiteDatabase database;

    public EmpresaDAO(Context context) {
        super(context);
        campos = new String[]{"_id","codigo", "latestPrice", "changePercent"};
        tableName = "empresa";
        database = getWritableDatabase();
    }

    public List<Empresa> getByCodigo(String codigo){
        List<Empresa> list = new ArrayList<Empresa>();

        Cursor cursor = executeSelect("codigo = ?", new String[]{codigo}, null);

        if(cursor!=null && cursor.moveToFirst())
        {
            do{
                list.add(serializeByCursor(cursor));
            }while(cursor.moveToNext());


        }

        if(!cursor.isClosed())
        {
            cursor.close();
        }

        return list;
    }

    public Empresa getByID(Integer id) {
        Empresa empresa = null;

        Cursor cursor = executeSelect("_id = ?", new String[]{String.valueOf(id)}, null);

        if(cursor!=null && cursor.moveToFirst())
        {
            empresa = serializeByCursor(cursor);
        }
        if(!cursor.isClosed())
        {
            cursor.close();
        }

        return empresa;
    }

    public List<Empresa> listAll() {
        List<Empresa> list = new ArrayList<Empresa>();
        Cursor cursor = executeSelect(null, null, "1");


        if(cursor!=null && cursor.moveToFirst())
        {
            do{
                list.add(serializeByCursor(cursor));
            }while(cursor.moveToNext());


        }

        if(!cursor.isClosed())
        {
            cursor.close();
        }

        return list;
    }

    public boolean salvar(Empresa empresa) {
        ContentValues values = serializeContentValues(empresa);
        if(database.insert(tableName, null, values)>0)
            return true;
        else
            return false;
    }

    public boolean atualizar(Empresa empresa) {
        ContentValues values = serializeContentValues(empresa);
        if(database.update(tableName, values, "_id = ?", new String[]{String.valueOf(empresa.getId())})>0)
            return true;
        else
            return false;
    }

    public boolean atualizarByCodigo(Empresa empresa){
        ContentValues values = serializeContentValues(empresa);
        if(database.update(tableName, values, "codigo = ?", new String[]{empresa.getSymbol()})>0)
            return true;
        else
            return false;
    }

    public boolean deletar(Integer id) {
        if(database.delete(tableName, "_id = ?", new String[]{String.valueOf(id)})>0)
            return true;
        else
            return false;
    }

    private Empresa serializeByCursor(Cursor cursor){
        Empresa empresa = new Empresa();
        empresa.setId(cursor.getInt(0));
        empresa.setSymbol(cursor.getString(1));
        empresa.setLatestPrice(cursor.getString(2));
        empresa.setChangePercent(cursor.getString(3));

        return empresa;
    }

    private ContentValues serializeContentValues(Empresa empresa){
        ContentValues values = new ContentValues();

        if(empresa.getId() > 0) {
            values.put("_id", empresa.getId());
        }

        values.put("codigo", empresa.getSymbol());
        values.put("latestPrice", empresa.getLatestPrice());
        values.put("changePercent", empresa.getChangePercent());

        return values;
    }

    private Cursor executeSelect(String selection, String[] selectionArgs, String orderBy){
        return database.query(tableName, campos, selection, selectionArgs, null, null, orderBy);
    }
}
