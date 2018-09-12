package com.example.android.myfitnessapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.myfitnessapp.DBHelper;
import com.example.android.myfitnessapp.DateDialogFragment;
import com.example.android.myfitnessapp.DateDialogFragmentListener;
import com.example.android.myfitnessapp.MainListAdapter;
import com.example.android.myfitnessapp.MainPagerAdapter;
import com.example.android.myfitnessapp.R;
import com.example.android.myfitnessapp.entities.MainListItem;
import com.example.android.myfitnessapp.entities.PlanListItem;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    Toolbar mToolbar;

    private ArrayList<MainListItem> mList;
    private MainListAdapter mListAdapter;
    private DBHelper mDB;

    private static TextView totalEnergyGainedView;
    private static TextView totalEnergyBurnedView;
    private static TextView totalSugarGainedView;

    private static String mUser;

    private Calendar mDate = new GregorianCalendar();

    private Context mContext;
    private TextView mDateHeader;

    private MainPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private ArrayList<Date> mDates;

    //google SignIn
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    private NavigationView navigationView;

    private int plannedActivityCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getApplication();

        // inicializacia preferences
        SharedPreferences preferences;

        // free vstup
        //boolean free = getIntent().getBooleanExtra(getString(R.string.login_free), false);
        preferences = getSharedPreferences(getString(R.string.login_free), MODE_PRIVATE);
        boolean free = preferences.getBoolean(getString(R.string.login_free_access), false);
        Log.v("MAIN FREE", ""+free);

        // overenie aktulane prihlaseneho pouzivatela
        preferences = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
        if (!preferences.contains(getString(R.string.preferences_session_user)) && !free) {

            // Login intent
            Intent authIntent = new Intent();
            authIntent.setClass(getApplicationContext(), LoginActivity.class);

            startActivity(authIntent);
            finish();
        } else {
            Log.v("MAIN LOGGED", ""+preferences.getString(getString(R.string.preferences_session_user), null));
            mUser = preferences.getString(getString(R.string.preferences_session_user), "0");

            String userName = preferences.getString(getString(R.string.preferences_user_data_username), null);

            preferences = getSharedPreferences(getString(R.string.preferences_user_data), MODE_PRIVATE);
            if (!preferences.contains(getString(R.string.preferences_user_data_weight))) {
                // Create Profile
                Intent createProfileIntent = new Intent();
                createProfileIntent.setClass(getApplicationContext(), CreateProfileActivity.class);
                startActivity(createProfileIntent);
                finish();
            }

            // Layout set
            setContentView(R.layout.activity_navigation);

            // inicializacia databazy
            mDB = new DBHelper(getApplicationContext());

            mDates = new ArrayList<>();

            // List inicializacia
            mList = new ArrayList<>();
            mListAdapter = new MainListAdapter(getApplicationContext(), mList);

            ListView listView = (ListView) findViewById(R.id.main_listview);
            listView.setAdapter(mListAdapter);
            listView.setClickable(true);
            listView.setOnItemClickListener(new ListItemClickListener());

            // inicializacia celkovej energetickej bilancie
            totalEnergyGainedView = (TextView) findViewById(R.id.main_energy_gained);
            totalEnergyBurnedView = (TextView) findViewById(R.id.main_energy_burned);
            totalSugarGainedView = (TextView) findViewById(R.id.main_sugar_gained);

            // nastavenie datumu
            mDateHeader = (TextView) findViewById(R.id.main_date_header);

            // toolbar
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            mToolbar.setTitle(R.string.title_home);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            View headerView = navigationView.getHeaderView(0);

            TextView usernameNavTxtview = (TextView) headerView.findViewById(R.id.nav_header_username);
            TextView emailNavTxtview = (TextView) headerView.findViewById(R.id.nav_header_email);

            if (usernameNavTxtview != null) {
                usernameNavTxtview.setText(userName);
            }
            if (emailNavTxtview != null) {
                emailNavTxtview.setText(mUser);
            }

            // pager setup
            mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

            new GetDatesWithEntries().execute();

            mPagerAdapter.setmCount(1);

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.main_pager);
            if (mViewPager != null) {
                //mViewPager.setAdapter(mPagerAdapter);


                ViewPager.OnPageChangeListener listener;
                mViewPager.addOnPageChangeListener( listener = new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        mViewPager.setCurrentItem(position);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(mDates.get(position));

                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        mDateHeader.setText(sdf.format(mDates.get(position)));

                        new GetTotalEnergyValues().execute(calendar);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

            }

            // kalendar button
            ImageButton calendarBtn = (ImageButton) findViewById(R.id.main_btn_date);
            if (calendarBtn != null) {
                calendarBtn.setOnClickListener(new CalendarOnClickListener());
            }

            // Google SignIn setup
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .build();
            // [END configure_signin]

            // [START build_client]
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addApi(Plus.API)
                    //.addScope(Scopes.PLUS_LOGIN)
                    //.addScope(Scopes.PLUS_ME)
                    .build();

            // kontrola planu
            new CheckPlannedActivities().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.main_menu_add_food) {
            Intent addEntryIntent = new Intent();
            addEntryIntent.setClass(getApplicationContext(), AddItemActivity.class);
            addEntryIntent.putExtra("entryType", true);

            startActivity(addEntryIntent);
            finish();
        }
        if (id == R.id.main_menu_add_activity) {
            Intent addEntryIntent = new Intent();
            addEntryIntent.setClass(getApplicationContext(), AddItemActivity.class);
            addEntryIntent.putExtra("entryType", false);

            startActivity(addEntryIntent);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*SharedPreferences preferences = getSharedPreferences(getString(R.string.login_free), MODE_PRIVATE);
        preferences.edit().clear().apply();*/
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), ProfileActivity.class);

            startActivity(intent);
            finish();
        } else if (id == R.id.nav_plan) {


            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), PlanActivity.class);

            startActivity(intent);
            finish();
        } else if (id == R.id.nav_logout) {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
            boolean hasAcc = preferences.getBoolean(getString(R.string.preferences_session_google), false);

            if (hasAcc)
                signOut();

            logout();
        } else if (id == R.id.nav_stats) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), StatActivity.class);

            startActivity(intent);
            finish();
        } else if (id == R.id.nav_connect_google) {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
            boolean hasAcc = preferences.getBoolean(getString(R.string.preferences_session_google), false);

            if (!hasAcc)
                signIn();
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            //showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    //hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean(getString(R.string.preferences_session_google), true);
            editor.apply();

            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            //updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        //updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        //updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    private class CheckPlannedActivities extends AsyncTask<Void, Void, ArrayList<PlanListItem>> {

        @Override
        protected ArrayList<PlanListItem> doInBackground(Void... params) {

            return mDB.getActPlannedActivities(mUser);
        }

        @Override
        protected void onPostExecute(ArrayList<PlanListItem> plannedActivities) {

            if (plannedActivities != null && !plannedActivities.isEmpty()) {
                // kontrola naplanovanych aktivit
                checkPlan(plannedActivities);

            }
        }
    }

    private void checkPlan(ArrayList<PlanListItem> list) {
        //final long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;
        int activitiesCount = 0;

        for (PlanListItem item : list) {
            activitiesCount++;
            if (Calendar.getInstance().after(item.getmDate())) {


                Calendar itemDate = item.getmDate();
                itemDate.set(Calendar.HOUR_OF_DAY, item.getmDate().get(Calendar.HOUR_OF_DAY) + 1);

                if (Calendar.getInstance().after(itemDate)) {
                }

            }
        }
        plannedActivityCounter = activitiesCount;

        //Toast.makeText(this, "Počet nesplnených naplánovaných aktivít: " + activitiesCount, Toast.LENGTH_LONG).show();
        if (activitiesCount > 0)
            setMenuCounter(activitiesCount);
    }

    private void setMenuCounter(int count) {
        /*TextView view = (TextView) navigationView.getMenu().findItem(R.id.nav_menu_plan_counter).getActionView();
        view.setText(count > 0 ? String.valueOf(count) : null);*/

        Menu menu = navigationView.getMenu();
        MenuItem planItem = menu.findItem(R.id.nav_plan);
        String before = planItem.getTitle().toString();

        String s = before + "   " + count + " ";
        SpannableString sColored = new SpannableString(s);

        sColored.setSpan(new BackgroundColorSpan( Color.RED ), s.length()-3, s.length(), 0);
        sColored.setSpan(new ForegroundColorSpan( Color.WHITE ), s.length()-3, s.length(), 0);

        planItem.setTitle(sColored);
    }

    private class GetDatesWithEntries extends AsyncTask<Void, Void, ArrayList<Date>> {

        @Override
        protected ArrayList<Date> doInBackground(Void... params) {
            ArrayList<Date> dates = new ArrayList<>();

            dates = mDB.getDatesWithEntries(mUser);

            return dates;
        }

        @Override
        protected void onPostExecute(ArrayList<Date> dates) {
            mDates = dates;

            if (dates != null) {
                mPagerAdapter.setmCount(dates.size());
                mPagerAdapter.setmDates(dates);

                mViewPager.setAdapter(mPagerAdapter);
                mViewPager.setCurrentItem(dates.size() - 1);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mDates.get(mDates.size() - 1));

                new GetTotalEnergyValues().execute(calendar);
            }
        }
    }

    private class CalendarOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            //vytvorenie DateDialogFragment
            DateDialogFragment ddf = DateDialogFragment.newInstance(v.getContext(), R.string.date_dialog_title, Calendar.getInstance());

            //nastavenie listenera DateDialogFragmentListener
            ddf.setDateDialogFragmentListener(new DateDialogFragmentListener() {
                @Override
                public void dateDialogFragmentDataSet(Calendar date) {

                    mDate = date;

                    String month = String.valueOf(date.get(Calendar.MONTH) + 1);
                    String dateStr = date.get(Calendar.DAY_OF_MONTH) + "." + month + "." + date.get(Calendar.YEAR);
                    mDateHeader.setText(dateStr);

                    //new GetUserEntries().execute();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dateStr = date.get(Calendar.YEAR) + "-" + month + "-" + date.get(Calendar.DAY_OF_MONTH);
                    Date targetDate = new Date();
                    try {
                        targetDate = sdf.parse(dateStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    int page = mDates.indexOf(targetDate);

                    //Log.v("MAINPAGEdate", "" + mDates.get(0) + " vs " + targetDate);
                    //Log.v("MAINPAGE", "" + page);
                    if (page >= 0 && page < mDates.size()) {
                        mViewPager.setCurrentItem(page);
                    }
                    new GetTotalEnergyValues().execute(mDate);
                }
            });

            ddf.show(getFragmentManager(), "date picker dialog fragment");

        }
    }

    public void logout() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
        preferences.edit().clear().apply();

        preferences = getSharedPreferences(getString(R.string.login_free), MODE_PRIVATE);
        preferences.edit().clear().apply();

        preferences = getSharedPreferences(getString(R.string.preferences_user_data), MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent authIntent = new Intent();
        authIntent.setClass(getApplicationContext(), LoginActivity.class);

        startActivity(authIntent);
        finish();
    }


    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView nameView = (TextView) view.findViewById(R.id.main_list_item_name);
            String name = nameView.getText().toString();
            TextView sizeView = (TextView) view.findViewById(R.id.main_list_item_size);
            String size = sizeView.getText().toString();
            TextView valueView = (TextView) view.findViewById(R.id.main_list_item_value);
            String value = valueView.getText().toString();

            int idToRemove = mList.get(position).getDbID();
            boolean isFoodItem = mList.get(position).isFoodItem();

            Bundle bundle = new Bundle();
            bundle.putInt("id", idToRemove);
            bundle.putString("name", name);
            bundle.putString("size", size);
            bundle.putString("value", value);
            bundle.putBoolean("is_food_item", isFoodItem);


            Intent detailIntent = new Intent();
            detailIntent.setClass(getApplicationContext(), DetailActivity.class);
            detailIntent.putExtras(bundle);

            startActivity(detailIntent);
            finish();
        }
    }

    private class GetUserEntries extends AsyncTask<Void, Void, ArrayList<MainListItem>> {

        @Override
        protected ArrayList<MainListItem> doInBackground(Void... params) {
            ArrayList<MainListItem> entries = new ArrayList<>();

            //if (mDB != null)
                entries = mDB.getAllEntries(mUser, mDate);

            return entries;
        }

        @Override
        protected void onPostExecute(ArrayList<MainListItem> entries) {

            mList = entries;

            if (mList != null && !mList.isEmpty()) {
                mListAdapter.setmItems(mList);
                mListAdapter.notifyDataSetChanged();

                Log.v("MainActivity", "Entries successfully loaded");
            } else {
                mListAdapter.setmItems(new ArrayList<MainListItem>());
                mListAdapter.notifyDataSetChanged();

                Log.e("MainActivity", "NULL ENTRIES");
            }

        }
    }

    private class GetTotalEnergyValues extends AsyncTask<Calendar, Void, Integer[]> {

        @Override
        protected Integer[] doInBackground(Calendar... params) {
            int totalGained = mDB.getTotalGainedValues(mUser, params[0]);
            int totalBurned = mDB.getTotalBurnedValues(mUser, params[0]);

            int totalGainedSugar = mDB.getTotalGainedSValues(mUser, params[0]);

            return new Integer[]{totalGained, totalBurned, totalGainedSugar};
        }

        @Override
        protected void onPostExecute(Integer[] totalValues) {
            if (totalValues != null) {
                String totalGained = String.valueOf(totalValues[0]);
                String totalBurned = "/" + String.valueOf(totalValues[1] + " kcal");

                String totalGainedSugar = String.valueOf(totalValues[2]) + " " + getString(R.string.main_list_svalue_unit);

                totalEnergyGainedView.setText(totalGained);
                totalEnergyBurnedView.setText(totalBurned);

                totalSugarGainedView.setText(totalGainedSugar);
            }
        }
    }
}
