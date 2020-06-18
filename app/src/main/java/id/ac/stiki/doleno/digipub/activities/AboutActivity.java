package id.ac.stiki.doleno.digipub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import id.ac.stiki.doleno.digipub.BuildConfig;
import id.ac.stiki.doleno.digipub.R;

import java.util.Calendar;

public class AboutActivity extends AppCompatActivity {

    ImageView logoIv;
    TextView versionTv, contactUsBtn, rateAppBtn, creditsBtn, copyrightBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        logoIv = findViewById(R.id.logoIv);
        versionTv = findViewById(R.id.appVersionTv);
        contactUsBtn = findViewById(R.id.contactUsBtn);
        rateAppBtn = findViewById(R.id.rateAppBtn);
        creditsBtn = findViewById(R.id.creditsBtn);
        copyrightBtn = findViewById(R.id.copyrightTv);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("About");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Glide.with(getApplicationContext()).load(R.drawable.digipub_logo)
                .centerCrop()
                .into(logoIv);

        String versionName = BuildConfig.VERSION_NAME;
        versionTv.setText("Version " + versionName);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        copyrightBtn.setText(getResources().getString(R.string.copyright) + " " + year + " " + getResources().getString(R.string.polover));

        contactUsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
            }
        });

        rateAppBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
            }
        });

        creditsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}