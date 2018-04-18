package jp.kshoji.blehid.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

import jp.kshoji.blehid.sample.R.id;
import jp.kshoji.blehid.sample.R.layout;
import jp.kshoji.blehid.sample.R.string;

/**
 * Main Activity
 * 
 * @author K.Shoji
 */
public class MainActivity extends AppCompatActivity {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        setTitle(getString(string.ble_hid));

        mContext = getApplicationContext();
        
        findViewById(id.mouseButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mouseIntent = new Intent(mContext, MouseActivity.class);
                startActivity(mouseIntent);
            }
        });
        findViewById(id.absoluteMouseButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AbsoluteMouseActivity.class));
            }
        });
        findViewById(id.keyboardButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), KeyboardActivity.class));
            }
        });
        findViewById(id.joystickButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), JoystickActivity.class));
            }
        });
    }
}
