package com.mx.rxjavaexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Map;
import java.util.TreeMap;

public class RxJavaActivity extends AppCompatActivity {

    private ViewGroup container;
    private TreeMap<String,Class<? extends Activity>>buttons=new TreeMap<String, Class<? extends Activity>>(){
        {
            put("Animation",AnimationActivity.class);
            put("Throttle search",ThrottleSearchActivity.class);
            put("NetWork",RetrofitActivity.class);
            put("Working with Realm",GotchasActivity.class);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_java);
        container= (ViewGroup) findViewById(R.id.list);
        setButtons();


    }

    private void setButtons() {

        for(final Map.Entry<String,Class<? extends Activity>> entry:buttons.entrySet()){
            Button button=new Button(this);
            button.setText(entry.getKey());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(entry.getValue());
                }
            });
            container.addView(button);
        }
    }

    private void startActivity(Class<? extends  Activity>activity){
        startActivity(new Intent(this,activity));
    }

}
