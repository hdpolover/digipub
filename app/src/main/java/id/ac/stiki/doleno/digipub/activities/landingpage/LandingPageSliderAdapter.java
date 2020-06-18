package id.ac.stiki.doleno.digipub.activities.landingpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import id.ac.stiki.doleno.digipub.R;

public class LandingPageSliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public LandingPageSliderAdapter(Context context) {
        this.context = context;
    }

    public int[] slide_images = {
            R.drawable.chill,
            R.drawable.chill,
            R.drawable.chill,
            R.drawable.chill
    };

    public String[] slide_headings = {
            "Welcome to YBB",
            "Connecting",
            "Communicating",
            "Contributing"
    };

    public String[] slide_descs = {
            "A foundation that focuses on all instruments regarding development and empowerment of youth to prepare excellent future leaders",
            "Finding global youths all over the world",
            "Building strong international networking",
            "Making real actions for the better future"
    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_landing_page, container, false);

        ImageView lpImageIv = view.findViewById(R.id.landingPageIv);
        TextView lpHeadingTv = view.findViewById(R.id.lpHeadingTv);
        TextView lpDescTv = view.findViewById(R.id.lpDescTv);

        lpImageIv.setImageResource(slide_images[position]);
        lpHeadingTv.setText(slide_headings[position]);
        lpDescTv.setText(slide_descs[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
