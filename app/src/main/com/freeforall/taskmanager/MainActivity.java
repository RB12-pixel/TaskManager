package com.FreeForAll.TaskManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class MainActivity extends Activity {

    private LinearLayout appListLayout;
    private CheckBox filterSystemAppsCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Controllo del permesso UsageStats
        if (!checkUsageStatsPermission()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);

        // --- 1. BARRA DI PROGRESSO E METRICHE RAM ---
        TextView ramTextView = new TextView(this);
        ramTextView.setTextSize(16);

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        long freeMB = mi.availMem / (1024 * 1024);
        long totalMB = mi.totalMem / (1024 * 1024);
        long usedMB = totalMB - freeMB;
        int usedPercentage = (int) ((usedMB * 100) / totalMB);

        ramTextView.setText("=== TASK MONITOR ===\n\n" +
                "RAM Usata: " + usedMB + " MB (" + usedPercentage + "%)\n" +
                "RAM Libera: " + freeMB + " MB / " + totalMB + " MB");
        mainLayout.addView(ramTextView);

        ProgressBar ramProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        ramProgressBar.setMax(100);
        ramProgressBar.setProgress(usedPercentage);
        ramProgressBar.setPadding(0, 15, 0, 30);
        mainLayout.addView(ramProgressBar);

        // --- 2. FILTRO APP UTENTE VS SISTEMA ---
        filterSystemAppsCheckBox = new CheckBox(this);
        filterSystemAppsCheckBox.setText("Mostra solo app scaricate dall'utente");
        filterSystemAppsCheckBox.setChecked(true);
        filterSystemAppsCheckBox.setPadding(0, 10, 0, 20);
        
        filterSystemAppsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                caricaListaApp();
            }
        });
        mainLayout.addView(filterSystemAppsCheckBox);

        // Contenitore per la lista dinamica
        appListLayout = new LinearLayout(this);
        appListLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(appListLayout);

        caricaListaApp();

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private void caricaListaApp() {
        appListLayout.removeAllViews();

        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (1000 * 60 * 60 * 4);

        List<UsageStats> statsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        PackageManager pm = getPackageManager();

        boolean soloUtente = filterSystemAppsCheckBox.isChecked();
        int iconSizePx = (int) (48 * getResources().getDisplayMetrics().density);

        if (statsList != null && !statsList.isEmpty()) {
            for (final UsageStats stats : statsList) {
                if (stats.getLastTimeUsed() > startTime) {
                    try {
                        final String packageName = stats.getPackageName();
                        ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);

                        boolean isSystemApp = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                        if (soloUtente && isSystemApp) {
                            continue;
                        }

                        CharSequence appName = pm.getApplicationLabel(ai);
                        Drawable appIcon = pm.getApplicationIcon(packageName);

                        LinearLayout appCard = new LinearLayout(this);
                        appCard.setOrientation(LinearLayout.VERTICAL);
                        appCard.setPadding(0, 20, 0, 20);

                        LinearLayout headerLayout = new LinearLayout(this);
                        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
                        headerLayout.setGravity(Gravity.CENTER_VERTICAL);

                        ImageView iconView = new ImageView(this);
                        iconView.setImageDrawable(appIcon);
                        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSizePx, iconSizePx);
                        iconParams.setMargins(0, 0, 20, 0);
                        iconView.setLayoutParams(iconParams);
                        headerLayout.addView(iconView);

                        TextView appInfoText = new TextView(this);
                        appInfoText.setText(appName + "\nPkg: " + packageName);
                        appInfoText.setTextSize(14);
                        headerLayout.addView(appInfoText);

                        appCard.addView(headerLayout);

                        LinearLayout btnLayout = new LinearLayout(this);
                        btnLayout.setOrientation(LinearLayout.HORIZONTAL);

                        Button btnInfo = new Button(this);
                        btnInfo.setText("Info / Arresta");
                        btnInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + packageName));
                                startActivity(intent);
                            }
                        });

                        Button btnKill = new Button(this);
                        btnKill.setText("Pulisci RAM");
                        btnKill.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                                manager.killBackgroundProcesses(packageName);
                                Toast.makeText(MainActivity.this, "Terminato background: " + packageName, Toast.LENGTH_SHORT).show();
                            }
                        });

                        btnLayout.addView(btnInfo);
                        btnLayout.addView(btnKill);
                        appCard.addView(btnLayout);

                        appListLayout.addView(appCard);

                    } catch (Exception e) {
                        // Ignora pacchetti non validi
                    }
                }
            }
        }
    }

    private boolean checkUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
