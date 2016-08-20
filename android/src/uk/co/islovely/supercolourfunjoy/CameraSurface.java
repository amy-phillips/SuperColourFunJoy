package uk.co.islovely.supercolourfunjoy;
/*
 * Copyright 2012 Johnny Lish (johnnyoneeyed@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */


        import java.io.ByteArrayOutputStream;
        import java.io.IOException;
        import java.util.List;
        import java.util.Vector;

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Canvas;
        import android.graphics.ImageFormat;
        import android.graphics.Paint;
        import android.graphics.PorterDuff;
        import android.graphics.PorterDuffColorFilter;
        import android.graphics.Rect;
        import android.graphics.YuvImage;
        import android.hardware.Camera;
        import android.util.Log;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;

        import com.badlogic.gdx.graphics.Color;

public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    private Vector<PorterDuffColorFilter> filters = new Vector<PorterDuffColorFilter>();

    public CameraSurface( Context context ) {
        super( context );

        setWillNotDraw(false);

        // We're implementing the Callback interface and want to get notified
        // about certain surface events.
        getHolder().addCallback( this );
        // We're changing the surface to a PUSH surface, meaning we're receiving
        // all buffer data from another component - the camera, in this case.
        getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );

    }

    private int findFrontFacingCamera() {

        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return 0;
    }

    private boolean safeCameraOpen() {
        boolean qOpened = false;


        try {
            releaseCameraAndPreview();
            camera = Camera.open(findFrontFacingCamera());
            qOpened = (camera != null);
        } catch (Exception e) {
            Log.e("CameraSurface", "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        //mPreview.setCamera(null);
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void surfaceCreated( SurfaceHolder holder ) {
        // Once the surface is created, simply open a handle to the camera hardware.
        safeCameraOpen();
    }

    private byte[] cameraFrame;
    private Camera.Size previewSize;

    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        // This method is called when the surface changes, e.g. when it's size is set.
        // We use the opportunity to initialize the camera preview display dimensions.

        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

        // You need to choose the most appropriate previewSize for your app
        previewSize = previewSizes.get(0);

        parameters.setPreviewSize(previewSize.width, previewSize.height);
        parameters.setColorEffect(android.hardware.Camera.Parameters.EFFECT_MONO);
        //parameters.setPreviewFormat(ImageFormat.RGB_565); TODO optimisation???

        camera.setParameters(parameters);

        int dataBufferSize=(int)(previewSize.height*previewSize.width*
                (ImageFormat.getBitsPerPixel(camera.getParameters().getPreviewFormat())/8.0));
        camera.addCallbackBuffer(new byte[dataBufferSize]);
        camera.addCallbackBuffer(new byte[dataBufferSize]);
        camera.addCallbackBuffer(new byte[dataBufferSize]);
        camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            private long timestamp=0;
            public synchronized void onPreviewFrame(byte[] data, Camera camera) {
                Log.v("CameraTest","Time Gap = "+(System.currentTimeMillis()-timestamp));

                // stash for drawing later
                cameraFrame = data;

                timestamp=System.currentTimeMillis();
                try{
                    camera.addCallbackBuffer(data);
                }catch (Exception e) {
                    Log.e("CameraTest", "addCallbackBuffer error");
                    return;
                }
                return;
            }
        });

        // We also assign the preview display to this surface...
        try {
            //camera.setPreviewDisplay( holder );
            camera.setPreviewDisplay( null );
        } catch( IOException e ) {
            e.printStackTrace();
        }

    }

    private ByteArrayOutputStream baos;
    private YuvImage yuvimage;
    private byte[] jdata;
    private Bitmap previewBmp;
    private Paint paint = new Paint();

    @Override //from SurfaceView
    public void onDraw(Canvas canvas) {
        // wait for data
        if(cameraFrame == null) {
            invalidate(); //to call ondraw again
            return;
        }

        baos = new ByteArrayOutputStream();
        yuvimage=new YuvImage(cameraFrame, ImageFormat.NV21, previewSize.width, previewSize.height, null);

        yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos); //width and height of the screen
        jdata = baos.toByteArray();

        for(int i=0; i<filters.size(); ++i) {
            paint.setColorFilter(filters.get(i));
        }


        previewBmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);

        canvas.drawBitmap(previewBmp , 0, 0, paint);
        invalidate(); //to call ondraw again
    }

    public void surfaceDestroyed( SurfaceHolder holder ) {
        // Once the surface gets destroyed, we stop the preview mode and release
        // the whole camera since we no longer need it.
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public Camera getCamera() {
        return camera;
    }

    public void ThrowPaint(int x, int y, Color fillColour) {
        int srcC = previewBmp.getPixel(x,y);

        filters.add(0, new PorterDuffColorFilter(srcC, PorterDuff.Mode.ADD));
        filters.setSize(1);
    }

}