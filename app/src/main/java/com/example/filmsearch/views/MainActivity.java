package com.example.filmsearch.views;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.filmsearch.adapters.FilmsListAdapter;
import com.example.filmsearch.interfaces.DataLoad;
import com.example.filmsearch.objects.Film;
import com.example.filmsearch.objects.GetFilms;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.page.filmsearch.R;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DataLoad {
    private FilmsListAdapter adapter;
    private RecyclerView filmList;
    private List<Film> currentFilms;
    private final GetFilms getFilms = new GetFilms();
    private LinearProgressIndicator linearProgressIndicator;
    private CircularProgressIndicator circularProgressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean loading = true;
    private int currentPage = 1;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filmList = findViewById(R.id.list_films);
        RelativeLayout listLayout = findViewById(R.id.list_layout);
        RelativeLayout noConnectionLayout = findViewById(R.id.no_connection_layout);
        TextView nothingFound = findViewById(R.id.text_view_nothing_found);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        RelativeLayout listNothingFound = findViewById(R.id.nothing_found_layout);
        circularProgressIndicator = findViewById(R.id.circular_indicator_main);
        EditText filmSearch = findViewById(R.id.search_film);

        if(!isOnline(this)) {
            Snackbar.make(this, findViewById(R.id.root_view), "Проверьте ваше соединение с интернетом и попробуйте ещё раз", Snackbar.LENGTH_LONG).show();
            listLayout.setVisibility(View.GONE);
            noConnectionLayout.setVisibility(View.VISIBLE);
        }
        else {
            getFilms.setPage(currentPage);
            getFilms.execute(this);
        }
            linearProgressIndicator = findViewById(R.id.linear_indicator);
            /*adapter = new FilmsListAdapter(this);
            filmList.setAdapter(adapter);*/
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            filmList.setLayoutManager(mLayoutManager);
            filmList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                        if (loading) {
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                loading = false;
                                if(MainActivity.isOnline(MainActivity.this)) {
                                    currentPage++;
                                    GetFilms getNextFilms = new GetFilms();
                                    getNextFilms.setPage(currentPage);
                                    linearProgressIndicator.show();
                                    getNextFilms.execute(MainActivity.this);
                                    loading = true;
                                }
                                else
                                {
                                    Snackbar.make(MainActivity.this, findViewById(R.id.root_view),
                                            "Проверьте ваше соединение с интернетом и попробуйте ещё раз",
                                            Snackbar.LENGTH_LONG).show();

                                }
                                loading = true;
                            }
                        }
                    }
                }
            });
            ImageButton refreshButton = new ImageButton(this);
            refreshButton.setImageResource(R.drawable.refresh);
            refreshButton.setBackgroundColor(Color.parseColor("#00000000"));
            filmSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(currentFilms != null & noConnectionLayout.getVisibility() != View.VISIBLE)
                        adapter.getFilter().filter(s);
                    if (filmList.getAdapter().getItemCount() == 0 &
                            noConnectionLayout.getVisibility() != View.VISIBLE && currentFilms != null) {
                        nothingFound.setText("По вашему запросу \"" + s + "\" ничего не найдено");
                        listLayout.setVisibility(View.GONE);
                        listNothingFound.setVisibility(View.VISIBLE);
                    } else if (s.length() == 0 & listNothingFound.getVisibility() == View.VISIBLE){
                        nothingFound.setText("");
                        listLayout.setVisibility(View.VISIBLE);
                        listNothingFound.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if(isOnline(MainActivity.this)) {
                        listLayout.setVisibility(View.VISIBLE);
                        noConnectionLayout.setVisibility(View.GONE);
                        if(currentFilms != null)
                        {
                            currentFilms.clear();
                            adapter.notifyDataSetChanged();
                        }

                        GetFilms refreshFilms = new GetFilms();
                        refreshFilms.setPage(1);
                        linearProgressIndicator.show();
                        refreshFilms.execute(MainActivity.this);
                    }
                    else
                    {
                        currentFilms.clear();
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(MainActivity.this, findViewById(R.id.root_view), "Проверьте ваше соединение с интернетом и попробуйте ещё раз", Snackbar.LENGTH_LONG).show();
                        listLayout.setVisibility(View.GONE);
                        noConnectionLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
    }

    @Override
    public void setFilms(List<Film> list) {
        circularProgressIndicator.hide();
        linearProgressIndicator.hide();
        swipeRefreshLayout.setRefreshing(false);
        if(currentFilms != null)
        {
            adapter.addNewFilms(list);
        }
        else
        {
            currentFilms = list;
            adapter = new FilmsListAdapter(MainActivity.this, list);
            filmList.setAdapter(adapter);
        }
    }

    @Override
    public void processFinish(int page) {
        if(page == 1)
            filmList.scrollToPosition(0);
    }

    public static boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting())
            return true;
        else
            return false;
    }
}
