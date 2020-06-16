package com.polover.digipub.activities.landingpage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polover.digipub.R;
import com.polover.digipub.activities.MainActivity;

public class LandingPageActivity extends AppCompatActivity {

    private ViewPager landingPageViewPager;
    private LinearLayout dotLinearLayout;
    private Button landingBackBtn, landingNextBtn;

    private TextView[] dots;

    private int currentPage;

    LandingPageSliderAdapter landingPageSliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        landingPageViewPager = findViewById(R.id.landingPageViewPager);
        dotLinearLayout = findViewById(R.id.dotLinearLayout);
        landingNextBtn = findViewById(R.id.landingNextBtn);
        landingBackBtn = findViewById(R.id.landingBackBtn);

        landingPageSliderAdapter = new LandingPageSliderAdapter(this);

        landingPageViewPager.setAdapter(landingPageSliderAdapter);

        addDotsIndicator(0);

        landingPageViewPager.addOnPageChangeListener(onPageChangeListener);

        landingNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                landingPageViewPager.setCurrentItem(currentPage + 1);
            }
        });

        landingBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                landingPageViewPager.setCurrentItem(currentPage - 1);
            }
        });
    }

    public void addDotsIndicator(int position) {
        dots = new TextView[4];
        dotLinearLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite));

            dotLinearLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[position].setTextColor(getResources().getColor(R.color.white));
        }
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            currentPage = position;

            if (position == 0) {
                landingNextBtn.setEnabled(true);
                landingBackBtn.setEnabled(false);
                landingBackBtn.setVisibility(View.GONE);

                landingNextBtn.setText("Next");
                landingBackBtn.setText("");
            } else if (position == dots.length - 1) {
                landingNextBtn.setEnabled(true);
                landingBackBtn.setEnabled(true);
                landingBackBtn.setVisibility(View.VISIBLE);

                landingNextBtn.setText("Finish");
                landingBackBtn.setText("Back");
            } else {
                landingNextBtn.setEnabled(true);
                landingBackBtn.setEnabled(true);
                landingBackBtn.setVisibility(View.VISIBLE);

                landingNextBtn.setText("Next");
                landingBackBtn.setText("Back");
            }

            if (landingNextBtn.getText().toString().equals("Finish")) {
                landingNextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(LandingPageActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}