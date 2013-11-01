package edu.mit.mitmobile2.laundry.httpgetters;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.mit.mitmobile2.laundry.constants.ValuesObj;

public class RetrieveSiteData extends AsyncTask<String, Void, String> {

    public final static Long RETRIEVAL_TIMEOUT_TIME_SECONDS = 10l;

	@Override
	protected String doInBackground(String... urls) {
		final long t = System.currentTimeMillis();
		// llog("starting retrieval");

		StringBuilder builder = new StringBuilder(2048);

		for (String url : urls) {// only 1 url!
			DefaultHttpClient client = new DefaultHttpClient();

			HttpGet httpGet = new HttpGet(url);

			try {
				HttpResponse execute = client.execute(httpGet);

				InputStream content = execute.getEntity().getContent();

				BufferedReader buffer = new BufferedReader(new InputStreamReader(content));

				String s;
				while ((s = buffer.readLine()) != null) {
					builder.append(s);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			llog((System.currentTimeMillis() - t)
					+ "ms. size= " + builder.length() + ". url= " + url);

		}

		return builder.toString();
	}

	@Override
	protected void onPostExecute(String result) {

	}

	private void llog(String msg) {
		ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
	}

}
