package com.example.filmsearch.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filmsearch.objects.Film;
import com.page.filmsearch.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FilmsListAdapter extends RecyclerView.Adapter<FilmsListAdapter.ViewHolder> implements Filterable {
    private List<Film> moviesModelList;
    private List<Film> moviesModelListFiltered;
    private Context context;
    private SharedPreferences sPref;
    private LayoutInflater inflater;

    public FilmsListAdapter(Context context, List<Film> films)
    {
        this.context = context;
        this.moviesModelList = films;
        this.moviesModelListFiltered = films;
        this.inflater = LayoutInflater.from(context);
    }

    public FilmsListAdapter(Context context)
    {
        this.context = context;
        this.moviesModelList = new ArrayList<>();
        this.moviesModelListFiltered = new ArrayList<>();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView filmTitle;
        TextView filmDescription;
        TextView filmReleaseDate;
        ImageView filmImageView;
        ConstraintLayout filmLayout;
        ImageButton saveFilm;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            filmTitle =  itemView.findViewById(R.id.film_title);
            filmDescription =  itemView.findViewById(R.id.film_description);
            filmReleaseDate = itemView.findViewById(R.id.film_release_date);
            filmImageView = itemView.findViewById(R.id.film_image);
            saveFilm = itemView.findViewById(R.id.film_save);
            filmLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.list_item_film, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.filmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, moviesModelListFiltered.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.filmImageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0,0,view.getWidth(),view.getHeight()+25,12.5f);
            }
        });
        holder.filmImageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0,0,view.getWidth()+25,view.getHeight(),12.5f);
            }
        });
        holder.filmImageView.setClipToOutline(true);
        holder.filmTitle.setText(moviesModelListFiltered.get(position).getTitle());
        holder.filmDescription.setText(moviesModelListFiltered.get(position).getOverview());
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        try{
            Date a= sdf.parse(moviesModelListFiltered.get(position).getReleaseDate());
            SimpleDateFormat f = new SimpleDateFormat("d MMMM yyy");
            holder.filmReleaseDate.setText(f.format(a));
        } catch (Exception e) {
            holder.filmReleaseDate.setText("Неизвестно");
        }

        Picasso.with(context)
                .load("https://image.tmdb.org/t/p/w500/"+moviesModelListFiltered.get(position).getPosterPath())
                .into(holder.filmImageView);


        sPref = context.getSharedPreferences("savedFilms", Context.MODE_PRIVATE);
        if(sPref.contains(moviesModelListFiltered.get(position).getTitle()))
            holder.saveFilm.setImageResource(R.drawable.heart_checked);
        else
            holder.saveFilm.setImageResource(R.drawable.heart_unchecked);

        holder.saveFilm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor ed = sPref.edit();
                if(!sPref.contains(moviesModelListFiltered.get(position).getTitle())) {
                    holder.saveFilm.setImageResource(R.drawable.heart_checked);
                    ed.putString(moviesModelListFiltered.get(position).getTitle(), "savedFilm");
                }
                else
                {
                    holder.saveFilm.setImageResource(R.drawable.heart_unchecked);
                    ed.remove(moviesModelListFiltered.get(position).getTitle());
                }
                ed.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return moviesModelListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();
                if(constraint == null || constraint.length() == 0){
                    filterResults.count = moviesModelList.size();
                    filterResults.values = moviesModelList;

                }else{
                    List<Film> resultsModel = new ArrayList<>();
                    String searchStr = constraint.toString().toLowerCase();

                    for(Film movie:moviesModelList){
                        if(movie.getTitle().toLowerCase().contains(searchStr)){
                            resultsModel.add(movie);
                        }
                        filterResults.count = resultsModel.size();
                        filterResults.values = resultsModel;
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                moviesModelListFiltered = (List<Film>) results.values;
                notifyDataSetChanged();

            }
        };
        return filter;
    }

    public void addNewFilms(List<Film> list)
    {
        moviesModelListFiltered.addAll(list);
        notifyDataSetChanged();
    }
}
