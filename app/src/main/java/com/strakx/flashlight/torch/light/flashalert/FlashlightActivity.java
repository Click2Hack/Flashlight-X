package com.strakx.flashlight.torch.light.flashalert;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

public class FlashlightActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean isFlashlightOn = false;
    private CameraManager cameraManager;
    private String cameraId;
    private ImageView flashlightButton;
    private TelephonyManager telephonyManager;
    private boolean isInFlashlightMode = false;
    private Handler strobeHandler = new Handler();
    private boolean isStrobeOn = false;
    private int strobeFrequency = 100; // Default frequency in milliseconds

    private final Runnable strobeRunnable = new Runnable() {
        @Override
        public void run() {
            if (isStrobeOn) {
                toggleFlashlight();
                strobeHandler.postDelayed(this, strobeFrequency);
            } else {
                strobeHandler.removeCallbacks(this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashlight);

        flashlightButton = findViewById(R.id.flashlightButton);
        flashlightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInFlashlightMode) {
                    toggleStrobe();
                } else {
                    toggleFlashlight();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermissions()) {
                requestPermissions();
            } else {
                initialize();
            }
        } else {
            initialize();
        }
    }

    private void initialize() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initialize();
            } else {
                Toast.makeText(this, "Permissions are required to use the flashlight", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleFlashlight() {
        try {
            if (isFlashlightOn) {
                cameraManager.setTorchMode(cameraId, false);
                isFlashlightOn = false;
                flashlightButton.setImageResource(R.drawable.ic_flashlight_off);
            } else {
                cameraManager.setTorchMode(cameraId, true);
                isFlashlightOn = true;
                flashlightButton.setImageResource(R.drawable.ic_flashlight_on);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (!isInFlashlightMode) {
                        switchToFlashlightMode();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (isInFlashlightMode) {
                        switchToNormalMode();
                    }
                    break;
            }
        }
    };

    private void toggleStrobe() {
        isStrobeOn = !isStrobeOn;
        if (isStrobeOn) {
            strobeHandler.post(strobeRunnable);
        } else {
            strobeHandler.removeCallbacks(strobeRunnable);
        }
    }

    private void switchToFlashlightMode() {
        // Enter flashlight mode
        isInFlashlightMode = true;
    }

    private void switchToNormalMode() {
        // Exit flashlight mode
        isInFlashlightMode = false;
    }
}
