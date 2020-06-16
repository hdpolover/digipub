package com.polover.digipub;

public interface OrientationConsumer {
    void accept(float azimuth, float pitch, float roll);
}