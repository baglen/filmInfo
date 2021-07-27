package com.example.filmsearch.interfaces;

import com.example.filmsearch.objects.Film;

import java.util.List;

public interface DataLoad {
    void setFilms(List<Film> list);
    void processFinish(int page);
}
