package br.edu.ifsp.sbv.newcotacoes;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifsp.sbv.newcotacoes.dao.EmpresaDAO;
import br.edu.ifsp.sbv.newcotacoes.model.Empresa;
import br.edu.ifsp.sbv.newcotacoes.webservice.JsonRequest;

public class SplashActivity extends AppCompatActivity {

    private EmpresaDAO dao;
    private List<Empresa> empresas = new ArrayList<Empresa>();
    private AsyncTask updateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        dao = new EmpresaDAO(getApplicationContext());

        empresas = dao.listAll();
        if (empresas.size() > 0) {
            updateEmpresas();
        }
        else{
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showMainActivity();
                }
            }, 2000);
        }
    }

    private void updateEmpresas(){

        updateThread = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                System.out.println("--- executa update thread ---");

                for(int i = 0; i < empresas.size(); i++){
                    try {
                        String jsonString = JsonRequest.request( Utils.API_URL + empresas.get(i).getSymbol() +"/quote?token="+ Utils.API_TOKEN );

                        Gson gson = new Gson();
                        final Empresa emp = gson.fromJson(jsonString, Empresa.class);
                        emp.setId(empresas.get(i).getId());

                        runOnUiThread(new Runnable() {
                            public void run() {
                                saveEmpresa(emp);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                System.out.println("--- SplashScreen: finalizou update empresas ---");
                showMainActivity();
            }
        }.execute();
    }

    private void saveEmpresa(Empresa empresa){

        //apresentacao
        System.out.println( "id = " + empresa.getId() );
        System.out.println( "symbol = " + empresa.getSymbol() );
        System.out.println( "companyName = " + empresa.getCompanyName() );
        System.out.println( "latestPrice = " + empresa.getLatestPrice() );
        System.out.println( "changePercent = " + empresa.getChangePercent() );
        System.out.println( "change = " + empresa.getChange() );
        System.out.println( "latestVolume = " + empresa.getLatestVolume() );
        System.out.println( "ytdChange = " + empresa.getYtdChange() );
        System.out.println( "week52High = " + empresa.getWeek52High() );
        System.out.println( "week52Low = " + empresa.getWeek52Low() );
        System.out.println( "open = " + empresa.getOpen() );
        System.out.println( "latestTime = " + empresa.getLatestTime() );

        if(empresa.getId() > 0){
            dao.atualizar(empresa);
        }
        else {
            dao.salvar(empresa);
        }
    }

    private void showMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class );
        startActivity(intent);
        finish();
    }
}
