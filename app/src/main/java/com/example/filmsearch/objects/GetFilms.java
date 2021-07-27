package com.example.filmsearch.objects;

import android.os.AsyncTask;
import com.example.filmsearch.interfaces.DataLoad;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetFilms extends AsyncTask<DataLoad, Void, Void>{
    private List<Film> filmsList = new ArrayList<>();
    private DataLoad dataLoad;
    private Integer page;

    public void setPage(Integer page)
    {
        this.page = page;
    }


    @Override
    protected Void doInBackground(DataLoad... params){
        if(params != null)
            dataLoad=params[0];
        try {
            String url = "https://api.themoviedb.org/3/discover/movie?api_key=6ccd72a2a8fc239b13f209408fc31c33&language=ru-RU&page="+ this.page;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
            Page page = gsonBuilder.create().fromJson(inputLine, Page.class);
            filmsList = page.getFilms();
        } catch (JsonParseException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void aVoid){
        dataLoad.setFilms(filmsList);
        dataLoad.processFinish(page);
    }
}

