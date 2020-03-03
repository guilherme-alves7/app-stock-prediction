package br.edu.ifsp.sbv.newcotacoes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifsp.sbv.newcotacoes.adapters.EmpresaListAdapter;
import br.edu.ifsp.sbv.newcotacoes.dao.EmpresaDAO;
import br.edu.ifsp.sbv.newcotacoes.model.Empresa;
import br.edu.ifsp.sbv.newcotacoes.webservice.CotacoesRequest;
import br.edu.ifsp.sbv.newcotacoes.webservice.JsonRequest;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ListView listViewEmpresas;
    private List<Empresa> empresas = new ArrayList<Empresa>();
    private EmpresaDAO dao;

    private ProgressDialog progressDialog;
    private AsyncTask updateThread;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //updateThread.cancel(true);

                AlertDialog.Builder mensagem = new AlertDialog.Builder(MainActivity.this);
                mensagem.setTitle("Adicionar Empresa");
                mensagem.setMessage("Digite o código da empresa");
                // DECLARACAO DO EDITTEXT
                final EditText input = new EditText(getApplicationContext());
                mensagem.setView(input);
                mensagem.setNegativeButton("Cancelar",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                mensagem.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        String codigo = input.getText().toString().trim().toUpperCase();

                        if(!codigo.equals("")) {
                            System.out.println("total = ");
                            System.out.println(dao.getByCodigo(codigo).size());
                            if (dao.getByCodigo(codigo).size() > 0) {
                                mostrarMensagem("Esta empresa já esta salva.");
                            } else {
                                Empresa empresa = new Empresa();
                                empresa.setSymbol(codigo);

                                new CotacoesRequest(MainActivity.this, codigo).execute();
                            }
                        }
                        else{
                            mostrarMensagem("Digite o código da empresa");
                        }
                    }

                });

                mensagem.show();
                // FORÇA O TECLADO APARECER AO ABRIR O ALERT
                /*InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);*/

            }
        });


        dao = new EmpresaDAO(getApplicationContext());

        listViewEmpresas = (ListView) findViewById(R.id.listViewEmpresas);

        //seta listeners da Listagem
        listViewEmpresas.setOnItemClickListener(onClick);
        listViewEmpresas.setOnItemLongClickListener(onLongClick);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if(empresas.size() > 0) {
                            if (listViewEmpresas.getChildAt(0).getTop() == 0) { //apenas habilita o swipe refresh quando a lista esta no topo
                                swipeRefreshLayout.setEnabled(true);
                            } else {
                                swipeRefreshLayout.setEnabled(false);
                            }
                        }
                    }
                });

        atualizarLista();
    }

    @Override
    public void onRefresh() {
        updateEmpresas();
    }

    //listeners Listagem
    private AdapterView.OnItemClickListener onClick = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long l) {
            //updateThread.cancel(true);

            Empresa empresa = empresas.get(position);
            Intent i = new Intent(getApplicationContext(), DetalhesActivity.class);
            i.putExtra("empresa", empresa );

            startActivityForResult(i, 1);
        }
    };

    private AdapterView.OnItemLongClickListener onLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
            final Empresa empresa = empresas.get(position);

            final AlertDialog.Builder mensagem = new AlertDialog.Builder(MainActivity.this);
            mensagem.setCancelable(true);
            mensagem.setTitle("Deletar empresa");
            mensagem.setMessage("Você realmente deseja apagar a empresa "+ empresa.getSymbol() +"?");
            mensagem.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    deletarEmpresa(empresa);
                }

            });
            mensagem.setNegativeButton("Não",  new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                }

            });

            mensagem.show();

            return true;
        }
    };


    private void deletarEmpresa(Empresa empresa){
        System.out.println("deletarEmpresa");

        if(dao.deletar(empresa.getId()) == true){
            mostrarMensagem("Empresa deletada com sucesso!");
        }
        else{
            mostrarMensagem("Erro ao deletar a empresa!");
        }

        atualizarLista();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void atualizarLista() {
        System.out.println("atualizarLista");
        empresas = dao.listAll();
        if (empresas.size() > 0) {

            EmpresaListAdapter empresaListAdapter = new EmpresaListAdapter(this, empresas);

            listViewEmpresas.setAdapter(empresaListAdapter);

        }
    }

    public void saveEmpresa(Empresa empresa){

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
            if (dao.atualizar(empresa)) {
                atualizarLista();
            }
        }
        else {
            if (dao.salvar(empresa)) {
                mostrarMensagem("Empresa salva com sucesso");
                atualizarLista();
            } else {
                mostrarMensagem("Erro ao salvar empresa");
            }
        }

        onResume();
    }

    public void addEmpresa(Empresa empresa){

        for(int i = 0; i < empresas.size(); i++){

            if( empresas.get(i).getSymbol().toLowerCase() == empresa.getSymbol().toLowerCase() ){
                empresa.setId( empresas.get(i).getId() );
                empresas.add( i, empresa );
            }

        }

    }

    public void showLoading(){
        progressDialog = new ProgressDialog(MainActivity.this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("retorno activity");

        if (resultCode == RESULT_OK && requestCode == 1) {
            System.out.println("atualiza");
            atualizarLista();
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

                runOnUiThread(new Runnable() {
                    public void run() {
                        mostrarMensagem("Dados atualizados");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {

            }

        }.execute();
    }
}
