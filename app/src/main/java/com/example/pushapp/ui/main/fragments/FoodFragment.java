package com.example.pushapp.ui.main.fragments;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pushapp.R;
import com.example.pushapp.models.api.EdamamApi;
import com.example.pushapp.models.api.food.EdamamResponse;
import com.example.pushapp.models.api.food.Food;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FoodFragment extends Fragment {

    // --- Constants ---
    private static final String APP_ID = "cf7d5953";
    private static final String APP_KEY = "5434e77ac49ff44200f1702a480ae8ec";
    private static final String BASE_URL = "https://api.edamam.com/";

    // Storage Keys
    private static final String PREF_NAME = "FoodDataPref";
    private static final String KEY_HISTORY = "food_history_json";

    // --- UI Variables ---
    private TextView tvDateDisplay, tvCalories, tvProtein, tvCarbs, tvFat, tvQuote;
    private TextView tvBreakfastCals, tvSnack1Cals, tvLunchCals, tvDinnerCals;
    private ProgressBar pbCalories;

    // --- Logic & Data ---
    private EdamamApi api;
    private Calendar currentDate;
    private Map<String, DayStats> allHistory = new HashMap<>(); // Holds data for ALL days
    private DayStats currentStats;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH);
    private SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private ArrayAdapter<String> listAdapter;
    private List<String> currentListItems = new ArrayList<>();

    // --- Persistence Tools ---
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    // --- Barcode Scanning ---
    private String pendingMealForScan = ""; // Remembers which meal triggered the scan

    // Defines what happens when the barcode scanner finishes
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    // Barcode found! Call API with the code (UPC/EAN)
                    Toast.makeText(getContext(), "Scanned: " + result.getContents(), Toast.LENGTH_SHORT).show();
                    downloadFoodDetails(result.getContents(), pendingMealForScan, null);
                }
            });

    // Data Class for saving daily progress
    private static class DayStats {
        double totalKcal = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
        double breakfastKcal = 0, snackKcal = 0, lunchKcal = 0, dinnerKcal = 0;
        Map<String, List<String>> mealLogs = new HashMap<String, List<String>>() {{
            put("Breakfast", new ArrayList<>());
            put("Snack", new ArrayList<>());
            put("Lunch", new ArrayList<>());
            put("Dinner", new ArrayList<>());
        }};
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup Status Bar (Transparent)
        setupWindow();

        // 2. Load Saved Data from Disk
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadHistoryData();

        // 3. Setup Networking
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS).build();
        api = new Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(EdamamApi.class);

        // 4. Init UI
        currentDate = Calendar.getInstance();
        initUI(view);
        loadDayData(); // Display data for today
    }

    private void setupWindow() {
        if (getActivity() != null) {
            android.view.Window window = getActivity().getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false);
            androidx.core.view.WindowInsetsControllerCompat wic = androidx.core.view.WindowCompat.getInsetsController(window, window.getDecorView());
            if (wic != null) wic.setAppearanceLightStatusBars(true);
        }
    }

    private void initUI(View view) {
        tvDateDisplay = view.findViewById(R.id.tvDateDisplay);
        tvQuote = view.findViewById(R.id.tvMotivationalQuote);
        tvCalories = view.findViewById(R.id.tvCaloriesEaten);
        tvProtein = view.findViewById(R.id.tvProteinVal);
        tvCarbs = view.findViewById(R.id.tvCarbsVal);
        tvFat = view.findViewById(R.id.tvFatVal);
        pbCalories = view.findViewById(R.id.progressBarCalories);

        tvBreakfastCals = view.findViewById(R.id.tvBreakfastCals);
        tvSnack1Cals = view.findViewById(R.id.tvSnack1Cals);
        tvLunchCals = view.findViewById(R.id.tvLunchCals);
        tvDinnerCals = view.findViewById(R.id.tvDinnerCals);

        view.findViewById(R.id.btnPrevDay).setOnClickListener(v -> changeDate(-1));
        view.findViewById(R.id.btnNextDay).setOnClickListener(v -> changeDate(1));

        view.findViewById(R.id.btnAddBreakfast).setOnClickListener(v -> showModernSearch("Breakfast", tvBreakfastCals));
        view.findViewById(R.id.btnAddSnack1).setOnClickListener(v -> showModernSearch("Snack", tvSnack1Cals));
        view.findViewById(R.id.btnAddLunch).setOnClickListener(v -> showModernSearch("Lunch", tvLunchCals));
        view.findViewById(R.id.btnAddDinner).setOnClickListener(v -> showModernSearch("Dinner", tvDinnerCals));
    }

    // --- Data Logic ---

    private void loadDayData() {
        String key = keyFormat.format(currentDate.getTime());

        // Retrieve stats for this specific day, or create new if empty
        if (allHistory.containsKey(key)) {
            currentStats = allHistory.get(key);
        } else {
            currentStats = new DayStats();
            allHistory.put(key, currentStats);
        }

        boolean isToday = keyFormat.format(Calendar.getInstance().getTime()).equals(key);
        tvDateDisplay.setText(isToday ? "Today" : dateFormat.format(currentDate.getTime()));

        refreshUI();
    }

    private void refreshUI() {
        tvCalories.setText(String.format(Locale.US, "%.0f", currentStats.totalKcal));

        // Smooth Animation for Progress Bar
        int currentProgress = pbCalories.getProgress();
        int targetProgress = (int) ((currentStats.totalKcal / 2500) * 100); // Assuming 2500 goal
        ObjectAnimator anim = ObjectAnimator.ofInt(pbCalories, "progress", currentProgress, targetProgress);
        anim.setDuration(500); // 0.5s animation
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();

        // Color Logic: Red if over limit, Blue otherwise
        if (targetProgress > 100) pbCalories.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#BA1A1A")));
        else pbCalories.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#005BB1")));

        tvProtein.setText(String.format("%.1fg", currentStats.totalProtein));
        tvCarbs.setText(String.format("%.1fg", currentStats.totalCarbs));
        tvFat.setText(String.format("%.1fg", currentStats.totalFat));

        tvBreakfastCals.setText(String.format("%.0f kcal", currentStats.breakfastKcal));
        tvSnack1Cals.setText(String.format("%.0f kcal", currentStats.snackKcal));
        tvLunchCals.setText(String.format("%.0f kcal", currentStats.lunchKcal));
        tvDinnerCals.setText(String.format("%.0f kcal", currentStats.dinnerKcal));
    }

    private void changeDate(int days) {
        currentDate.add(Calendar.DAY_OF_YEAR, days);
        loadDayData();
    }

    // --- Search & Barcode Dialog ---

    private void showModernSearch(String mealName, TextView mealTv) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_food, null);
        dialog.setContentView(dv);

        TextView tvTitle = dv.findViewById(R.id.tvDialogTitle);
        EditText etSearch = dv.findViewById(R.id.etSearchFood);
        ListView lv = dv.findViewById(R.id.lvFoodResults);
        TextView tvLog = dv.findViewById(R.id.tvFoodLog);

        // IMPORTANT: Add an ImageButton with ID btnScanBarcode in your XML
        ImageButton btnScan = dv.findViewById(R.id.btnScanBarcode);

        tvTitle.setText("Add to " + mealName);
        updatePopupLog(mealName, tvLog);

        // Barcode Button Logic
        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                pendingMealForScan = mealName; // Remember which meal we are adding to
                dialog.dismiss(); // Close dialog to show camera
                scanBarcode();
            });
        }

        currentListItems.clear();
        loadQuickSuggestions(mealName);
        listAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, currentListItems);
        lv.setAdapter(listAdapter);

        // Click on list item
        lv.setOnItemClickListener((p, v, pos, id) ->
                downloadFoodDetails(currentListItems.get(pos), mealName, tvLog));

        // Search text change listener
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int b, int c, int a) {}
            public void onTextChanged(CharSequence s, int b, int be, int c) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
            }
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    loadQuickSuggestions(mealName);
                    listAdapter.notifyDataSetChanged();
                } else if (s.length() >= 3) {
                    searchRunnable = () -> downloadSuggestions(s.toString());
                    handler.postDelayed(searchRunnable, 600);
                }
            }
        });

        dv.findViewById(R.id.btnDone).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void scanBarcode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a barcode");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void updatePopupLog(String meal, TextView tv) {
        List<String> logs = currentStats.mealLogs.get(meal);
        if (logs.isEmpty()) tv.setText("No items added yet.");
        else {
            StringBuilder sb = new StringBuilder();
            for (String s : logs) sb.append("• ").append(s).append("\n");
            tv.setText(sb.toString());
        }
    }

    // --- API Logic ---

    private void loadQuickSuggestions(String meal) {
        currentListItems.clear();
        if (meal.equals("Breakfast")) currentListItems.addAll(Arrays.asList("1 cup Milk", "1 large Egg", "Oatmeal", "1 Banana"));
        else if (meal.equals("Lunch")) currentListItems.addAll(Arrays.asList("150g Chicken", "100g Rice", "Pasta", "Green Salad"));
        else if (meal.equals("Dinner")) currentListItems.addAll(Arrays.asList("200g Salmon", "Vegetable Soup", "Steak", "Potatoes"));
        else currentListItems.addAll(Arrays.asList("Apple", "Almonds", "Yogurt", "Protein Bar"));
    }

    private void downloadSuggestions(String query) {
        api.getSuggestions(APP_ID, APP_KEY, query).enqueue(new Callback<List<String>>() {
            public void onResponse(Call<List<String>> c, Response<List<String>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    currentListItems.clear();
                    currentListItems.addAll(r.body());
                    listAdapter.notifyDataSetChanged();
                }
            }
            public void onFailure(Call<List<String>> c, Throwable t) {}
        });
    }

    // Pass logTv as null if coming from barcode (since dialog is closed)
    private void downloadFoodDetails(String query, String meal, @Nullable TextView logTv) {
        api.getFoodDetails(APP_ID, APP_KEY, query, "logging").enqueue(new Callback<EdamamResponse>() {
            public void onResponse(Call<EdamamResponse> c, Response<EdamamResponse> r) {
                if (r.isSuccessful() && r.body() != null && !r.body().hints.isEmpty()) {
                    Food f = r.body().hints.get(0).food;

                    // Update Stats
                    currentStats.totalKcal += f.nutrients.calories;
                    currentStats.totalProtein += f.nutrients.protein;
                    currentStats.totalCarbs += f.nutrients.carbs;
                    currentStats.totalFat += f.nutrients.fat;

                    if (meal.equals("Breakfast")) currentStats.breakfastKcal += f.nutrients.calories;
                    else if (meal.equals("Snack")) currentStats.snackKcal += f.nutrients.calories;
                    else if (meal.equals("Lunch")) currentStats.lunchKcal += f.nutrients.calories;
                    else if (meal.equals("Dinner")) currentStats.dinnerKcal += f.nutrients.calories;

                    // Add to log
                    currentStats.mealLogs.get(meal).add(f.label + " (" + (int)f.nutrients.calories + " kcal)");

                    // SAVE DATA
                    saveHistoryData();
                    refreshUI();
                    if(logTv != null) updatePopupLog(meal, logTv);

                } else {
                    showErrorDialog(query);
                }
            }
            public void onFailure(Call<EdamamResponse> c, Throwable t) { showErrorDialog("Connection Error"); }
        });
    }

    // --- Persistence (Save/Load) ---

    private void saveHistoryData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(allHistory); // Convert Map to JSON
        editor.putString(KEY_HISTORY, json);
        editor.apply();
    }

    private void loadHistoryData() {
        String json = sharedPreferences.getString(KEY_HISTORY, "");
        if (!json.isEmpty()) {
            // Convert JSON back to Map<String, DayStats>
            Type type = new TypeToken<Map<String, DayStats>>(){}.getType();
            allHistory = gson.fromJson(json, type);
        }
    }

    private void showErrorDialog(String msg) {
        if(getContext() == null) return;
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_error_food, null);
        b.setView(v);
        TextView tvM = v.findViewById(R.id.tvErrorMessage);
        tvM.setText("Could not find '" + msg + "'. Try checking spelling or internet.");
        AlertDialog d = b.create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        v.findViewById(R.id.btnErrorOk).setOnClickListener(x -> d.dismiss());
        d.show();
    }
}