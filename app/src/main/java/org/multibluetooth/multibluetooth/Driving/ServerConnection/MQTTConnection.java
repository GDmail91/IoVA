/*
package org.multibluetooth.multibluetooth.Driving.ServerConnection;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.multibluetooth.multibluetooth.R;

*/
/**
 * Created by YS on 2016-11-01.
 *//*

public class MQTTConnection {

    private Context mContext;

    private Messenger service = null;
    private final Messenger serviceHandler = new Messenger(new ServiceHandler());
    private IntentFilter intentFilter = null;
    private PushReceiver pushReceiver;

    public MQTTConnection(Context context) {
        this.mContext = context;
    }

    public void init() {

        mContext.startService(new Intent(this, MQTTService.class));
        addSubscribeButtonListener();
        addPublishButtonListener();
    }

    public void onStart() {
        mContext.bindService(new Intent(this, MQTTService.class), serviceConnection, 0);
    }

    @Override
    protected void onStop()
    {
        mContext.unbindService(serviceConnection);
    }

    @Override
    protected void onResume()
    {
        mContext.registerReceiver(pushReceiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        mContext.unregisterReceiver(pushReceiver);
    }

    public class PushReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent i)
        {
            String topic = i.getStringExtra(MQTTService.TOPIC);
            String message = i.getStringExtra(MQTTService.MESSAGE);
            Toast.makeText(context, "Push message received - " + topic + ":" + message, Toast.LENGTH_LONG).show();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder)
        {
            service = new Messenger(binder);
            Bundle data = new Bundle();
            //data.putSerializable(MQTTservice.CLASSNAME, MainActivity.class);
            data.putCharSequence(MQTTService.INTENTNAME, "com.example.MQTT.PushReceived");
            Message msg = Message.obtain(null, MQTTService.REGISTER);
            msg.setData(data);
            msg.replyTo = serviceHandler;
            try
            {
                service.send(msg);
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
        }
    };

    private void addSubscribeButtonListener()
    {
        Button subscribeButton = (Button) mContext.findViewById(R.id.buttonSubscribe);
        subscribeButton.setOnClickListener(new View.OnClickListener()
        {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            @Override
            public void onClick(View arg0)
            {
                TextView result = (TextView) findViewById(R.id.textResultStatus);
                EditText t = (EditText) findViewById(R.id.EditTextTopic);
                String topic = t.getText().toString().trim();
                inputMethodManager.hideSoftInputFromWindow(result.getWindowToken(), 0);

                if (topic != null && topic.isEmpty() == false)
                {
                    result.setText("");
                    Bundle data = new Bundle();
                    data.putCharSequence(MQTTservice.TOPIC, topic);
                    Message msg = Message.obtain(null, MQTTservice.SUBSCRIBE);
                    msg.setData(data);
                    msg.replyTo = serviceHandler;
                    try
                    {
                        service.send(msg);
                    }
                    catch (RemoteException e)
                    {
                        e.printStackTrace();
                        result.setText("Subscribe failed with exception:" + e.getMessage());
                    }
                }
                else
                {
                    result.setText("Topic required.");
                }
            }
        });
    }

    private void addPublishButtonListener()
    {
        Button publishButton = (Button) findViewById(R.id.buttonPublish);
        publishButton.setOnClickListener(new View.OnClickListener()
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            @Override
            public void onClick(View arg0)
            {
                EditText t = (EditText) findViewById(R.id.EditTextTopic);
                EditText m = (EditText) findViewById(R.id.editTextMessage);
                TextView result = (TextView) findViewById(R.id.textResultStatus);
                inputMethodManager.hideSoftInputFromWindow(result.getWindowToken(), 0);

                String topic = t.getText().toString().trim();
                String message = m.getText().toString().trim();

                if (topic != null && topic.isEmpty() == false && message != null && message.isEmpty() == false)
                {
                    result.setText("");
                    Bundle data = new Bundle();
                    data.putCharSequence(MQTTService.TOPIC, topic);
                    data.putCharSequence(MQTTService.MESSAGE, message);
                    Message msg = Message.obtain(null, MQTTService.PUBLISH);
                    msg.setData(data);
                    msg.replyTo = serviceHandler;
                    try
                    {
                        service.send(msg);
                    }
                    catch (RemoteException e)
                    {
                        e.printStackTrace();
                        result.setText("Publish failed with exception:" + e.getMessage());
                    }
                }
                else
                {
                    result.setText("Topic and message required.");
                }
            }
        });
    }

    class ServiceHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MQTTService.SUBSCRIBE: 	break;
                case MQTTService.PUBLISH:		break;
                case MQTTService.REGISTER:		break;
                default:
                    super.handleMessage(msg);
                    return;
            }

            Bundle b = msg.getData();
            if (b != null)
            {
                TextView result = (TextView) mContext.findViewById(R.id.textResultStatus);
                Boolean status = b.getBoolean(MQTTService.STATUS);
                if (status == false)
                {
                    result.setText("Fail");
                }
                else
                {
                    result.setText("Success");
                }
            }
        }
    }
}
*/
