package paleta.aplicaciones.usarautocompletargoogleplaces;

/**
 * Created by renesotolira on 08/11/15.
 */



import java.io.IOException;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.net.URLEncoder;
        import java.util.ArrayList;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import android.app.Activity;
        import android.content.Context;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.AdapterView.OnItemClickListener;
        import android.widget.ArrayAdapter;
        import android.widget.AutoCompleteTextView;
        import android.widget.Filter;
        import android.widget.Filterable;
        import android.widget.Toast;



public class GooglePlacesAutoCompletarActivity extends Activity implements OnItemClickListener {

    private static final String TAG = "TAG";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TIPO_AUTOCOMPLETADO = "/autocomplete";
    private static final String FORMATO_ENVIO = "/json";

    private static final String API_KEY = "AIzaSyB5IFRrtueth7Ycsz1qmRro3gIzMQBJUyw"; //tu api key del tipo servidor

    /*
        LA SOLICITUD DE ENVIO ES
         https://maps.googleapis.com/maps/api/place/autocomplete/json?key={key}&components=country:gr&input={input strings for autocomplete search}


GOOGLE_KEY: Tu api KEY de google
country: El país que deseas hacer búsqueda específica.
input: las letras o palabras que se usarán para la búsqueda del autocompletar.

     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        autoCompView.setAdapter(new AdaptadorAutocompletar(this, R.layout.listado_de_lugares));
        autoCompView.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public static ArrayList autocompletar(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TIPO_AUTOCOMPLETADO + FORMATO_ENVIO);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:mx");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            Log.d(TAG,"URL enviada fue "+ url);
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Eror JSON" + e.toString() );
        }

        return resultList;
    }


    /*clase de autocompletado Adaptador*/
    class AdaptadorAutocompletar extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public AdaptadorAutocompletar(Context context, int textViewResourceId) {
            super(context, textViewResourceId);//llamando contructor de clase padre ArrayAdapter
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {

            return resultList.get(index).toString();
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults resultadosDelFiltro = new FilterResults();
                    if (constraint != null) {
                        // Regresa los resultado del autocomplete
                        resultList = autocompletar(constraint.toString());

                        // Asgianr los datos al FilterResults
                        resultadosDelFiltro.values = resultList;
                        resultadosDelFiltro.count = resultList.size();
                    }
                    return resultadosDelFiltro;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}