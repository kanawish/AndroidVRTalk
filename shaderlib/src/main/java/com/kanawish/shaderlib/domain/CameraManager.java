package com.kanawish.shaderlib.domain;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * Very simple 'manager' here to control where the camera is going.
 *
 * Not even bothering splitting the model from the business logic yet.
 *
 */
public class CameraManager {

    public static class CameraData {
        private float[] cameraRotation = {0,0,0};
        private float[] cameraTranslation = {0,0,0};

        public float[] getCameraRotation() {
            return cameraRotation;
        }

        public void setCameraRotation(float[] cameraRotation) {
            this.cameraRotation = cameraRotation;
        }

        public float[] getCameraTranslation() {
            return cameraTranslation;
        }

        public void setCameraTranslation(float[] cameraTranslation) {
            this.cameraTranslation = cameraTranslation;
        }
    }

    // The state of the 'global' camera.
    private CameraData cameraData = new CameraData();

    // The camera data bus.
    private final Subject<CameraData, CameraData> cameraBus;
    {
        // Behavior will provide latest value and subsequent changes to subscribers.
        BehaviorSubject<CameraData> subject = BehaviorSubject.create();
        cameraBus = subject.toSerialized();
    }

    // Subscribe to the camera data bus via this method.
    public Observable<CameraData> cameraDataObservable() {
        return cameraBus;
    }

    private void publishCameraData() {
        cameraBus.onNext(cameraData);
    }

    // Use this method to apply an incremental change to the camera position.
    public void applyDelta(CameraData delta) {
        float[] cameraRotation = cameraData.getCameraRotation();
        for (int i = 0; i < cameraRotation.length; i++) cameraRotation[i] += delta.getCameraRotation()[i];

        float[] cameraTranslation = cameraData.getCameraTranslation();
        for (int i = 0; i < cameraTranslation.length; i++) cameraTranslation[i] += delta.getCameraTranslation()[i];

        publishCameraData();
    }

    public void applyTranslationDelta(float x, float y, float z) {
        float[] translation = cameraData.getCameraTranslation();
        translation[0] += x; translation[1] += y; translation[2] += z;
        publishCameraData();
    }

    public void applyRotationDelta(float x, float y, float z) {
        float[] rotation = cameraData.getCameraRotation();
        rotation[0] += x; rotation[1] += y; rotation[2] += z;
        publishCameraData();
    }

    // Reset everything to 0.
    public void resetCamera() {
        cameraData = new CameraData();
        publishCameraData();
    }

    // Reset to provided position.
    public void resetCamera(CameraData resetData) {
        cameraData = resetData;
        publishCameraData();
    }

    public void resetTranslation() {
        cameraData.setCameraTranslation(new float[]{0, 0, 0});
        publishCameraData();
    }

    public void resetRotation() {
        cameraData.setCameraRotation(new float[]{0, 0, 0});
        publishCameraData();
    }
}
