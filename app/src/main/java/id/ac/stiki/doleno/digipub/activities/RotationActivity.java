package id.ac.stiki.doleno.digipub.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import id.ac.stiki.doleno.digipub.OrientationConsumer;
import id.ac.stiki.doleno.digipub.OrientationReporter;
import id.ac.stiki.doleno.digipub.R;

public class RotationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final TextView azimuthView = findViewById(R.id.azimuth);
        final TextView pitchView = findViewById(R.id.pitch);
        final TextView rollView = findViewById(R.id.roll);

        getLifecycle().addObserver(new OrientationReporter(this, new OrientationConsumer() {
            @Override
            public void accept(float azimuth, float pitch, float roll) {
                azimuthView.setText("azimuth: " + azimuth);
                pitchView.setText("pitch: " + pitch);
                rollView.setText("roll: " + roll);
            }
        }));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}