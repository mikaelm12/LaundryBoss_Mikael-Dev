package edu.mit.mitmobile2.laundry;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;

import edu.mit.mitmobile2.laundry.constants.ValuesObj;

public class AlarmReceiver extends Activity {
	private MediaPlayer mMediaPlayer;
	private PowerManager.WakeLock wl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, "My Tag");
		wl.acquire();

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.laundry_alarmview);

		Button stopAlarm = (Button) findViewById(R.id.bStopAlarm);
		stopAlarm.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				mMediaPlayer.stop();
				finish();
				return false;
			}
		});

		playSound(this.getApplicationContext(), getAlarmUri());
	}

	@Override
	protected void onPause() {
		// end the wakelock
		super.onPause();

		try {
			wl.release();
		} catch (Throwable th) {
			// ignoring this exception, probably wakeLock was already released
			llog("wakelock error caught");
		}
	}

	private void playSound(Context context, Uri alert) {
		mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.setDataSource(context, alert);
			final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.prepare();
				mMediaPlayer.setLooping(true);
				mMediaPlayer.start();
			}
		} catch (IOException e) {
			llog("alarm could not be played...");
		}
	}

	// Get an alarm sound. Try for an alarm. If none set, try notification,
	// Otherwise, ringtone.
	private Uri getAlarmUri() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (alert == null) {
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert == null) {
				alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			}
		}
		return alert;
	}

	private void llog(String msg) {
		ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
	}
}