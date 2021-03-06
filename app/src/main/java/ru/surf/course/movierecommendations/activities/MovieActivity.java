package ru.surf.course.movierecommendations.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.surf.course.movierecommendations.R;
import ru.surf.course.movierecommendations.Utilities;
import ru.surf.course.movierecommendations.adapters.MovieInfosPagerAdapter;
import ru.surf.course.movierecommendations.models.Genre;
import ru.surf.course.movierecommendations.models.Media;
import ru.surf.course.movierecommendations.models.MovieInfo;
import ru.surf.course.movierecommendations.tmdbTasks.GetMediaTask;
import ru.surf.course.movierecommendations.tmdbTasks.GetMoviesTask;
import ru.surf.course.movierecommendations.tmdbTasks.ImageLoader;

/**
 * Created by andrew on 2/15/17.
 */

public class MovieActivity extends AppCompatActivity {

    final static String KEY_MOVIE = "movie";
    final static String KEY_MOVIE_ID = "movie_id";
    final static int DATA_TO_LOAD = 1;
    final String LOG_TAG = getClass().getSimpleName();

    private ProgressBar progressBar;
    private TextView title;
    private TextView releaseDate;
    private MovieInfo currentMovie;
    private ImageView poster;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private View fakeStatusBar;
    private ViewPager infosPager;
    private TextView originalTitle;
    private Button backButton;

    private int dataLoaded = 0;

    public static void start(Context context, MovieInfo movieInfo) {
        Intent intent = new Intent(context, MovieActivity.class);
        intent.putExtra(KEY_MOVIE, movieInfo);
        context.startActivity(intent);
    }

    public static void start(Context context, int movieId) {
        Intent intent = new Intent(context, MovieActivity.class);
        intent.putExtra(KEY_MOVIE_ID, movieId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra(KEY_MOVIE) && !getIntent().hasExtra(KEY_MOVIE_ID))
            onDestroy();

        setContentView(R.layout.activity_movie);

        initViews();
        setupViews();
    }

    private void initViews() {
        progressBar = (ProgressBar)findViewById(R.id.movie_info_progress_bar);
        if (progressBar != null) {
            progressBar.setIndeterminate(true);
            progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        }
        title = (TextView)findViewById(R.id.movie_title);
        releaseDate = (TextView)findViewById(R.id.movie_release_date);
        poster = (ImageView) findViewById(R.id.movie_backdrop);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.movie_collapsing_toolbar_layout);
        appBarLayout = (AppBarLayout) findViewById(R.id.movie_appbar_layout);
        fakeStatusBar = findViewById(R.id.movie_fake_status_bar);
        infosPager = (ViewPager) findViewById(R.id.movie_info_infos_pager);
        originalTitle = (TextView)findViewById(R.id.movie_original_title);
        backButton = (Button)findViewById(R.id.movie_back_button);
    }

    private void setupViews() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                verticalOffset = Math.abs(verticalOffset);
                int offsetToToolbar = appBarLayout.getTotalScrollRange() - Utilities.getActionBarHeight(MovieActivity.this) - fakeStatusBar.getMeasuredHeight();
                if (verticalOffset >= offsetToToolbar) {
                    fakeStatusBar.setAlpha((float)(verticalOffset-offsetToToolbar)/Utilities.getActionBarHeight(MovieActivity.this));
                }
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (Utilities.isConnectionAvailable(this))
            startLoading();
        else {
            final View errorPlaceholder = findViewById(R.id.movie_no_internet_screen);
            errorPlaceholder.setVisibility(View.VISIBLE);
            ((Button)findViewById(R.id.message_no_internet_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utilities.isConnectionAvailable(MovieActivity.this)) {
                        errorPlaceholder.setVisibility(View.GONE);
                        startLoading();
                    }
                }
            });
        }
    }

    private void startLoading() {
        dataLoaded = 0;
        if (getIntent().hasExtra(KEY_MOVIE)) {
            currentMovie = (MovieInfo) getIntent().getSerializableExtra(KEY_MOVIE);
        } else if (getIntent().hasExtra(KEY_MOVIE_ID)) {
            currentMovie = new MovieInfo(getIntent().getIntExtra(KEY_MOVIE_ID, -1));
        }
        if (currentMovie != null) {
            loadInformationInto(currentMovie, getCurrentLocale().getLanguage());
        }

        if (Build.VERSION.SDK_INT >= 19) {
            fakeStatusBar.setVisibility(View.VISIBLE);
        }
    }

    private void loadInformationInto(final MovieInfo movie, String language) {
        GetMoviesTask getMoviesTask = new GetMoviesTask();
        getMoviesTask.addListener(new GetMediaTask.TaskCompletedListener() {
            @Override
            public void mediaLoaded(List result, boolean newResult) {
                if (movie != null)
                    movie.fillFields(result.get(0));
                dataLoadComplete();
            }
        });
        getMoviesTask.getMediaById(movie.getId(), language);
    }

    private boolean checkInformation(MovieInfo movie) {
        return Utilities.checkString(movie.getOverview());
        //for now checking only biography
    }

    private String firstLetterToUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    private void fillInformation() {
        title.setText(currentMovie.getTitle());

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);

        if (currentMovie.getDate() != null) {
            releaseDate.setText("(" + dateFormat.format(currentMovie.getDate()) + ")");
        }

        originalTitle.setText(currentMovie.getOriginalTitle());

        if (Utilities.checkString(currentMovie.getPosterPath())) {
            ImageLoader.putPosterNoResize(this, currentMovie.getPosterPath(), poster, ImageLoader.sizes.w500);
            poster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> image = new ArrayList<String>();
                    image.add(currentMovie.getPosterPath());
                    GalleryActivity.start(MovieActivity.this, image);
                }
            });
        }

        MovieInfosPagerAdapter movieInfosPagerAdapter = new MovieInfosPagerAdapter(getSupportFragmentManager(), this, currentMovie);
        infosPager.setAdapter(movieInfosPagerAdapter);
    }

    private void dataLoadComplete() {
        dataLoaded++;
        Log.v(LOG_TAG, "data loaded:" + dataLoaded);
        if (dataLoaded == DATA_TO_LOAD) {
            fillInformation();
            View progressBarPlaceholder = null;
                progressBarPlaceholder = findViewById(R.id.movie_progress_bar_placeholder);
            if (progressBarPlaceholder != null)
                progressBarPlaceholder.setVisibility(View.GONE);
        }
    }

    public void onGenreClick(Genre genre) {
        MainActivity.start(this, MainActivity.class, String.valueOf(genre.getId()), genre.getName(), true);
    }

    private Locale getCurrentLocale() {
        return Locale.getDefault();
    }

}
