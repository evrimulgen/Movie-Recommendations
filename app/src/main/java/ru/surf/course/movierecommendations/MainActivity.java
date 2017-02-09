package ru.surf.course.movierecommendations;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Locale;

import ru.surf.course.movierecommendations.adapters.ContentFragmentPagerAdapter;
import ru.surf.course.movierecommendations.fragments.MoviesListFragment;
import ru.surf.course.movierecommendations.tmdbTasks.Filters;
import ru.surf.course.movierecommendations.tmdbTasks.Tasks;

public class MainActivity extends AppCompatActivity {


    public static final String TAG_MOVIES_LIST_FRAGMENT = "movie_list_fragment";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private MoviesListFragment moviesListFragment;

    public static void start(Context context, Class c) {
        Intent intent = new Intent(context, c);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.popular);
        NotSwipeableViewPager viewPager = (NotSwipeableViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new ContentFragmentPagerAdapter(getSupportFragmentManager(), this, Filters.popular));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);


//        if (isInternetAvailable(this)) {
//            loadMainFragment(savedInstanceState, Filters.popular.toString());
//        } else {
//            final TextView error = (TextView) findViewById(R.id.activity_main_text_internet_error);
//            error.setVisibility(View.VISIBLE);
//            final Button button = (Button) findViewById(R.id.activity_main_btn_reload);
//            button.setVisibility(View.VISIBLE);
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (isInternetAvailable(MainActivity.this)) {
//                        error.setVisibility(View.GONE);
//                        button.setVisibility(View.GONE);
//                        loadMainFragment(savedInstanceState, Filters.popular.toString());
//                    }
//                }
//            });
//        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isInternetAvailable(MainActivity.this)) {
                    searchByName(query.replace(' ', '+'));
                }
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

//    private void loadMainFragment(Bundle savedInstanceState, String filter) {
//        if (savedInstanceState == null) {
//            final FragmentManager fragmentManager = getFragmentManager();
//            moviesListFragment = MoviesListFragment.newInstance(filter, Locale.getDefault().getLanguage(), Tasks.SEARCH_BY_FILTER);
//            fragmentManager.beginTransaction().add(R.id.activity_main_container, moviesListFragment, TAG_MOVIES_LIST_FRAGMENT).commit();
//        } else {
//            moviesListFragment = (MoviesListFragment) getFragmentManager().findFragmentByTag(TAG_MOVIES_LIST_FRAGMENT);
//        }
//    }

    public static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null) {
            Log.d(LOG_TAG, "no internet connection");
            return false;
        } else {
            if (info.isConnected()) {
                Log.d(LOG_TAG, " internet connection available...");
                return true;
            } else {
                return false;
            }

        }
    }


    private void searchByName(String name) {
        MoviesListFragment fragment = MoviesListFragment.newInstance(name, Locale.getDefault().getLanguage(), Tasks.SEARCH_BY_NAME, true);
        switchContent(R.id.activity_main_container, fragment);
    }

    public void switchContent(int id, Fragment fragment) {
        //noinspection ResourceType
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment).addToBackStack(null).commit();
    }

    public void switchContent(int id, Fragment fragment, int[] customAnimations) {
        //noinspection ResourceType
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(customAnimations[0], customAnimations[1], customAnimations[2], customAnimations[3])
                .replace(id, fragment).addToBackStack(null).commit();
    }
}
