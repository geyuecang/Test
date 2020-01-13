package com.gc.myjni;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 通知弹窗管理类
 */
public class NotifyHelper {

    private static ViewQueue<ToastView> viewQueue;//空闲的view,采用复用机制
    private static List<ToastView> showingViews;//正在展示的view，用作清除用

    private NotifyHelper() {
        viewQueue = new ViewQueue<>();
        showingViews = new ArrayList<>();
    }

    private static class SingletonInner {
        private static NotifyHelper instance = new NotifyHelper();
    }

    public static NotifyHelper getInstance() {
        return SingletonInner.instance;
    }

    public void show(Activity activity, String bean) {
        if (activity == null) return;
        ToastView toastView = viewQueue.pop();
        if (toastView == null) {
            toastView = new ToastView(activity.getApplicationContext());
            toastView.setOnDismissListener(new ToastView.OnDismissListener() {
                @Override
                public void onDismiss(ToastView view) {
                    viewQueue.put(view);
                }

                @Override
                public void onCompleteShow(ToastView view) {
                    removeOthers(view);
                }
            });
            Log.d("ToastView", "又创建了一个");
        }
        showingViews.add(toastView);
        toastView.show(activity, bean);
    }

    /**
     * 删除显示view
     * @param view
     */
    public static void removeShowing(ToastView view) {
        if (showingViews.contains(view)) {
            showingViews.remove(view);
        }
    }

    /**
     * 当activity进入不可见状态时隐藏提示
     *
     * @param activity
     */
    public static void hide(Activity activity) {
        try {
            if (showingViews == null) return;
            Iterator<ToastView> iterator = showingViews.iterator();
            while (iterator.hasNext()) {
                ToastView next = iterator.next();
                if (activity == next.getActivity()) {
                    next.detachView();
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 当新的弹窗完全显示之后，删除掉被挡住的view
     */
    public static void removeOthers(ToastView curr) {
        try {
            if (showingViews == null) return;
            Iterator<ToastView> iterator = showingViews.iterator();
            while (iterator.hasNext()) {
                ToastView next = iterator.next();
                if (next != curr) {
                    next.detachView();
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
