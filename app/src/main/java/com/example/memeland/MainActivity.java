package com.example.memeland;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private String singleMemeURL = "https://meme-api.herokuapp.com/gimme";//Not Used
    private String URL;

    private int totalItemCount;
    private int firstVisibleItem;
    private int visibleItemCount;
    private int previousTotal;


    private final String[] memeLinks = {
            "https://www.reddit.com/r/wholesomememes/.json?limit=",
            "https://www.reddit.com/r/dankmemes/.json?limit=",
            "https://www.reddit.com/r/MemeEconomy/.json?limit=",
            "https://www.reddit.com/r/ProgrammerHumor/.json?limit=",
            "https://www.reddit.com/r/PewdiepieSubmissions/.json?limit=",
            "https://www.reddit.com//r/funny/.json?limit=",
            "https://www.reddit.com/r/memes/.json?limit=",
            "https://www.reddit.com/r/okbuddyretard/.json?limit=",
            "https://www.reddit.com/r/CricketShitpost/.json?limit=",
            "https://www.reddit.com/r/school_memes/.json?limit=",
            "https://www.reddit.com/r/programmingmemes/.json?limit=",
            "https://www.reddit.com/r/IndianMeyMeys/.json?limit=",
            "https://www.reddit.com/r/bollywoodmemes/.json?limit=",
            "https://www.reddit.com/r/AmongUsMemes/.json?limit=",
            "https://www.reddit.com/r/indiameme/.json?limit="
    };

    private int limit_memes_per_page = 60;
    private HashSet<Integer> past_URLS;

    private final String TAG = "meme";


    private int memeCount = 15;//number of memes in one page
    private int sweep = 0;//sweeps across all memes in a specific subreddit

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Row_item> RowList;
    String[] urls;

    private int total_elements;
    private TextView mLoading_Text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoading_Text = (TextView) findViewById(R.id.textView);

        mRecyclerView = findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(MainActivity.this);

        past_URLS = new HashSet<>();

        Add_Limit_to_URLS();
        RowList = new ArrayList<>();
        MemeLoader();
        mAdapter = new ItemAdapter(RowList, getBaseContext(), MainActivity.this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }

    private void MemeLoader() {
        try {
            sweep = 0;
            Random_URL();
            Log.e(TAG, URL);//Only for developer
            getMemes();
        } catch (Exception e) {
        }
    }

    private void getMemes() {
        try {
            HttpsTrustManager.allowAllSSL();
            final RequestQueue requestQueue = Volley.newRequestQueue(this);

            final JsonObjectRequest objectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    URL,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Log.d(TAG, "SUCCESS! JSON: " + response.toString());
                            MemeDataModel memeData = MemeDataModel.fromJSON(response);
                            if (memeData == null) {
                                Toast.makeText(MainActivity.this, "No Currently Available Memes", Toast.LENGTH_SHORT).show();
                            } else {
                                urls = memeData.getUrls();
                                url_to_RowList();
                                update_memes();

                                printArray(urls);//for developer

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Fail " + error.toString());
                            Toast.makeText(MainActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            requestQueue.add(objectRequest);
            pagination();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
        }

    }

    private void pagination() {

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    firstVisibleItem = mLayoutManager.getChildCount();
                    //Log.e(TAG, "FirstVisibleItem : " + firstVisibleItem);
                    totalItemCount = mLayoutManager.getItemCount();
                    //Log.e(TAG, "totalItemCount : " + totalItemCount);
                    firstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                    //Log.e(TAG, "firstVisibleItem : " + firstVisibleItem);


                    if (totalItemCount - firstVisibleItem <= 5)
                        nextPage();
                }
            }
        });
    }

    private void printArray(String[] urls) {

        for (int i = 0; i < urls.length; i++)
            if (urls[i] != null)
                Log.e(TAG, urls[i]);
    }

    private void url_to_RowList() {

        int actual_memes = 0;
        for (int i = 1; i <= memeCount; i++) {
            if (sweep == urls.length) {
                Log.e(TAG, "sweep == urls.length");
                break;
            }
            if (urls[sweep] != null) {
                RowList.add(new Row_item(urls[sweep]));
                actual_memes++;
            } else
                i--;
            sweep++;
        }
        total_elements += actual_memes;
    }

    private void update_memes() {
        //mAdapter.notifyItemRangeRemoved(0,total_elements-memeCount);
        mAdapter.notifyItemRangeInserted(total_elements - memeCount, total_elements);//better to use this than "notifyDataSetChanged"
        //mAdapter.notifyDataSetChanged();
        Log.e(TAG, "mAdapter.getItemCount() : " + mAdapter.getItemCount());
        // mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount()-memeCount+1);
        if (mLoading_Text.getVisibility() == View.VISIBLE)
            mLoading_Text.setVisibility(View.GONE);

    }

    public void nextPage() {
        if (past_URLS.size() > (memeLinks.length / 2)) {
            past_URLS.clear();
        }
        if (urls.length - sweep < memeCount)
            MemeLoader();
        else {
            url_to_RowList();
            Log.e(TAG, "Total elements : " + total_elements);
            update_memes();
        }
    }

    public void Random_URL() {
        Log.e(TAG, "" + memeLinks.length);
        int t = (int) (Math.random() * (memeLinks.length));

        if (past_URLS.contains(t))
            Random_URL();
        else {

            URL = memeLinks[t];
            past_URLS.add(t);
            Log.e(TAG, "" + t);
        }
    }

    private void Add_Limit_to_URLS() {
        for (int i = 0; i < memeLinks.length; i++) {
            memeLinks[i] = memeLinks[i] + limit_memes_per_page;
        }
    }

    @Override
    protected void onStop() {

        super.onStop();
        deleteCache(getApplicationContext());

    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}