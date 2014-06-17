package ru.mami.schedule.fragments;

import java.util.LinkedList;
import java.util.List;

import ru.mami.schedule.R;
import ru.mami.schedule.utils.StringConstants;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AppPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;
    
    private String addressHostValue;
    private String addressPortValue;
    private String accountListValue;
    private Boolean syncCalendarValue;

    private String addressHostKey;
    private String addressPortKey;
    private String accountListKey;
    private String syncCalendarKey;

    private EditTextPreference addressHostPreference;
    private EditTextPreference addressPortPreference;
    private CheckBoxPreference syncCalendarPreference;
    private ListPreference accountListPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_activity);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        addressHostKey = getString(R.string.pref_address_host_id);
        addressPortKey = getString(R.string.pref_address_port_id);
        accountListKey = getString(R.string.pref_account_list_id);
        syncCalendarKey = getString(R.string.pref_sync_id);

        addressHostPreference = (EditTextPreference) getPreferenceScreen().findPreference(addressHostKey);
        addressPortPreference = (EditTextPreference) getPreferenceScreen().findPreference(addressPortKey);
        syncCalendarPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(syncCalendarKey);
        accountListPreference = (ListPreference) getPreferenceScreen().findPreference(accountListKey);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        Log.i(getClass().getSimpleName(), "onSharedPreferenceChanged()");
        Log.i(getClass().getSimpleName(), "Field: [" + key + "], new value: " + sharedPreferences.getAll().get(key).toString());
        updatePreferences();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void updatePreferences() {
        Log.i(getClass().getSimpleName(), "Update Preferences");
        addressHostValue = sharedPreferences.getString(addressHostKey, StringConstants.DEFAULT_HOST);
        addressHostPreference.setSummary(addressHostValue);
        addressHostPreference.setText(addressHostValue);

        addressPortValue = sharedPreferences.getString(addressPortKey, StringConstants.DEFAULT_PORT);
        addressPortPreference.setSummary(addressPortValue);
        addressPortPreference.setText(addressPortValue);

        accountListValue = sharedPreferences.getString(accountListKey, null);
        if (accountListValue != null)
            accountListPreference.setSummary(accountListValue);
        else
            // TODO HARDCODE
            accountListPreference.setSummary("Выберите аккаунт для синхронизации");
        
        syncCalendarValue = sharedPreferences.getBoolean(syncCalendarKey, false);

        if (syncCalendarValue) {
            List<String> accountList = getAccountNames();
            CharSequence[] accountSequence = accountList.toArray(new CharSequence[accountList.size()]);
            accountListPreference.setEntries(accountSequence);
            accountListPreference.setEntryValues(accountSequence);
        }
    }

    private List<String> getAccountNames() {
        AccountManager manager = AccountManager.get(getActivity());
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts)
            possibleEmails.add(account.name);

        Log.i(getClass().getSimpleName(), "Possible e-mails: " + possibleEmails.toString());
        return possibleEmails;
    }
}
