package br.edu.ifsp.sbv.newcotacoes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import br.edu.ifsp.sbv.newcotacoes.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import br.edu.ifsp.sbv.newcotacoes.Utils;
import br.edu.ifsp.sbv.newcotacoes.model.Empresa;
import br.edu.ifsp.sbv.newcotacoes.webservice.JsonRequest;

/**
 * Created by guilherme on 30/09/17.
 */

public class EmpresaListAdapter extends BaseAdapter {

    private Context context;
    private List<Empresa> lista;

    public EmpresaListAdapter(Context context, List<Empresa> lista) {
        this.context = context;
        this.lista = lista;
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Empresa empresa = lista.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_item_quote, null);

        TextView symbol = (TextView) view.findViewById(R.id.stock_symbol);
        TextView price = (TextView) view.findViewById(R.id.bid_price);
        TextView change = (TextView) view.findViewById(R.id.change);

        symbol.setText( empresa.getSymbol().toUpperCase() );

        if(!empresa.getLatestPrice().equals("") && empresa.getLatestPrice() != null) {
            price.setText(Utils.NUMBER_FORMAT.format(Double.valueOf(empresa.getLatestPrice())));
        }

        DecimalFormat df = new DecimalFormat("0.00");

        Double perc = Double.valueOf(empresa.getChangePercent()) * 100;
        change.setText( df.format( perc ) + "%" );

        if(Double.valueOf(empresa.getChangePercent()) > 0){
            change.setBackgroundResource(R.drawable.percent_change_pill_green);
        }
        else{
            change.setBackgroundResource(R.drawable.percent_change_pill_red);
        }

        return view;
    }
}
