package ru.surf.course.movierecommendations.tmdbTasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.surf.course.movierecommendations.BuildConfig;
import ru.surf.course.movierecommendations.models.Actor;
import ru.surf.course.movierecommendations.models.Credit;
import ru.surf.course.movierecommendations.models.CrewMember;
import ru.surf.course.movierecommendations.models.Media;
import ru.surf.course.movierecommendations.models.MovieInfo;
import ru.surf.course.movierecommendations.models.Person;
import ru.surf.course.movierecommendations.models.TVShowInfo;

/**
 * Created by andrew on 2/3/17.
 */

public class GetCreditsTask extends AsyncTask<String, Void, List<Credit>> {

    private final String API_KEY_PARAM = "api_key";
    private final String API_LANGUAGE_PARAM = "language";
    private final String TMDB_CAST = "cast";
    private final String TMDB_CREW = "crew";
    private final String TMDB_CAST_ID = "cast_id";
    private final String TMDB_CHARACTER = "character";
    private final String TMDB_CREDIT_ID = "credit_id";
    private final String TMDB_ID = "id";
    private final String TMDB_NAME = "name";
    private final String TMDB_ORDER = "order";
    private final String TMDB_PROFILE_PATH = "profile_path";
    private final String TMDB_DEPARTMENT = "department";
    private final String TMDB_JOB = "job";
    private final String TMDB_MEDIA_TYPE = "media_type";
    private final String TMDB_MOVIE = "movie";
    private final String TMDB_TV = "tv";
    private final String TMDB_ORIGINAL_TITLE = "original_title";
    private final String TMDB_ORIGINAL_NAME = "original_name";
    private final String TMDB_FIRST_AIR_DATE = "first_air_date";
    private final String TMDB_POSTER_PATH = "poster_path";
    private final String TMDB_TITLE = "title";
    private final String TMDB_DATE = "release_date";
    private final String LOG_TAG = getClass().getSimpleName();

    private Tasks task;
    private Locale language;
    private boolean isLoadingList;
    private List<CreditsTaskCompleteListener> listeners;

    public GetCreditsTask() {
        listeners = new ArrayList<>();
    }

    @Override
    protected List<Credit> doInBackground(String... strings) {

        if (strings.length == 0)
            return null;

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;

        String creditsJsonStr = null;

        try {

            Uri builtUri;
            switch (task) {
                case GET_MOVIE_CREDITS:
                    builtUri = uriByMovieId(Integer.valueOf(strings[0]));
                    break;
                case GET_PERSON_CREDITS:
                    builtUri = uriByPersonId(Integer.valueOf(strings[0]), language);
                    break;
                case GET_TV_CREDITS:
                    builtUri = uriByTVShowId(Integer.valueOf(strings[0]));
                    break;
                default:
                    builtUri = Uri.EMPTY;
            }

            Log.d(LOG_TAG, "Built uri" + builtUri);

            URL url = new URL(builtUri.toString());

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            creditsJsonStr = buffer.toString();

            Log.v(LOG_TAG, "Credits list: " + creditsJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            List<Credit> result = parseJson(creditsJsonStr);
            return result;
        } catch (JSONException | ParseException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Credit> credits) {
        invokeCompleteEvent(credits);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    private List<Credit> parseJson(String jsonStr) throws JSONException, ParseException {

        JSONObject jsonObject = new JSONObject(jsonStr);

        List<Credit> result = new ArrayList<>();

        switch (task) {
            case GET_MOVIE_CREDITS:
            case GET_TV_CREDITS:
                result = parseMediaCredits(jsonObject);
                break;
            case GET_PERSON_CREDITS:
                result = parsePersonCredits(jsonObject);
                break;
        }

        return result;

    }

    public void getMovieCredits(int movieId) {
        isLoadingList = true;
        task = Tasks.GET_MOVIE_CREDITS;
        execute(String.valueOf(movieId));
    }

    public void getTVShowCredits(int tvShowId) {
        isLoadingList = true;
        task = Tasks.GET_TV_CREDITS;
        execute(String.valueOf(tvShowId));
    }

    public void getPersonCredits(int personId, Locale language) {
        isLoadingList = true;
        task = Tasks.GET_PERSON_CREDITS;
        this.language = language;
        execute(String.valueOf(personId));
    }

    private Uri uriByMovieId(int movieId) {
        final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie";
        final String TMDB_CREDITS = "credits";
        return Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(TMDB_CREDITS)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .build();
    }

    private Uri uriByTVShowId(int tvShowId) {
        final String TMDB_BASE_URL = "https://api.themoviedb.org/3/tv";
        final String TMDB_CREDITS = "credits";
        return Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(String.valueOf(tvShowId))
                .appendPath(TMDB_CREDITS)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .build();
    }

    private Uri uriByPersonId(int personId, Locale language) {
        final String TMDB_BASE_URL = "https://api.themoviedb.org/3/person";
        final String TMDB_CREDITS = "combined_credits";
        return Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(String.valueOf(personId))
                .appendPath(TMDB_CREDITS)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .appendQueryParameter(API_LANGUAGE_PARAM, language.getLanguage())
                .build();
    }

    private List<Credit> parseMediaCredits(JSONObject jsonObject) throws JSONException {
        JSONArray cast = jsonObject.getJSONArray(TMDB_CAST);
        JSONArray crew = jsonObject.getJSONArray(TMDB_CREW);
        List<Credit> result = new ArrayList<>();
        Credit credit;

        for (int i = 0; i < cast.length(); i++) {

            int castId = 0;
            try {
                castId = cast.getJSONObject(i).getInt(TMDB_CAST_ID);
            } catch (JSONException e ) {
                Log.d(LOG_TAG, "No cast id ", e);
            }

            credit = new Actor(
                    cast.getJSONObject(i).getString(TMDB_CREDIT_ID),
                    new Person(
                            cast.getJSONObject(i).getString(TMDB_NAME),
                            cast.getJSONObject(i).getInt(TMDB_ID),
                            cast.getJSONObject(i).getString(TMDB_PROFILE_PATH)
                    ),
                    castId,
                    cast.getJSONObject(i).getString(TMDB_CHARACTER),
                    cast.getJSONObject(i).getInt(TMDB_ORDER)
            );
            result.add(credit);
        }

        for (int i = 0; i < crew.length(); i++) {
            credit = new CrewMember(
                    crew.getJSONObject(i).getString(TMDB_CREDIT_ID),
                    new Person(
                            crew.getJSONObject(i).getString(TMDB_NAME),
                            crew.getJSONObject(i).getInt(TMDB_ID),
                            crew.getJSONObject(i).getString(TMDB_PROFILE_PATH)
                    ),
                    crew.getJSONObject(i).getString(TMDB_DEPARTMENT),
                    crew.getJSONObject(i).getString(TMDB_JOB)
            );
            result.add(credit);
        }

        return result;
    }

    private List<Credit> parsePersonCredits(JSONObject jsonObject) throws JSONException, ParseException
    {

        JSONArray cast = jsonObject.getJSONArray(TMDB_CAST);
        JSONArray crew = jsonObject.getJSONArray(TMDB_CREW);
        List<Credit> result = new ArrayList<>();
        Credit credit;
        for (int i = 0; i < cast.length(); i++) {


            credit = new Actor(
                    cast.getJSONObject(i).getString(TMDB_CREDIT_ID),
                    parseMediaInfo(cast.getJSONObject(i)),
                    cast.getJSONObject(i).getString(TMDB_CHARACTER)
            );
            result.add(credit);
        }

        for (int i = 0; i < crew.length(); i++) {


            credit = new CrewMember(
                    crew.getJSONObject(i).getString(TMDB_CREDIT_ID),
                    parseMediaInfo(crew.getJSONObject(i)),
                    crew.getJSONObject(i).getString(TMDB_DEPARTMENT),
                    crew.getJSONObject(i).getString(TMDB_JOB)
            );
            result.add(credit);
        }

        return result;
    }

    private Media parseMediaInfo(JSONObject jsonObject) throws JSONException, ParseException
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Media media;

        if (jsonObject.getString(TMDB_MEDIA_TYPE).equals(TMDB_MOVIE)) {
            media = new MovieInfo(jsonObject.getInt(TMDB_ID));
            media.setOriginalTitle(jsonObject.getString(TMDB_ORIGINAL_TITLE));
            media.setTitle(jsonObject.getString(TMDB_TITLE));

            try {
                media.setDate(simpleDateFormat.parse(jsonObject.getString(TMDB_DATE)));
            } catch (ParseException e) {
                Log.d(LOG_TAG, "Error parsing date");
            }
        }
        else {
            media = new TVShowInfo(jsonObject.getInt(TMDB_ID));
            media.setOriginalTitle(jsonObject.getString(TMDB_ORIGINAL_NAME));
            media.setTitle(jsonObject.getString(TMDB_NAME));

            try {
                media.setDate(simpleDateFormat.parse(jsonObject.getString(TMDB_FIRST_AIR_DATE)));
            } catch (ParseException e) {
                Log.d(LOG_TAG, "Error parsing date");
            }
        }
        media.setPosterPath(jsonObject.getString(TMDB_POSTER_PATH));

        return media;
    }

    public void addListener(CreditsTaskCompleteListener listener) {
        listeners.add(listener);
    }

    private void invokeCompleteEvent(List<Credit> result) {
        for (int i = 0; i < listeners.size(); i++)
            listeners.get(i).taskCompleted(result);
    }

    public interface CreditsTaskCompleteListener {
        void taskCompleted(List<Credit> result);
    }

}
