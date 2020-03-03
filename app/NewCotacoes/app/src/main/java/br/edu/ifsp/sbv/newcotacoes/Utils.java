package br.edu.ifsp.sbv.newcotacoes;

import java.text.NumberFormat;
import java.util.Locale;

public class Utils {

    public static String END_POINT_WS = "http://192.168.0.101:5000/prediction/get/api/iex/days";
    public static String[] EMPRESAS_TRAINED = {"AAPL", "FB", "GOOG", "MSFT", "TSLA", "PETR4"};

    public static String API_URL = "https://cloud.iexapis.com/stable/stock/";
    public static String API_TOKEN = "pk_d5b21f257bc045239f67819e432d7fbd";

    public static NumberFormat NUMBER_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
}
