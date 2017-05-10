package com.np_app.paul.organizer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 10.05.2017.
 */

public class Kamera extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = Kamera.class.getSimpleName();

    private CameraManager manager;
    private SurfaceHolder holder;
    private String cameraId;
    private CameraDevice camera;
    private CameraCaptureSession activeSession;

    /**
     * Benutzeroberfläche erstellen und vorhandene Kameras nach Rückseitenkamera durchsuchen
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kamera);
        SurfaceView view = (SurfaceView) findViewById(R.id.surface);
        holder = view.getHolder();

        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        cameraId = null;
        Size[] sizes = null;

        try {
            String[] ids = manager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics cc = manager.getCameraCharacteristics(id);
                Log.d(TAG, id + ": " + cc.toString());

                if (cc.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    // Größe bestimmen
                    StreamConfigurationMap configs = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    sizes = configs.getOutputSizes(SurfaceHolder.class);
                }

            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "getCameraIdList() oder getCameraCharacteristics()", e);
        }

        if ((cameraId == null) || sizes == null) {
            Log.d(TAG, "keine passende Kamera gefunden");
            finish();
        } else {
            final int width = sizes[0].getWidth();
            final int height = sizes[0].getHeight();
            holder.setFixedSize(width, height);
        }


    }


    protected void onPause() {
        super.onPause();
        if (camera != null) {
            if (activeSession != null) {
                activeSession.close();
                activeSession = null;
            }
            camera.close();
            camera = null;
        }
        holder.removeCallback(this);
        Log.d(TAG, "onPause()");
    }

    protected void onResume() {
        super.onResume();
        holder.addCallback(this);
        Log.d(TAG, "onResume()");
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void openCamera() {

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {


                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Kamera.this.camera = camera;
                    createPreviewCaptureSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, null);
        } catch (Exception e){
            Log.e(TAG, "openCamera()",e);
        }

    }


    private void createPreviewCaptureSession(){

        List<Surface> outputs = new ArrayList<>();
        outputs.add(holder.getSurface());
        try {
            final CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(holder.getSurface());
            camera.createCaptureSession(
                    outputs, new CameraCaptureSession.StateCallback(){

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try{
                                session.setRepeatingRequest(builder.build(),null,null);
                                Kamera.this.activeSession = session;
                            }catch(CameraAccessException e){
                                Log.e(TAG,"capture()",e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG,"onConfigureFailed()");
                        }
                    },null);

        }catch(CameraAccessException e){
            Log.e(TAG, "capture()",e);
        }



    }


















}
