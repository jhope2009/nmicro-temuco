package inf.uct.nmicro.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectWS extends AsyncTask{
    private LinearLayout animado;
    private Context context;
    private int byGetOrPost = 0;
    private TextView te1;
    private TextView te2;
    private TextView te3;

    //flag 0 means get and 1 means post.(By default it is get.)
    public ConnectWS(Context cont, TextView t1,TextView t2,TextView t3,int flag) {
        this.context = cont;
        this.byGetOrPost = flag;
        this.te1 = t1;
        this.te2 = t2;
        this.te3 = t3;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        StringBuffer sb = new StringBuffer("");
        if(byGetOrPost == 0){ //means by Get Method

            try{
                String username = "admin";
                String password = "admin";
                String link = "http://192.168.43.142/microenparaderoconid.php?cadena="+(String)objects[0];

                URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(response.getEntity().getContent()));


                String line="";

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();
            } catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }
        return sb.toString();
    }

    protected void onPreExecute(){
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        //aqui esta la respuesta del webservice
        Log.i("Respuesta del WS", o.toString());
        try{
            String diaArray[] = o.toString().split(",");
            te1.setText("" + ((Float.parseFloat(diaArray[0].substring(0, 3)) / 60) * 60) + " min");
            te2.setText("" + diaArray[0].substring(0, 4) + " km");
            te3.setText("micro " + diaArray[1]);
        }catch (Exception e){
            te1.setText("0 min");
            te2.setText("0 Km");
            te3.setText("");
        }
    }
}
