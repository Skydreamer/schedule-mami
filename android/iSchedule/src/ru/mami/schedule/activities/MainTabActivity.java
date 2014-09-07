package ru.mami.schedule.activities;

import java.util.ArrayList;

import ru.mami.schedule.R;
import ru.mami.schedule.adapters.DatabaseAdapter;
import ru.mami.schedule.adapters.TabCollectionPagerAdapter;
import ru.mami.schedule.utils.CalendarManager;
import ru.mami.schedule.utils.StringConstants;
import ru.mami.schedule.utils.Subject;
import ru.mami.schedule.utils.UpdateServiceManager;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

import ru.mami.schedule.R;
import ru.mami.schedule.adapters.DatabaseAdapter;
import ru.mami.schedule.adapters.TabCollectionPagerAdapter;
import ru.mami.schedule.utils.CalendarManager;
import ru.mami.schedule.utils.StringConstants;
import ru.mami.schedule.utils.Subject;
import ru.mami.schedule.utils.UpdateServiceManager;

public class MainTabActivity extends FragmentActivity implements
        OnPageChangeListener, TabListener {
    private TabCollectionPagerAdapter collectionPagerAdapter;
    private ExitDialogFragment exitDialogFragment;
    private SharedPreferences sharedPreferences;

    private TextView lastSyncTV;
    private ViewPager tabPager;

    private static final Uri EVENT_URI = CalendarContract.Events.CONTENT_URI;
    private static final String ACCOUNT_NAME = "buyvich@gmail.com";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getClass().getSimpleName(), "MainTabActivity created");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.main_tab_layout);
        lastSyncTV = (TextView) findViewById(R.id.maintab_last_sync);
        tabPager = (ViewPager) findViewById(R.id.tabPager);

        initiateTabs();
    }
    
    private void initiateTabs() {
        collectionPagerAdapter = new TabCollectionPagerAdapter(
                getSupportFragmentManager());
        tabPager.setAdapter(collectionPagerAdapter);
        tabPager.setOnPageChangeListener(this);
        
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Tab weeklyTab = actionBar.newTab();
        weeklyTab.setText("%ближайшие%");
        weeklyTab.setTabListener(this);
        actionBar.addTab(weeklyTab);

        Tab scheduleTab = actionBar.newTab();
        scheduleTab.setText(R.string.schedule);
        scheduleTab.setTabListener(this);
        actionBar.addTab(scheduleTab);

        Tab updatesTab = actionBar.newTab();
        updatesTab.setText(R.string.updates);
        updatesTab.setTabListener(this);
        actionBar.addTab(updatesTab);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String lastSync = sharedPreferences.getString(
                StringConstants.USER_LAST_SYNC_DATE, "-");
        lastSyncTV.setText(lastSync);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(getClass().getSimpleName(), "onStop()");
        // TODO 
        UpdateServiceManager.getInstance().stopService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_check_updates:
                this.testCalendar();
                break;
            case R.id.menu_exit:
                if (exitDialogFragment == null)
                    exitDialogFragment = new ExitDialogFragment(
                            getString(R.string.sure_to_exit), this);
                exitDialogFragment.show(getSupportFragmentManager(), "");
                break;
            case R.id.menu_info:
                intent = new Intent(this, UserInfoDialogActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_settings:
                intent = new Intent(this, AppPreferenceActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }

    private void testCalendar() {
        DatabaseAdapter subjectAdapter = DatabaseAdapter.getInstance(this);
        CalendarManager calendarManager = new CalendarManager(this);
        ArrayList<Subject> subjects = subjectAdapter.getAll();
        for (Subject subject : subjects) {
            calendarManager.addEvent(subject.getSubject(), subject.getPlace(),
                    subject.getDt_start(), subject.getDt_end(),
                    subject.getT_start(), subject.getT_end(),
                    subject.getPeriod());
        }

    }

    /** Builds the Uri for events (as a Sync Adapter) */
    public static Uri buildEventUri() {
        return EVENT_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,
                        "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME,
                        ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE,
                        CalendarContract.ACCOUNT_TYPE_LOCAL).build();
    }

    @Override
    public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
        tabPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
    }

    @Override
    public void onPageScrollStateChanged(int position) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        getActionBar().setSelectedNavigationItem(position);
    }

}

class ExitDialogFragment extends android.support.v4.app.DialogFragment
        implements DialogInterface.OnClickListener {
    private final String question;
    private final Activity activity;

    public ExitDialogFragment(String question, Activity activity) {
        this.question = question;
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setMessage(question);

        builder.setPositiveButton(getString(R.string.dialog_yes), this);
        builder.setNegativeButton(getString(R.string.dialog_no), this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(activity);
                SharedPreferences.Editor preferenceEditor = sharedPreferences
                        .edit();
                preferenceEditor.remove(StringConstants.USER_TOKEN);
                preferenceEditor.remove(StringConstants.USER_LAST_SYNC_DATE);
                preferenceEditor.apply();

                // ??? ???
                SharedPreferences schedule = activity.getSharedPreferences(
                        StringConstants.MY_SCHEDULE, Context.MODE_PRIVATE);
                preferenceEditor = schedule.edit();
                for (String key : sharedPreferences.getAll().keySet()) {
                    preferenceEditor.remove(key);
                }
                preferenceEditor.apply();

                startActivity(new Intent(activity, IScheduleActivity.class));
                activity.finish();
                break;
            case Dialog.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
        }
    }

}
