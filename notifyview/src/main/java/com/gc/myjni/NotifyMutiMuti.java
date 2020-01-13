package com.gc.myjni;


import android.view.View;
import android.widget.TextView;

public class NotifyMutiMuti {
    private TextView tv_content;

    public NotifyMutiMuti(View view, String msg) {
        tv_content = view.findViewById(R.id.tv_content);
        tv_content.setText(msg);
    }

}
