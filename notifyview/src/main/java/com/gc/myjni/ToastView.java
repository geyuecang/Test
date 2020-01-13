package com.gc.myjni;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * @author ge
 * @Des 全局消息弹窗
 */
public class ToastView extends LinearLayout {
    private Context mContext;
    private LayoutInflater mInflater;
    private final static int ANIM_DURATION = 600;//动画时长
    private final static int DISMISS_AFTER_TOUCH = 3000;//用户手指离开时若是完全显示状态，3秒后消失弹窗
    private final static int DISMISS_NOMAL = 5000;//弹窗显示时间
    private int NOTIFY_HEIGHT;//弹窗高度
    private static final int MARGIN_TOP = DensityUtil.dp2px(10);
    private int statusBarHeight;
    private ObjectAnimator animator;
    private ObjectAnimator animator1;
    private float currentTransY;
    private OnDismissListener listener;
    private View view;//弹窗UI
    private String className;
    private boolean isClickable;
    private int mTouchSlop;

    public ToastView(Context context) {
        this(context, null);
    }

    public ToastView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private float lastX;
    private float lastY;
    private boolean isMoved = false;

    public ToastView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        NOTIFY_HEIGHT = DensityUtil.dp2px(100);
        statusBarHeight = DisplayUtils.getStatusBarHeight(mContext);
        mInflater = LayoutInflater.from(context);
        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMoved = false;
                lastX = event.getRawX();
                lastY = event.getRawY();
                currentTransY = getTranslationY();
                mHandler.removeMessages(MSG_TIMER);//用户点触时停止计时
                Log.d("ontouch", "ACTION_DOWN" + "lastX==" + lastX + "lastY===" + lastY);
                break;
            case MotionEvent.ACTION_MOVE:
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                float tranY = lastY - rawY;
                if (Math.abs(rawX - lastX) > mTouchSlop || Math.abs(rawY - lastY) > mTouchSlop) {
                    isMoved = true;
                }
                //设定下边界
                if (getTranslationY() >= statusBarHeight && tranY <= 0) {
                    setTranslationY(statusBarHeight);
                    return true;
                }
                setTranslationY(currentTransY - tranY);

                Log.d("ontouch", "ACTION_MOVE" + "rawX==" + rawX + "rawY===" + rawY + "viewRawY==" + ToastView.this.getTranslationY());
                break;
            case MotionEvent.ACTION_UP:
                Log.d("ontouch", "ACTION_UP" + "rawX==" + event.getRawX() + "rawY===" + event.getRawY());
                //手动退出一半
                if (getTranslationY() < currentTransY) {
                    float duration = Math.abs(getTranslationY()) / (NOTIFY_HEIGHT + statusBarHeight) * ANIM_DURATION;
                    animDismiss((int) getTranslationY(), -NOTIFY_HEIGHT, (int) duration);
                } else {
                    //用户触碰了view，等待3秒消失
                    startTimer(DISMISS_AFTER_TOUCH);
                }

                if (!isMoved && isClickable) {
                    onClick();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 开始计时，等到计时结束时执行小时动画
     *
     * @param timeDismissMax
     */
    private void startTimer(int timeDismissMax) {
        mHandler.removeMessages(MSG_TIMER);
        this.timeDismissMax = timeDismissMax;
        timerCurrent = 0;
        mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
    }

    /**
     * 每次弹窗时设置数据，在此处可以通过改变NOTIFY_HEIGHT的值来改变弹窗高度
     * @param msg
     */
    private void setData(String msg) {
        //高度可以动态设置
        NOTIFY_HEIGHT = DensityUtil.dp2px(100);
        view = mInflater.inflate(R.layout.view_muti_muti, null);
        new NotifyMutiMuti(view, msg);
        //设置弹窗距离屏幕左右的margin
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, NOTIFY_HEIGHT);
        layoutParams.leftMargin = DensityUtil.dp2px(20);
        layoutParams.rightMargin = DensityUtil.dp2px(20);
        addView(view, layoutParams);
    }

    /**
     * 点击相应事件
     */
    public void onClick() {
//        Intent intent = new Intent();
//        intent.setClassName("com.kongfz.app", className);
//        intent.putExtra("contactId", String.valueOf(data.getSendId()));
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("receiverNickName", String.valueOf(data.getNickName()));
//        getContext().startActivity(intent);
    }

    private Activity mActivity;

    /**
     * 展示头部消息提示弹窗
     *
     * @param activity
     * @param bean
     */
    public void show(Activity activity, String bean) {
        mActivity = activity;
        setData(bean);
        findTopView(activity).addView(this, ViewGroup.LayoutParams.MATCH_PARENT, NOTIFY_HEIGHT);
        startShowAnim();
        startTimer(DISMISS_NOMAL);
    }

    /**
     * 用于在退出activity时隐藏view
     *
     * @return
     */
    public Activity getActivity() {
        return mActivity;
    }

    private static final int MSG_TIMER = 1;
    private int timerCurrent;
    private int timeDismissMax = DISMISS_NOMAL;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIMER:
                    timerCurrent += 1000;
                    if (timerCurrent == timeDismissMax) {
                        mHandler.removeMessages(MSG_TIMER);
                        animDismiss(statusBarHeight, -NOTIFY_HEIGHT, ANIM_DURATION);
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 还原状态
     */
    public void reset() {
        if (animator != null) {
            animator.cancel();
        }
        if (animator1 != null) {
            animator1.cancel();
        }
        timerCurrent = 0;//计时清 0
        timeDismissMax = DISMISS_NOMAL;
        mHandler.removeMessages(MSG_TIMER);
        removeAllViews();
        mActivity = null;
    }

    /**
     * 删除view时调用
     */
    public void recycle() {
        reset();
        if (listener != null) {
            listener.onDismiss(this);
        }
    }

    private void startShowAnim() {
        animator = ObjectAnimator.ofFloat(this, "translationY", -NOTIFY_HEIGHT, statusBarHeight);
        animator.setDuration(ANIM_DURATION);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) {
                    listener.onCompleteShow(ToastView.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void animDismiss(int from, int to, int duration) {
        Log.d("animator", "animDismiss---" + "from==" + from + "to===" + to + "duration===" + duration);
        animator1 = ObjectAnimator.ofFloat(this, "translationY", from, to);
        animator1.setDuration(duration);
        animator1.start();
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                detachView();
                //TODO REMOVE
                NotifyHelper.removeShowing(ToastView.this);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });
    }

    /**
     * 移除HeaderToast  (一定要在动画结束的时候移除,不然下次进来的时候由于wm里边已经有控件了，所以会导致卡死)
     * 在动画接收后或者activity的onStop生命周期调用
     */
    public void detachView() {
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(this);
            recycle();
        }
    }

    /**
     * 获取decoview的framlayout
     */
    private ViewGroup findTopView(Activity activity) {
        View view = activity.findViewById(Window.ID_ANDROID_CONTENT);
        FrameLayout frameLayout = null;
        while (view != null) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                view = (View) parent;
            } else {
                view = null;
            }
            if (parent instanceof FrameLayout) {
                frameLayout = (FrameLayout) parent;
            }
        }
        return frameLayout;
    }

    public interface OnDismissListener {
        void onDismiss(ToastView view);

        void onCompleteShow(ToastView view);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.listener = listener;
    }

}
