package br.edu.ifsp.sbv.newcotacoes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import br.edu.ifsp.sbv.newcotacoes.model.Empresa;

public class PredictionActivity extends AppCompatActivity {

    private Empresa empresa;
    private ProgressDialog progressDialog;

    private Button btnPredict, btnRequestTraining;
    private TextView latestPrice, predictedPrice, labelPrediction, labelWarning, txtWarning;
    private RadioButton radio1d, radio7d, radio15d, radio30d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        //back button
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        empresa = (Empresa) intent.getSerializableExtra("empresa");

        latestPrice = (TextView) findViewById(R.id.txtLatestPrice);
        predictedPrice = (TextView) findViewById(R.id.txtPredictedPrice);
        labelPrediction = (TextView) findViewById(R.id.txtLabelPrediction);
        labelWarning = (TextView) findViewById(R.id.labelWarning);
        txtWarning = (TextView) findViewById(R.id.txtWarning);

        btnRequestTraining = (Button) findViewById(R.id.btnRequestTraining);
        btnPredict = (Button) findViewById(R.id.btnPredict);

        radio1d = (RadioButton) findViewById(R.id.radio1d);
        radio7d = (RadioButton) findViewById(R.id.radio7d);
        radio15d = (RadioButton) findViewById(R.id.radio15d);
        radio30d = (RadioButton) findViewById(R.id.radio30d);

        latestPrice.setText( Utils.NUMBER_FORMAT.format(Double.valueOf(empresa.getLatestPrice())) );

        setTitle("Previsão "+ empresa.getSymbol());

        predictIsAvailable();
    }

    //function back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void predictIsAvailable(){

        if(Arrays.asList(Utils.EMPRESAS_TRAINED).contains(empresa.getSymbol())) {
            btnPredict.setEnabled(true);
            radio1d.setEnabled(true);
            radio7d.setEnabled(true);
            radio15d.setEnabled(true);
            radio30d.setEnabled(true);

            labelPrediction.setVisibility(View.INVISIBLE);
            labelWarning.setVisibility(View.INVISIBLE);
            txtWarning.setVisibility(View.INVISIBLE);
            btnRequestTraining.setVisibility(View.INVISIBLE);
        }
        else {
            btnPredict.setEnabled(false);
            radio1d.setEnabled(false);
            radio7d.setEnabled(false);
            radio15d.setEnabled(false);
            radio30d.setEnabled(false);

            labelPrediction.setVisibility(View.INVISIBLE);
            labelWarning.setVisibility(View.VISIBLE);
            txtWarning.setVisibility(View.VISIBLE);
            btnRequestTraining.setVisibility(View.VISIBLE);
        }
    }

    public void predictStock(View view) {
        System.out.println("--- PREDICT STOCK ---");

        labelPrediction.setVisibility(View.INVISIBLE);
        predictedPrice.setText("");

        if(radio1d.isChecked()){
            System.out.println("1 dia");
            new GetPrediction().execute(Utils.END_POINT_WS+"?days=1&symbol="+empresa.getSymbol());
        }
        else if(radio7d.isChecked()){
            System.out.println("7 dias");
            new GetPrediction().execute(Utils.END_POINT_WS+"?days=7&symbol="+empresa.getSymbol());
        }
        else if(radio15d.isChecked()){
            System.out.println("15 dias");
            new GetPrediction().execute(Utils.END_POINT_WS+"?days=15&symbol="+empresa.getSymbol());
        }
        else if(radio30d.isChecked()){
            System.out.println("30 dias");
            new GetPrediction().execute(Utils.END_POINT_WS+"?days=30&symbol="+empresa.getSymbol());
        }
        else{
            Toast.makeText(this, "Selecione os dias para fazer a previsão", Toast.LENGTH_SHORT).show();
        }
    }

    public void setPrediction(String json){

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(json);

            Boolean success = jsonObj.getBoolean("success");
            String msg = jsonObj.getString("msg");
            String predicted = jsonObj.getString("predicted");
            //Double current = jsonObj.getDouble("current");

            if(success){
                labelPrediction.setVisibility(View.VISIBLE);

                if(!predicted.equals(null)) {
                    predictedPrice.setText( Utils.NUMBER_FORMAT.format(Double.valueOf(predicted)) );
                }
            }

            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro na leitura do WebService", Toast.LENGTH_SHORT).show();
        }
    }

    public void requestTraining(View view) {
        progressDialog = new ProgressDialog(PredictionActivity.this);
        progressDialog.setMessage("Solicitando treinamento");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Pedido realizado. Em breve a previsão estará disponivel para essa empresa.", Toast.LENGTH_LONG).show();
            }
        }, 5000);
    }

    private class GetPrediction extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(PredictionActivity.this);
            progressDialog.setMessage("Carregando");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if(result != null){
                setPrediction(result);
            }
            else{
                Toast.makeText(getApplicationContext(), "Erro: Não foi possivel conectar ao WebService", Toast.LENGTH_SHORT).show();
            }

            Log.i("onPostExecute", "onPostExecute");
            System.out.println(result);                    
        }
    }
}
