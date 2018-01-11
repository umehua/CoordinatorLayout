package com.android.launcher3.theme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;

public class ThemeStore extends AppCompatActivity{

    private Toolbar mToolbar;
    private LinearLayout mContentContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme_store);

        initToolbar();
        initContent();
    }

    private void initToolbar() {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
    }

    private void initContent() {
        mContentContainer = (LinearLayout)findViewById(R.id.content_container);

        String[] categoryArr = getResources().getStringArray(R.array.ts_categories);
        for (String c : categoryArr) {
            View content = LayoutInflater.from(this).inflate(R.layout.theme_category_content, mContentContainer, false);
            TextView tvTitle = (TextView)content.findViewById(R.id.title);
            tvTitle.setText(c);
            mContentContainer.addView(content);
        }
    }
}
