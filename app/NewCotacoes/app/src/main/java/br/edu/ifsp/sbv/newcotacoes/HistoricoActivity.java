package br.edu.ifsp.sbv.newcotacoes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.edu.ifsp.sbv.newcotacoes.model.Cotacao;
import br.edu.ifsp.sbv.newcotacoes.model.Empresa;

public class HistoricoActivity extends AppCompatActivity {

    private Empresa empresa;
    private GraphView graph;
    private ProgressDialog progressDialog;
    private RadioButton radio1M, radio3M, radio6M, radio1Y;
    private Button btnFilterChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        //back button
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        empresa = (Empresa) intent.getSerializableExtra("empresa");

        setTitle("Histórico "+ empresa.getSymbol());

        btnFilterChart = (Button)  findViewById(R.id.btnFilterChart);
        btnFilterChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterChart();
            }
        });

        radio1M = (RadioButton) findViewById(R.id.radio1M);
        radio3M = (RadioButton) findViewById(R.id.radio3M);
        radio6M = (RadioButton) findViewById(R.id.radio6M);
        radio1Y = (RadioButton) findViewById(R.id.radio1Y);

        graph = (GraphView) findViewById(R.id.historicGraph);

        //settings from graph
        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScrollableY(true); // enables vertical scrolling
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling

        radio1M.setChecked(true); //is default value
        filterChart();
    }

    //function back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setValuesChart(String json){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        //parse string to obj
        Type listType = new TypeToken<ArrayList<Cotacao>>(){}.getType();
        List<Cotacao> cotacoesList = new Gson().fromJson(json, listType);

        List<DataPoint> dataPoints = new ArrayList<DataPoint>();

        //create all points of graph
        for(Cotacao cotacao : cotacoesList){
            try {
                cotacao.setDateTime( simpleDateFormat.parse(cotacao.getDate()) );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dataPoints.add( new DataPoint(cotacao.getDateTime(), cotacao.getClose()) );
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>( dataPoints.toArray(new DataPoint[dataPoints.size()]) );
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(getApplicationContext(), "Fechamento: "+ dataPoint.getY(), Toast.LENGTH_SHORT).show();
            }
        });

        graph.removeAllSeries();
        graph.addSeries(series);

        // set manual x bounds to have nice steps
        graph.getViewport().setMinX( cotacoesList.get(0).getDateTime().getTime() ); //set first date
        graph.getViewport().setMaxX( cotacoesList.get(cotacoesList.size()-1).getDateTime().getTime() ); //set last date
        graph.getViewport().setXAxisBoundsManual(true);

        // set manual Y bounds
        graph.getViewport().setMinY( cotacoesList.get(0).getClose() );
        graph.getViewport().setMaxY( cotacoesList.get(cotacoesList.size()-1).getClose() );
        //graph.getViewport().setYAxisBoundsManual(true);


        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        graph.getGridLabelRenderer().setNumVerticalLabels(5);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graph.getGridLabelRenderer().setHumanRounding(true); //fix too many labels in Y axis
    }

    public void filterChart() {

        if(radio1M.isChecked()){
            new GetValuesChart().execute(Utils.API_URL + empresa.getSymbol() +"/chart/1m?token="+ Utils.API_TOKEN);
        }
        else if(radio3M.isChecked()){
            new GetValuesChart().execute(Utils.API_URL + empresa.getSymbol() +"/chart/3m?token="+ Utils.API_TOKEN);
        }
        else if(radio6M.isChecked()){
            new GetValuesChart().execute(Utils.API_URL + empresa.getSymbol() +"/chart/6m?token="+ Utils.API_TOKEN);
        }
        else if(radio1Y.isChecked()){
            new GetValuesChart().execute(Utils.API_URL + empresa.getSymbol() +"/chart/1y?token="+ Utils.API_TOKEN);
        }
    }

    private class GetValuesChart extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(HistoricoActivity.this);
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
            Log.i("onPostExecute", "onPostExecute");
            if(progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if(result != null){
                setValuesChart(result);
            }
            else{
                Toast.makeText(getApplicationContext(), "Erro: Não foi possivel conectar ao WebService", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
