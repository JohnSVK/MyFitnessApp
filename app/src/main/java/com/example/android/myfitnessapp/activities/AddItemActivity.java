package com.example.android.myfitnessapp.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myfitnessapp.CustomPagerAdapater;
import com.example.android.myfitnessapp.DBHelper;
import com.example.android.myfitnessapp.JSONParser;
import com.example.android.myfitnessapp.MainListAdapter;
import com.example.android.myfitnessapp.R;
import com.example.android.myfitnessapp.entities.MainListItem;
import com.github.channguyen.rsv.RangeSliderView;
import com.google.android.gms.plus.PlusShare;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {

    private ArrayList<MainListItem> mList;
    private MainListAdapter mListAdapter;
    private DBHelper mDB;

    private ListView listView;

    private static MainListItem entryToAdd;

    private TextView sizeView;
    private static String entryUnit;
    private static int entryDefaultSize;

    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int PLUS_REQUEST_CODE = 1;

    private static ProgressDialog pDialog;

    private boolean hasValueSet = false;

    private String mInputText;

    private String mUser;

    private int entryState = 0;
    private LinearLayout bottomLayout;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inicializacia databazy
        mDB = new DBHelper(this);

        // vytvorenie polozky na pridanie do zoznamu
        entryToAdd = new MainListItem();

        // ziskanie udaju o type zaznamu
        Intent intent = getIntent();
        entryToAdd.setFoodItem(intent.getBooleanExtra("entryType", true));

        setContentView(R.layout.activity_add_item);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_session), MODE_PRIVATE);
        mUser = preferences.getString(getString(R.string.preferences_session_user), null);

        // zmena oznacenia aktivity
        TextView activityHeaderTextView = (TextView) findViewById(R.id.add_entry_header);
        if (activityHeaderTextView != null) {
            activityHeaderTextView.setText(entryToAdd.isFoodItem() ?
                    getString(R.string.add_entry_food_header) : getString(R.string.add_entry_exercise_header));
        }

        // inicializacia zoznamu a prislusneho adaptera
        mList = new ArrayList<>();
        mListAdapter = new MainListAdapter(getApplicationContext(), mList);

        listView = (ListView) findViewById(R.id.add_item_list);
        if (listView != null) {
            listView.setAdapter(mListAdapter);
            listView.setVisibility(View.INVISIBLE);
        }

        // inicializacia slidera na ziskanie hodnoty mnozstva zaznamu
        RangeSliderView slider = (RangeSliderView) findViewById(R.id.add_entry_slider);
        if (slider != null) {
            slider.setOnSlideListener(new SliderListener());
            slider.setInitialIndex(1);
            slider.setRangeCount(10);
        }

        entryUnit = entryToAdd.isFoodItem() ? "g" : "min";
        entryDefaultSize = entryToAdd.isFoodItem() ? 100 : 30;

        entryToAdd.setmSize(entryDefaultSize);

        String sliderText = entryDefaultSize + entryUnit;
        sizeView = (TextView) findViewById(R.id.add_entry_size);
        if (sizeView != null) {
            sizeView.setText(sliderText);
        }

        // prisposobenie zobrazenej aktivity na zaklade typu polozky
        ViewPager pager = (ViewPager) findViewById(R.id.add_entry_pager);
        LinearLayout pagerLayout = (LinearLayout) findViewById(R.id.add_entry_pager_layout);

        bottomLayout = (LinearLayout) findViewById(R.id.add_entry_bottom_layout);

        if (entryToAdd.isFoodItem()) {

            if (pagerLayout != null) {
                pagerLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            }

            // inicializacia tlacidla mikrofonu a ziskanie nazvu stravy pomocou hlasu
            ImageButton micBtn = new ImageButton(this);
            micBtn.setId(R.id.add_entry_mic_btn);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(120, 120);
            params.height = 220;
            params.width = 220;
            micBtn.setLayoutParams(params);

            micBtn.setImageResource(R.drawable.mic_grey);
            micBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
            micBtn.setBackground(null); //target API 16

            micBtn.setOnClickListener(new MicListener());

            if (bottomLayout != null) {
                bottomLayout.addView(micBtn);
            }


        } else {
            int images[] = {R.drawable.walking, R.drawable.running, R.drawable.cycling};

            CustomPagerAdapater pagerAdapater = new CustomPagerAdapater(this, images);

            if (pager != null) {
                pager.setAdapter(pagerAdapater);

                PagerListener pagerListener = new PagerListener();

                UnderlinePageIndicator mIndicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
                mIndicator.setFades(false);
                mIndicator.setViewPager(pager);
                mIndicator.setOnPageChangeListener(pagerListener);

                pager.addOnPageChangeListener(pagerListener);
            }

            entryToAdd.setmName(getString(R.string.add_entry_exercise_walking));
            entryToAdd.setmValue(getExerciseCalValue(0));
            updateEntryListData();
        }


        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.title_activity_add_item);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
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
        if (id == R.id.menu_add_item_confirm) {
            if (!mList.isEmpty()) {
                new AddEntry().execute(mList.get(0));
            }
        } else if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);

        startActivity(intent);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void switchState(View v) {
        Button btn = (Button) v;

        if (entryState == 0) {
            entryState = 1;
            btn.setText(getString(R.string.add_entry_voice));
            bottomLayout.removeAllViews();

            EditText editTxt = new EditText(this);
            editTxt.setId(R.id.add_entry_edit_txt);
            editTxt.setHint(getString(R.string.add_entry_edit_txt_hint));

            /*ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(120, 120);
            params.height = ;
            params.width = 220;
            editTxt.setLayoutParams(params);*/

            Button confirmBtn = new Button(this);
            confirmBtn.setId(R.id.add_entry_manual_confirm);
            confirmBtn.setText(getText(R.string.add_entry_manual_btn));
            confirmBtn.setOnClickListener(new ManualListener());

            if (bottomLayout != null) {
                bottomLayout.addView(editTxt);
                bottomLayout.addView(confirmBtn);
            }
        } else {
            entryState = 0;
            btn.setText(getString(R.string.add_entry_manual));
            bottomLayout.removeAllViews();

            ImageButton micBtn = new ImageButton(this);
            micBtn.setId(R.id.add_entry_mic_btn);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(120, 120);
            params.height = 220;
            params.width = 220;
            micBtn.setLayoutParams(params);

            micBtn.setImageResource(R.drawable.mic_grey);
            micBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
            micBtn.setBackground(null); //target API 16

            micBtn.setOnClickListener(new MicListener());

            if (bottomLayout != null) {
                bottomLayout.addView(micBtn);
            }
        }
    }

    private class ManualListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            EditText editText = (EditText) bottomLayout.findViewById(R.id.add_entry_edit_txt);
            String foodStr = editText.getText().toString();

            mInputText = foodStr;

            pDialog = new ProgressDialog(v.getContext());
            pDialog.setMessage(getString(R.string.add_entry_progress_dialog));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            new TranslateText().execute(foodStr);
        }
    }

    // share on Google Plus
    private void plusShare(MainListItem item) {
        String text = "";

        if (item.isFoodItem()) {
            text = "Práve som prijal " + item.getmValue() + " kalórií";
        } else {
            text = "Práve som spálil " + item.getmValue() + " kalórií";
        }

        // Launch the Google+ share dialog with attribution to your app.
        Intent shareIntent = new PlusShare.Builder(this)
                .setType("text/plain")
                .setText(text)
                        //.setContentUrl(Uri.parse("https://developers.google.com/+/"))
                .getIntent();

        startActivityForResult(shareIntent, PLUS_REQUEST_CODE);
    }

    private class AddEntry extends AsyncTask<MainListItem, Void, Void> {

        // pridanie novej polozky do databazy
        @Override
        protected Void doInBackground(MainListItem... params) {
            if (params[0] == null)
                return null;

            String name = params[0].getmName();
            int size = params[0].getmSize();
            int value = params[0].getmValue();
            int sValue = params[0].getmSValue();
            int type = (params[0].isFoodItem()) ? 1 : 0;

            mDB.insertEntry(mUser, name, size, value, sValue, type);

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            plusShare(mList.get(0));

        }
    }

    private void updateEntryListData() {
        TextView listPlaceholder = (TextView) findViewById(R.id.add_entry_list_placeholder);
        if (listPlaceholder != null) {
            listPlaceholder.setVisibility(View.INVISIBLE);
        }

        entryToAdd.setmDate(Calendar.getInstance());

        mList.clear();
        mList.add(entryToAdd);

        // aktualizovanie adaptera
        mListAdapter.setmItems(mList);
        mListAdapter.notifyDataSetChanged();
        listView.setVisibility(View.VISIBLE);
    }

    private class ItemCalValue extends AsyncTask<String, Void, Integer[]> {

        private static final String API_KEY = "nPEbhuXN4Yb82Dr8UYALWsCCTPahBZr53ALsxsma";

        private static final String BASE_URL = "http://api.nal.usda.gov/ndb/";

        private static final String PARAM_FORMAT = "format";
        private static final String PARAM_QUERY = "q";
        private static final String PARAM_SORT = "sort";
        private static final String PARAM_MAX = "max";
        private static final String PARAM_OFFSET = "offset";
        private static final String PARAM_KEY = "api_key";

        private static final String PARAM_FOOD_ID = "ndbno";
        private static final String PARAM_REPORT_TYPE = "type";

        @Override
        protected Integer[] doInBackground(String... entryName) {
            Integer foodValues[] = {0, 0};

            int foodEnergyValue = 0;
            int foodSugarValue = 0;
            String foodID = "";

            try {
                // API CALL to get FOOD ID
                Uri builtUri = Uri.parse(BASE_URL + "search/?").buildUpon()
                        .appendQueryParameter(PARAM_FORMAT, "json")
                        .appendQueryParameter(PARAM_QUERY, entryName[0])
                        .appendQueryParameter(PARAM_SORT, "r")
                        .appendQueryParameter(PARAM_MAX, "1")
                        .appendQueryParameter(PARAM_OFFSET, "0")
                        .appendQueryParameter(PARAM_KEY, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v("URL", url.toString());

                JSONObject jsonObject = JSONParser.getJSONFromUrl(url, "GET");
                if (jsonObject == null) {
                    Log.e("AddItemActivity", "json is null");

                    return null;
                }

                try {
                    JSONArray jsonArray = jsonObject.getJSONObject("list").getJSONArray("item");
                    jsonObject = jsonArray.getJSONObject(0);

                    foodID = jsonObject.getString("ndbno");
                } catch (JSONException e) {
                    Log.v("AddItemActivity", "Zadane jedlo nie je v databaze!");

                    foodValues[0] = -1;
                    return foodValues;
                }

                // API CALL to get FOOD CALORIE VALUE
                builtUri = Uri.parse(BASE_URL + "reports/?").buildUpon()
                        .appendQueryParameter(PARAM_FOOD_ID, foodID)
                        .appendQueryParameter(PARAM_REPORT_TYPE, "b")
                        .appendQueryParameter(PARAM_FORMAT, "json")
                        .appendQueryParameter(PARAM_KEY, API_KEY)
                        .build();

                url = new URL(builtUri.toString());

                jsonObject = JSONParser.getJSONFromUrl(url, "GET");
                if (jsonObject == null) {
                    Log.e("AddItemActivity", "json is null");

                    return null;
                }

                JSONArray jsonArray = jsonObject.getJSONObject("report").getJSONObject("food").getJSONArray("nutrients");
                JSONObject jsonObjectEnergy = jsonArray.getJSONObject(1);

                foodEnergyValue = jsonObjectEnergy.getInt("value");

                jsonArray = jsonObject.getJSONObject("report").getJSONObject("food").getJSONArray("nutrients");
                JSONObject jsonObjectSugar = jsonArray.getJSONObject(6);

                /*if (jsonObjectSugar.getInt("nutrient_id") == 269)
                    foodSugarValue = (int) Math.ceil(jsonObjectSugar.getDouble("value") / 10);
                else {*/
                jsonObjectSugar = jsonArray.getJSONObject(4);
                foodSugarValue = (int) Math.ceil(jsonObjectSugar.getDouble("value") / 10);
                //}

                foodValues[0] = foodEnergyValue;
                foodValues[1] = foodSugarValue;

                return foodValues;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return foodValues;
        }

        @Override
        protected void onPostExecute(Integer[] entryCalValue) {
            pDialog.dismiss();

            int calValue = 0;
            if (entryCalValue != null)
                calValue = entryCalValue[0];

            if (entryCalValue != null && entryCalValue[0] != -1) {
                double entryValueKoeficient = entryToAdd.getmSize() / (double) entryDefaultSize;

                int finalEntryCalValue = (int) (entryCalValue[0] * entryValueKoeficient);

                entryToAdd.setmValue(finalEntryCalValue);
                entryToAdd.setmName(mInputText);
                entryToAdd.setmSValue(entryCalValue[1]);

                hasValueSet = true;

                updateEntryListData();
            } else if (calValue == -1) {
                Log.v("AddItemActivity", "Zadane jedlo nie je v databaze!");

                Toast.makeText(getApplicationContext(), getString(R.string.add_entry_food_not_found), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.general_connection_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    // API Translator
    private class TranslateText extends AsyncTask<String, Void, String> {

        private static final String CLIENT_ID = "myfitnessapp";
        private static final String CLIENT_SECRET = "00ujjNNdzbZDhKEIUf55opWOphj9Q3kge6/f5hv6waU=";

        @Override
        protected String doInBackground(String... inputText) {
            String outputText = "";
            try {
                outputText = translate(inputText[0]);
            } catch (Exception e) {
                Log.e("AddItem", "Translator Error", e);
            }
            return outputText;
        }

        @Override
        protected void onPostExecute(String translatedText) {
            new ItemCalValue().execute(translatedText);
        }

        protected String translate(String text) throws Exception {
            Translate.setClientId(CLIENT_ID);
            Translate.setClientSecret(CLIENT_SECRET);

            String[] inputWords = text.split(" ");
            String translatedText = "";

            for (String word : inputWords) {
                translatedText += Translate.execute(word, Language.SLOVAK, Language.ENGLISH);
            }

            return translatedText;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String spokenText = results.get(0);

            if (spokenText != null) {
                //entryToAdd.setmName(spokenText);
                mInputText = results.get(0);

                pDialog = new ProgressDialog(this);
                pDialog.setMessage(getString(R.string.add_entry_progress_dialog));
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();

                new TranslateText().execute(spokenText);
            }
        } else if (requestCode == PLUS_REQUEST_CODE) {
            // Google Plus Result
            Log.v("ADDENTRY", "Successfully posted on Google Plus");

            Intent mainIntent = new Intent();
            mainIntent.setClass(getApplicationContext(), MainActivity.class);

            startActivity(mainIntent);
            finish();
        }
    }

    // Google Voice Recognizer
    private void displaySpeechRecognizer(String promptText) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, promptText);

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Sorry, your device doesnt support speech input",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class MicListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            displaySpeechRecognizer(getString(R.string.add_item_speak));
        }
    }


    private class SliderListener implements RangeSliderView.OnSlideListener {

        @Override
        public void onSlide(int index) {
            int k = entryToAdd.isFoodItem() ? 50 : 15;
            int size = (index + 1) * k;
            int value = (int) ((size / (double) entryToAdd.getmSize()) * (double) entryToAdd.getmValue());
            int sValue = (int) ((size / (double) entryToAdd.getmSize()) * (double) entryToAdd.getmSValue());

            entryToAdd.setmSize(size);
            entryToAdd.setmValue(value);

            sizeView.setText(size + entryUnit);

            if (entryToAdd.isFoodItem()) {
                entryToAdd.setmSValue(sValue);
            }

            if (!entryToAdd.isFoodItem() || hasValueSet)
                updateEntryListData();
        }
    }

    private class PagerListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            int entryValue;

            entryValue = getExerciseCalValue(position);

            entryToAdd.setmValue(entryValue);

            updateEntryListData();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private int getExerciseCalValue(int type) {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_user_data), MODE_PRIVATE);

        int weight = preferences.getInt(getString(R.string.preferences_user_data_weight), 1);
        int duration = entryToAdd.getmSize();

        double koeficient = 0;

        switch (type) {
            case 0: {
                entryToAdd.setmName(getString(R.string.add_entry_exercise_walking));
                koeficient = 0.07;

                break;
            }
            case 1: {
                entryToAdd.setmName(getString(R.string.add_entry_exercise_running));
                koeficient = 0.22;

                break;
            }
            case 2: {
                entryToAdd.setmName(getString(R.string.add_entry_exercise_cycling));
                koeficient = 0.14;

                break;
            }
        }

        return (int) (weight * duration * koeficient);
    }
}
