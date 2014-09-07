package ru.mami.schedule.activities;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import ru.mami.schedule.R;
import ru.mami.schedule.adapters.UserAdapter;
import ru.mami.schedule.utils.RequestStringsCreater;
import ru.mami.schedule.utils.StringConstants;
import ru.mami.schedule.utils.UpdateServiceManager;
import ru.mami.schedule.utils.XMLParser;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class IScheduleActivity extends Activity implements OnClickListener {
    private SharedPreferences sharedPreferences;
    private InputMethodManager inputMethodManager;
    private EditText loginEditText;
    private EditText passEditText;
    private Button loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getClass().getSimpleName(), "IScheduleActivity created");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        if (sharedPreferences.getString(StringConstants.USER_TOKEN, null) == null) {
            setContentView(R.layout.auth_layout);
        } else {
            Log.i(getClass().getSimpleName(), "Load user info from preferences (token is found)");
            startMainTabActivity();
        }
    }
    
    @Override
    protected void onStart() {
        this.loginEditText = (EditText) findViewById(R.id.loginEText);
        this.passEditText = (EditText) findViewById(R.id.passEText);
        this.loginButton = (Button) findViewById(R.id.logInButton);
        this.loginButton.setOnClickListener(this);

        super.onStart();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
        case R.id.logInButton:
        	Log.i(getClass().getSimpleName(), "Login button clicked");
            if (this.loginEditText.getText().toString().isEmpty()) {
                Log.i(getClass().getSimpleName(), "Not valid login");
                Toast.makeText(IScheduleActivity.this, R.string.login_empty, Toast.LENGTH_SHORT).show();
            } else if (this.passEditText.getText().toString().isEmpty()) {
                Log.i(getClass().getSimpleName(), "Not valid password");
                Toast.makeText(IScheduleActivity.this, R.string.password_empty, Toast.LENGTH_SHORT).show();
            } else {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                PostRequestAuthManager authManager = new PostRequestAuthManager(
                    loginEditText.getText().toString(),
                    passEditText.getText().toString());
                authManager.execute();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startMainTabActivity() {
        Log.i(getClass().getSimpleName(), "Starting MainTabActivity");
        ////////
        UpdateServiceManager.getInstance().startService();
        ////////
        startActivity(new Intent(this, MainTabActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.ischedule_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_compose:
            startActivity(new Intent(this, AppPreferenceActivity.class));
            break;
        }

        return true;
    }


    private class PostRequestAuthManager extends
            AsyncTask<Void, Void, HttpResponse> {

        private final String login;
        private final String pass;
        private String token;
        private ProgressDialog progressDialog;
        
        private final int connectionTimeout = 3000; // in milliseconds

        public PostRequestAuthManager(String login, String pass) {
            this.login = login;
            this.pass = pass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(getClass().getSimpleName(), "onPreExecute()");       
            progressDialog = ProgressDialog.show(IScheduleActivity.this, "", getString(R.string.loading), true);
        }
        
        @Override
        protected HttpResponse doInBackground(Void... params) {
            Log.i(getClass().getSimpleName(), "doInBackground()");
            Thread.currentThread().setName(getClass().getName());
            
            String reqString = null;
            try {
                reqString = RequestStringsCreater.createAuthRequestSting(
                        login, pass);
            } catch (ParserConfigurationException e1) {
                e1.printStackTrace();
            }
            HttpResponse response = null;
            try {
                String httpHost = sharedPreferences.getString(getString(R.string.pref_address_host_id),
                        StringConstants.DEFAULT_HOST);
                String httpPort = sharedPreferences.getString(getString(R.string.pref_address_port_id),
                        StringConstants.DEFAULT_PORT);
                String httpUri = httpHost + ":" + httpPort + "/main";
                Log.i(getClass().getSimpleName(), "Try to connect: " + httpUri);
                HttpPost request = new HttpPost(httpUri);
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
                HttpClient client = new DefaultHttpClient(httpParams);

                StringEntity entity = new StringEntity(reqString, "UTF-8");
                request.setHeader(HTTP.CONTENT_TYPE, "text/xml");
                request.setEntity(entity);
                response = client.execute(request);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (ConnectTimeoutException e) {
            	Log.e(getClass().getSimpleName(), "Не удалось подключится к серверу. Истекло время подключения.");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponse response) {
            super.onPostExecute(response);
            Log.i(getClass().getSimpleName(), "onPostExecute()");
            if (response != null) {
                Log.i(getClass().getSimpleName(), "Response status code: " + response.getStatusLine().toString());
                Log.i(getClass().getSimpleName(), "Response content length: " + response.getEntity().getContentLength());
                
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    try {
                        String entityString = EntityUtils.toString(entity); 
                        
                        Log.i(getClass().getSimpleName(), "Got XML: " + entityString);
                        Map<String, String> resultMap = XMLParser.parseResponse(entityString, "/response/*");
                        if(resultMap.get(XMLParser.STATUS).equals(XMLParser.OK)) {
                            Toast.makeText(IScheduleActivity.this,
                                    getString(R.string.auth_success), Toast.LENGTH_LONG).show();
                            this.token = resultMap.get(XMLParser.TOKEN);
                            Log.i(getClass().getSimpleName(), "Got token: " + this.token);
                            saveSessionData();
    
                            resultMap = XMLParser.parseResponse(entityString, "/response/login-session/user/*");
                            UserAdapter us = new UserAdapter(getApplicationContext());
                            us.saveUser(resultMap.get(XMLParser.NAME), resultMap.get(XMLParser.LOGIN),
                                    resultMap.get(XMLParser.EMAIL), resultMap.get(XMLParser.PHONE));
                            startMainTabActivity();
    
                        } else {
                            Toast.makeText(IScheduleActivity.this,
                                    getString(R.string.login_error), Toast.LENGTH_LONG).show();
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Log.i(getClass().getSimpleName(), "Response is null");
                }
            } else {
                Toast.makeText(IScheduleActivity.this,
                        "Не получилось соединиться с серверoм.",
                        Toast.LENGTH_LONG).show();
            }
            progressDialog.dismiss();
        }

        private void saveSessionData() {
            Editor editor = sharedPreferences.edit();
            editor.putString(StringConstants.USER_TOKEN, this.token);
            editor.apply();
        }

    }
}