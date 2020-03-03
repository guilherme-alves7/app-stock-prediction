package br.edu.ifsp.sbv.newcotacoes.webservice;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;

import br.edu.ifsp.sbv.newcotacoes.DetalhesActivity;
import br.edu.ifsp.sbv.newcotacoes.MainActivity;
import br.edu.ifsp.sbv.newcotacoes.Utils;
import br.edu.ifsp.sbv.newcotacoes.model.Empresa;

/**
 * Created by guilherme on 03/12/17.
 */

public class DetalhesRequest extends AsyncTask<Void, Void, Empresa>{

    private WeakReference<DetalhesActivity> activity;

    private String codigo;

    public DetalhesRequest(DetalhesActivity activity, String codigo){
        this.activity = new WeakReference<>( activity );
        this.codigo = codigo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        activity.get().showLoading();
    }

    @Override
    protected Empresa doInBackground(Void... voids) {
        try {
            String jsonString = JsonRequest.request( Utils.API_URL + codigo +"/quote?token="+ Utils.API_TOKEN );

            Gson gson = new Gson();
            return gson.fromJson(jsonString, Empresa.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Empresa empresa) {
        super.onPostExecute(empresa);

        if (empresa != null) {
            activity.get().setCamposView(empresa);
        }
        else{
            activity.get().erroWS();
        }

        activity.get().hideLoading();
    }
}
