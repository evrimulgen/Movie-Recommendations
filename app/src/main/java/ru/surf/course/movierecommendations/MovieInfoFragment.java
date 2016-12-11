package ru.surf.course.movierecommendations;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apmem.tools.layouts.FlowLayout;

import java.util.Calendar;
import java.util.List;
import java.util.zip.Inflater;

import ru.surf.course.movierecommendations.tmdbTasks.GetMoviesTask;
import ru.surf.course.movierecommendations.tmdbTasks.ImageLoader;

/**
 * Created by andrew on 11/26/16.
 */

public class MovieInfoFragment extends Fragment implements GetMoviesTask.TaskCompletedListener{

    final static String KEY_MOVIE = "movie";
    final static String KEY_MOVIE_ID = "moveId";
    final static int DATA_TO_LOAD = 2;

    private TextView title;
    private TextView overview;
    private TextView releaseYear;
    private ImageView poster;
    private MovieInfo currentMovie;
    private FlowLayout genresPlaceholder;

    private Bitmap posterBitmap;
    private int dataLoaded = 0;

    public static MovieInfoFragment newInstance(MovieInfo movieInfo) {
        MovieInfoFragment movieInfoFragment = new MovieInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_MOVIE, movieInfo);
        movieInfoFragment.setArguments(bundle);
        return movieInfoFragment;
    }

    public static MovieInfoFragment newInstance(int movieId) {
        MovieInfoFragment movieInfoFragment = new MovieInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_MOVIE_ID, movieId);
        movieInfoFragment.setArguments(bundle);
        return movieInfoFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() == null)
            onDestroy();
        View root = inflater.inflate(R.layout.fragment_movie_info, container, false);
        title = (TextView)root.findViewById(R.id.movie_info_name);
        poster = (ImageView)root.findViewById(R.id.movie_info_poster);
        overview = (TextView)root.findViewById(R.id.movie_info_overview);
        releaseYear = (TextView)root.findViewById(R.id.movie_info_release_year);
        genresPlaceholder = (FlowLayout) root.findViewById(R.id.movie_info_genres_placeholder);

        poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBigPoster();
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getArguments().containsKey(KEY_MOVIE)) {
            currentMovie = (MovieInfo) getArguments().getSerializable(KEY_MOVIE);
            dataLoaded = 0;
            loadInformation(currentMovie.id, "en");
            loadPoster();
        }
        else if (getArguments().containsKey(KEY_MOVIE_ID)){
            dataLoaded = 0;
            loadInformation(getArguments().getInt(KEY_MOVIE_ID), "en");
        }
    }

    public void showBigPoster() {
        //TODO make image popup somehow
    }


    public void loadInformation(int movieId, String language) {
        GetMoviesTask getMoviesTask = new GetMoviesTask();
        getMoviesTask.addListener(this);
        getMoviesTask.getMovieInfo(movieId, language);


    }

    public void loadPoster(){
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                posterBitmap = bitmap;
                dataLoadComplete();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                //TODO handle error
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        ImageLoader.getPoster(getActivity(), currentMovie.posterPath, poster.getLayoutParams().width, poster.getLayoutParams().height, target);
    }

    public void fillInformation() {
        poster.setImageBitmap(posterBitmap);
        title.setText(currentMovie.title);
        overview.setText(currentMovie.overview);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentMovie.date);
        releaseYear.setText("(" + calendar.get(Calendar.YEAR) + ")");

        for (String genreName : currentMovie.genreNames) {
            Button genreButton = (Button)getActivity().getLayoutInflater().inflate(R.layout.genre_btn_template, null);
            genreButton.setText(genreName);
            genresPlaceholder.addView(genreButton);
            FlowLayout.LayoutParams layoutParams = (FlowLayout.LayoutParams)genreButton.getLayoutParams();
            layoutParams.setMargins(0,0,(int)getResources().getDimension(R.dimen.genre_button_margin_right),(int)getResources().getDimension(R.dimen.genre_button_margin_bottom));
            genreButton.setLayoutParams(layoutParams);
        }

    }

    @Override
    public void taskCompleted(List<MovieInfo> result) {
        currentMovie = result.get(0);
        dataLoadComplete();
        loadPoster();
    }

    public void dataLoadComplete(){
        if (++dataLoaded == DATA_TO_LOAD) {
            fillInformation();
            View progressBarPlaceholder = null;
            if (getView() != null)
                progressBarPlaceholder = getView().findViewById(R.id.movie_info_progress_bar_placeholder);
            if (progressBarPlaceholder != null)
                progressBarPlaceholder.setVisibility(View.GONE);
        }
    }
}
