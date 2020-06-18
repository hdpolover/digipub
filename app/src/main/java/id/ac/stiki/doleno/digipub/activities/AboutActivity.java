package id.ac.stiki.doleno.digipub.activities;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
                rateApp();
            }
        });

        creditsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreditsDialog();
            }
        });
    }

    void showCreditsDialog() {
        final MaterialAlertDialogBuilder creditDialog = new MaterialAlertDialogBuilder(this);
        creditDialog.setTitle(getResources().getString(R.string.credits_title))
                .setMessage(getResources().getString(R.string.credits_content))
                .setPositiveButton(getResources().getString(R.string.ok_credits), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    /*
     * Start with rating the app
     * Determine if the Play Store is installed on the device
     *
     * */
    public void rateApp()
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}