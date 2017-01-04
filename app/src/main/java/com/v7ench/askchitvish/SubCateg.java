package com.v7ench.askchitvish;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubCateg extends AppCompatActivity {
     ListView subcat_list;
    private SQLiteHandler db;
    MaterialSearchView searchView;
    ProgressBar progressBar;
    TextView gourl;
    EditText searchme;
    ImageButton clickser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_categ);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#971627"));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        subcat_list=(ListView) findViewById(R.id.sub_cat);
gourl=(TextView) findViewById(R.id.anc);
        progressBar =(ProgressBar) findViewById(R.id.progressBar2);
        Intent details=getIntent();
      final String cid=details.getStringExtra("id");
        String catt_nam=details.getStringExtra("cat_name");
        setTitle(catt_nam);

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        final String uid = user.get("uid");
        final String url="http://gettalentsapp.com/askchitvish/androadmin/sub_catog.php?catg_id="+cid+"&uid="+uid;
        new JSONTask().execute(url);

        searchme=(EditText) findViewById(R.id.searchedit);
        clickser=(ImageButton) findViewById(R.id.imabutton);
        clickser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query=searchme.getText().toString();
                if (query.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Please enter a keyword",Toast.LENGTH_SHORT).show();
                }
else {
                    String url3 = "http://gettalentsapp.com/askchitvish/androadmin/pom_pom.php?catg_id=" + cid + "&uid=" + uid + "&pompom=" + query;
                    new JSONTask().execute(url3);

                }
            }
        });
    }

    public class JSONTask extends AsyncTask<String,String, List<Subcategory> > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Subcategory> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("result");
                List<Subcategory> movieModelList = new ArrayList<>();
                Gson gson = new Gson();
                for(int i=0; i<parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    if (finalObject.getString("active").contains("Y")) {
                        Subcategory subcategory = gson.fromJson(finalObject.toString(), Subcategory.class);
                       movieModelList.add(subcategory);

                   }

                }
                return movieModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return  null;

        }

        @Override
        protected void onPostExecute(final List<Subcategory> movieModelList) {
            super.onPostExecute(movieModelList);
            progressBar.setVisibility(View.INVISIBLE);
            if(movieModelList != null) {
                MovieAdapter adapter = new MovieAdapter(getApplicationContext(), R.layout.list_cubcate, movieModelList);
                subcat_list.setAdapter(adapter);
                adapter.notifyDataSetChanged();

               subcat_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                       Subcategory subcategory = movieModelList.get(position);
                        Intent intent = new Intent(SubCateg.this, DetailsActivity.class);
                        intent.putExtra("subcatdetails", new Gson().toJson(subcategory));
                        startActivity(intent);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Internet connection is too slow for process.Please wait", Toast.LENGTH_SHORT).show();
            }
        }

    }


    public class MovieAdapter extends ArrayAdapter {

        private List<Subcategory> movieModelList;
        private int resource;
        Context context;
        private LayoutInflater inflater;
        public MovieAdapter(Context context, int resource, List<Subcategory> objects) {
            super(context, resource, objects);
            movieModelList = objects;
            this.context =context;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        }
        @Override
        public int getViewTypeCount() {

            return 1;
        }

        @Override
        public int getItemViewType(int position) {

            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder  ;
            if(convertView == null){
                convertView = inflater.inflate(resource,null);
                holder = new ViewHolder();
                holder.topic = (TextView)convertView.findViewById(R.id.topic_n);
                holder.short_dec=(TextView) convertView.findViewById(R.id.short_d);
                holder.naming=(TextView) convertView.findViewById(R.id.namei);
//                holder.ima=(ImageView) convertView.findViewById(R.id.imavi);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            Subcategory subcategory= movieModelList.get(position);
            holder.topic.setText(subcategory.getTopic_name());
            holder.short_dec.setText(subcategory.getShort_desc());
            holder.naming.setText(subcategory.getName());
//            Picasso.with(context).load(subcategory.getImages()).fit().error(R.drawable.load).fit().into(holder.ima);
            return convertView;

        }

        class ViewHolder{
            private ImageView ima;
            private TextView topic,short_dec,naming;


        }


    }
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.kikama, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }*/
}
