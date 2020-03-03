package br.edu.ifsp.sbv.newcotacoes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.internal.NavigationMenu;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

import br.edu.ifsp.sbv.newcotacoes.dao.EmpresaDAO;
import br.edu.ifsp.sbv.newcotacoes.model.Empresa;
import br.edu.ifsp.sbv.newcotacoes.webservice.CotacoesRequest;
import br.edu.ifsp.sbv.newcotacoes.webservice.DetalhesRequest;
import io.github.yavski.fabspeeddial.FabSpeedDial;

public class DetalhesActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private TextView symbol;
    private TextView companyName;
    private TextView latestPrice;
    private TextView changePercent;
    private TextView change;
    private TextView latestVolume;
    private TextView ytdChange;

    private TextView week52High;
    private TextView week52Low;
    private TextView open;
    private TextView latestTime;

    private FabSpeedDial fabSpeedDial;

    Empresa empresa;
    private EmpresaDAO dao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);

        //back button
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        empresa = (Empresa) intent.getSerializableExtra("empresa");

        symbol = (TextView) findViewById(R.id.txtSymbol);
        companyName = (TextView) findViewById(R.id.txtName);
        latestPrice = (TextView) findViewById(R.id.txtLatestPrice);
        changePercent = (TextView) findViewById(R.id.txtChangePercent);
        change = (TextView) findViewById(R.id.txtChange);
        latestVolume = (TextView) findViewById(R.id.txtLatestVolume);
        ytdChange = (TextView) findViewById(R.id.txtYtdChange);

        week52High = (TextView) findViewById(R.id.txtWeek52High);
        week52Low = (TextView) findViewById(R.id.txtweek52Low);
        open = (TextView) findViewById(R.id.txtOpen);
        latestTime = (TextView) findViewById(R.id.txtLatestTime);

        fabSpeedDial = (FabSpeedDial) findViewById(R.id.fabSpeedDial);
        fabSpeedDial.setMenuListener(menuListener);

        dao = new EmpresaDAO(getApplicationContext());

        new DetalhesRequest( DetalhesActivity.this, empresa.getSymbol() ).execute();
    }

    //function back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("empresa", empresa);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private FabSpeedDial.MenuListener menuListener = new FabSpeedDial.MenuListener(){

        @Override
        public boolean onPrepareMenu(NavigationMenu navigationMenu) {
            return true; //false dont show menu
        }

        @Override
        public boolean onMenuItemSelected(MenuItem menuItem) {

            if(menuItem.getTitleCondensed().equals("historico")){
                openHistorico();
            }
            else if(menuItem.getTitleCondensed().equals("previsao")){
                openPrediction();
            }
            else if(menuItem.getTitleCondensed().equals("deletar")){
                final AlertDialog.Builder mensagem = new AlertDialog.Builder(DetalhesActivity.this);
                mensagem.setCancelable(true);
                mensagem.setTitle("Deletar empresa");
                mensagem.setMessage("Você realmente deseja apagar a empresa "+ empresa.getSymbol() +"?");
                mensagem.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        deletar();
                    }

                });
                mensagem.setNegativeButton("Não",  new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                    }

                });

                mensagem.show();
            }
            return true;
        }

        @Override
        public void onMenuClosed() {

        }
    };

    public void setCamposView(Empresa empresa){

        empresa.setId(this.empresa.getId());
        this.empresa = empresa;
        dao.atualizar(this.empresa); //update empresa on database

        symbol.setText( empresa.getSymbol() );
        companyName.setText( empresa.getCompanyName() );
        latestPrice.setText( Utils.NUMBER_FORMAT.format(Double.valueOf(empresa.getLatestPrice())) );


        if( Double.valueOf(empresa.getChangePercent()) > 0 ){
            changePercent.setBackgroundResource(R.drawable.percent_change_pill_green);
        }
        else{
            changePercent.setBackgroundResource(R.drawable.percent_change_pill_red);
        }

        DecimalFormat df = new DecimalFormat("0.00");

        Double perc = Double.valueOf(empresa.getChangePercent()) * 100;
        changePercent.setTextColor(Color.WHITE);
        changePercent.setText( df.format( perc ) +"%" );

        if( Double.valueOf(empresa.getChange()) > 0 ){
            change.setBackgroundResource(R.drawable.percent_change_pill_green);
        }
        else{
            change.setBackgroundResource(R.drawable.percent_change_pill_red);
        }
        change.setTextColor(Color.WHITE );
        change.setText( Utils.NUMBER_FORMAT.format(Double.valueOf(empresa.getChange())) );


        latestVolume.setText( empresa.getLatestVolume() );
        perc = Double.valueOf(empresa.getYtdChange()) * 100;
        ytdChange.setText( df.format( perc ) +"%" );

        week52High.setText( Utils.NUMBER_FORMAT.format(Double.valueOf(empresa.getWeek52High())) );
        week52Low.setText( Utils.NUMBER_FORMAT.format(Double.valueOf(empresa.getWeek52Low())) );
        open.setText( Utils.NUMBER_FORMAT.format(Double.valueOf(empresa.getOpen())) );


        latestTime.setText( getDate(Long.parseLong(empresa.getLatestUpdate())) );
    }

    public void showLoading(){
        progressDialog = new ProgressDialog(DetalhesActivity.this);
        //progressDialog.setMessage("por favor aguarde..."); // Setting Message
        progressDialog.setTitle("Carregando"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
    }

    public void hideLoading(){
        progressDialog.dismiss();
    }

    public void mostrarMensagem(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void deletar() {
        EmpresaDAO dao = new EmpresaDAO(getApplicationContext());

        System.out.println("id = "+empresa.getId());

        if(dao.deletar(empresa.getId()) == true){
            mostrarMensagem("Empresa apagada com sucesso!");
        }
        else{
            mostrarMensagem("Erro ao deletar a empresa!");
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra("empresa", empresa);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    public void erroWS(){
        mostrarMensagem("Erro: Não foi possivel conectar ao serviço");
        finish();
    }

    public void openHistorico() {
        Intent i = new Intent(getApplicationContext(), HistoricoActivity.class);
        i.putExtra("empresa", empresa );
        startActivity(i);
    }

    public void openPrediction() {
        Intent i = new Intent(getApplicationContext(), PredictionActivity.class);
        i.putExtra("empresa", empresa );
        startActivity(i);
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd/MM/yyyy hh:mm:ss", cal).toString();
        return date;
    }
}
