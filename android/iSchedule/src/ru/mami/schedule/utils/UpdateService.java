package ru.mami.schedule.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.mami.schedule.R;
import ru.mami.schedule.activities.IScheduleActivity;
import ru.mami.schedule.adapters.DatabaseAdapter;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class UpdateService extends Service {
    public final IBinder binder = new MyBinder();
    private SharedPreferences sharedPreferences;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(getClass().getSimpleName(), "onBind()");
        return this.binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(getClass().getSimpleName(), "onStartCommand()");
        Log.i(getClass().getSimpleName(), "Service params: startId - " + startId + " flags - " + flags);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        
        String httpHostKey = getString(R.string.pref_address_host_id);
        String httpPortKey = getString(R.string.pref_address_port_id);
        String token = sharedPreferences.getString(StringConstants.USER_TOKEN, null);
        String httpHost = sharedPreferences.getString(httpHostKey, "");
        String httpPort = sharedPreferences.getString(httpPortKey, "");
        String httpUri = httpHost + ":" + httpPort + "/main";
        
        if (token != null)
            if (isNetworkAvailable()){
                Log.i(getClass().getSimpleName(), "Starting AsyncUpdater");
                AsyncUpdater updater = new AsyncUpdater(this, token, httpUri);
                updater.execute();
            }
            else
                Log.i(getClass().getSimpleName(), "Not starting AsyncUpdater (network unavailable)");
        else
            Log.i(getClass().getSimpleName(), "No token - exit service");
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(getClass().getSimpleName(), "onCreate()");
    }
    
    public void notifyUser() {
        int notificationID = 001;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        notificationBuilder.setContentTitle("Доступно обновление");
        notificationBuilder.setContentText("Загружена обновленная версия расписания");
        
        Intent clickIntent = new Intent(this, IScheduleActivity.class);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(clickPendingIntent);
        
        NotificationManager notificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    private class AsyncUpdater extends
        AsyncTask<Void, Void, ArrayList<Subject>> {

        private String token;
        private String httpUri;
        private Context context;
        
        private int connectionTimeout = 3000; // in milliseconds

        public AsyncUpdater(Context context, String token, String uri) {
            this.token = token;
            this.context = context;
            this.httpUri = uri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(getClass().getSimpleName(), "onPreExecute");
        }

        @Override
        protected ArrayList<Subject> doInBackground(Void... params) {
            Log.i(getClass().getSimpleName(), "doInBackground");
            Thread.currentThread().setName(getClass().getName());

            String reqString = null;
            try {
                reqString = RequestStringsCreater.createUpdateString(token);
            } catch (ParserConfigurationException exeption) {
                exeption.printStackTrace();
            }
            HttpResponse response = null;
            try {
                Log.i(getClass().getSimpleName(), "Try to connect: " + httpUri);
                HttpPost request = new HttpPost(httpUri);
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
                HttpClient client = new DefaultHttpClient(httpParams);
                
                StringEntity stringEntity = new StringEntity(reqString, "UTF-8");
                request.setHeader(HTTP.CONTENT_TYPE, "text/xml");
                request.setEntity(stringEntity);
                response = client.execute(request);
                
                HttpEntity entity = response.getEntity();
                String entityString = EntityUtils.toString(entity);
                String status = XMLParser.parseResponse(entityString, "/response/*").get("status");
                if (status.equals("OK")) {
                    return parseSchedule(entityString);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(ArrayList<Subject> subjects) {
            super.onPostExecute(subjects);
            Log.i(getClass().getSimpleName(), "onPostExecute");

            if (subjects != null) {
                notifyUser();
            }
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            Editor editor = sharedPreferences.edit();
            Calendar calend = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

            DatabaseAdapter databaseAdapter = DatabaseAdapter.getInstance(context);
            databaseAdapter.syncDB(subjects);

            editor.putString(StringConstants.USER_LAST_SYNC_DATE, sdf.format(calend.getTime()));
            editor.commit();
            stopSelf();
        }

        private ArrayList<Subject> parseSchedule(String response) throws IOException {
            Log.i("parseSchedule", response);
            InputStream is = null;
            ArrayList<Subject> subjects = new ArrayList<Subject>();
            try {
                is = new ByteArrayInputStream(response.getBytes("UTF-8"));
                DocumentBuilder builder;
                builder = XMLParser.getBuilder();
                Document dom = builder.parse(is);
                XPathExpression expr = XMLParser.getXPathExpression("/response/schedule/items/*");
                NodeList subTree = (NodeList) expr.evaluate(dom, XPathConstants.NODESET);
                for (int i = 0; i < subTree.getLength(); ++i) {
                    Node currentNode = subTree.item(i);
                    HashMap<String, String> subject = new HashMap<String, String>();
                    NodeList children = currentNode.getChildNodes();
                    XMLParser.parseSubTree(children, subject, false);
                    Node period = XMLParser.findFirstByName(children, "time").getFirstChild();
                    XMLParser.parseSubTree(period.getChildNodes(), subject, false);
                    subject.put("dt_start", subject.get("start"));
                    subject.put("dt_end", subject.get("end"));

                    HashMap<String, String> hoursMap = new HashMap<String, String>();
                    Node hours = XMLParser.findFirstByName(period.getChildNodes(), "hours");
                    XMLParser.parseSubTree(hours.getChildNodes(), hoursMap, false);

                    subject.put("t_start", hoursMap.get("start"));
                    subject.put("t_end", hoursMap.get("end"));
                    subjects.add(new Subject(subject));
                }
            }
            catch (XPathExpressionException e) {
                e.printStackTrace();
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            catch (SAXException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                is.close();
            }
            return subjects;
        }
    }

    public class MyBinder extends Binder {
        public UpdateService getService() {
          return UpdateService.this;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

}
