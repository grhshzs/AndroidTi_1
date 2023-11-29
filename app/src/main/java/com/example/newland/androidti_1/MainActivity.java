package com.example.newland.androidti_1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.newland.CameraManager;
import com.newland.PTZ;
import com.nle.mylibrary.forUse.mdbus4150.Modbus4150;
import com.nle.mylibrary.transfer.ConnectResultListener;
import com.nle.mylibrary.transfer.DataBusFactory;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private TextureView textureView;
    private ImageView start, lamp, _switch, up, down, left, right;
    private Modbus4150 modbus4150;
    private CameraManager cameraManager;

    private Boolean canRun = false, isOpenCamera = false, lampIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);

        start = findViewById(R.id.start);
        lamp = findViewById(R.id.lamp);
        _switch = findViewById(R.id._switch);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);

        modbus4150 = new Modbus4150(DataBusFactory.newSocketDataBus("192.168.1.15", 6001), new ConnectResultListener() {
            @Override
            public void onConnectResult(boolean b) {
                canRun = b;
                Toast.makeText(MainActivity.this, "Connected Successed!", Toast.LENGTH_LONG).show();
            }
        });

        cameraManager = CameraManager.getInstance();

        start.setOnClickListener((V) -> SwitchCamera());

        up.setOnTouchListener(this);
        down.setOnTouchListener(this);
        left.setOnTouchListener(this);
        right.setOnTouchListener(this);

        _switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canRun){
                    if(lampIsOpen){
                        try {
                            modbus4150.ctrlRelay(5, false, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        lamp.setImageResource(R.drawable.pic_lamp_off);
                        _switch.setImageResource(R.drawable.btn_switch_off);
                    }
                    else{
                        try {
                            modbus4150.ctrlRelay(5, true, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        lamp.setImageResource(R.drawable.pic_lamp_on);
                        _switch.setImageResource(R.drawable.btn_switch_on);
                    }
                    lampIsOpen = !lampIsOpen;
                }
            }
        });

        ShowOrHideComponents(false);
    }

    private void SwitchCamera(){
        isOpenCamera = !isOpenCamera;
        if(isOpenCamera){
            cameraManager.setupInfo(textureView, "admin", "admin", "192.168.1.13", "11");
            ShowOrHideComponents(true);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if(!isOpenCamera) return;
                    cameraManager.openCamera();
                }
            }, 300);
        }
        else{
            cameraManager.releaseCamera();
            ShowOrHideComponents(false);
        }
    }

    private void ShowOrHideComponents(Boolean isShow){
        if(isShow){
            start.setImageResource(R.drawable.btn_closed_press);
            up.setVisibility(View.VISIBLE);
            down.setVisibility(View.VISIBLE);
            left.setVisibility(View.VISIBLE);
            right.setVisibility(View.VISIBLE);
        }
        else{
            start.setImageResource(R.drawable.btn_start_press);
            up.setVisibility(View.INVISIBLE);
            down.setVisibility(View.INVISIBLE);
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            if(view.getId() == R.id.up) cameraManager.controlDir(PTZ.Up);
            else if(view.getId() == R.id.down) cameraManager.controlDir(PTZ.Down);
            else if(view.getId() == R.id.left) cameraManager.controlDir(PTZ.Left);
            else if(view.getId() == R.id.right) cameraManager.controlDir(PTZ.Right);
        }
        if(motionEvent.getAction() == MotionEvent.ACTION_UP) cameraManager.controlDir(PTZ.Stop);

        return true;
    }
}
