package id.ac.stiki.doleno.digipub;

public interface OrientationConsumer {
    void accept(float azimuth, float pitch, float roll);
}