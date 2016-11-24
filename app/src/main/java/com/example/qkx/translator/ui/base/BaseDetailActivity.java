package com.example.qkx.translator.ui.base;

import android.support.v7.app.ActionBar;

public abstract class BaseDetailActivity extends BaseToolbarActivity {
    @Override
    public boolean canBack() {
        return true;
    }

    protected void setTitle(String title) {
        ActionBar localActionBar = getSupportActionBar();
        if (localActionBar != null) {
            localActionBar.setTitle(title);
        }
    }
}

