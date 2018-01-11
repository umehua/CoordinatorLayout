package com.android.launcher3.theme;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.extension.utils.LogUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ThemeStoreViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    private final String TAG = ThemeStoreViewBehavior.class.getSimpleName();

    private Integer MAX_BOTTOM_DISTANCE = null;
    private Integer MIN_BOTTOM_DISTANCE = null;

    public ThemeStoreViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        offsetChildAsNeeded(parent, child, dependency);
        return false;
    }

    private void offsetChildAsNeeded(CoordinatorLayout parent, View child, View dependency) {
        final CoordinatorLayout.Behavior behavior =
                ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
        if (behavior instanceof AppBarLayout.Behavior) {
            // Offset the child, pinning it to the bottom the header-dependency, maintaining
            // any vertical gap and overlap
            final AppBarLayout.Behavior ablBehavior = (AppBarLayout.Behavior) behavior;
            final int mOffsetDelta = getObjectIntField(AppBarLayout.Behavior.class, ablBehavior, "mOffsetDelta");
            Class<?> clzHeaderScrollingViewBehavior = getClassByName("android.support.design.widget.HeaderScrollingViewBehavior");
            final int offset = (dependency.getBottom() - child.getTop())
                    + mOffsetDelta
                    + getObjectIntMethod(clzHeaderScrollingViewBehavior, this, "getVerticalLayoutGap", null)
                    - getObjectIntMethod(clzHeaderScrollingViewBehavior, this, "getOverlapPixelsForOffset", new Class[] {View.class}, dependency);

            // android坐标的原点在左上角，往下y值增加，往右x值增加
            // child.getTop()是指child的最顶端离parent最顶端的距离, child.getBottom()指child最底端距离parent最顶端的距离.
            // dependency是AppBarLayout，当折叠AppBarLayout时，实际上相当于AppBarLayout占的整个矩形平面往上移动，而里面的toolbar则往下移动，因为toolbar总要显示在最顶端，如果它往上移动，那就不见了。
            // 实际上AppBarLayout不能完全折叠，最后还剩下约一个toolbar的高度(因为存在padding,margin这类距离)

            // 目标：让toolbar的文字折叠时显示黑色，展开时显示白色。
            // 初始状态为展开，展开时AppBar.getBottom()值为a, toolbar.getBottom()值为b,因为toolbar最后会移动到AppBar的最底端，所以折叠时， toolbar.getBottom()值为a，也就是展开折叠，toolbar.getBottom()的值从b变为a.
            Toolbar toolbar = (Toolbar)dependency.findViewById(R.id.toolbar);
            if (MAX_BOTTOM_DISTANCE == null) {
                MAX_BOTTOM_DISTANCE = dependency.getBottom();
                MIN_BOTTOM_DISTANCE = toolbar.getBottom();
            }
            float percent = (toolbar.getBottom() - MIN_BOTTOM_DISTANCE) * 1.0f / (MAX_BOTTOM_DISTANCE - MIN_BOTTOM_DISTANCE);
            int colorByte = (int)(0xff- percent * (0xff - 0x4e));
            long color = Long.parseLong(String.format("ff%x%x%x", colorByte, colorByte, colorByte), 16);

            ((TextView)toolbar.findViewById(R.id.title)).setTextColor((int)color);
            ((TextView)toolbar.findViewById(R.id.theme)).setTextColor((int)color);
            ((TextView)toolbar.findViewById(R.id.wallpaper)).setTextColor((int)color);

            ViewCompat.offsetTopAndBottom(child, offset);
        }
    }

    private Integer getObjectIntField(Class clz, Object object, String fieldName) {
        try {
            Field field = clz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Integer)field.get(object);
        } catch (Exception e) {
            LogUtil.d(TAG, "getObjectIntField()", e);
        }

        return null;
    }

    private Integer getObjectIntMethod(Class clz, Object object, String methodName, Class<?> parameterTypes[], Object... args) {
        try {
            Method method = null;
            if (parameterTypes == null) {
                method = clz.getDeclaredMethod(methodName);
            } else {
                method = clz.getDeclaredMethod(methodName, parameterTypes);
            }
            method.setAccessible(true);
            return (Integer)method.invoke(object, args);
        } catch (Exception e) {
            LogUtil.d(TAG, "getObjectIntField()", e);
        }

        return null;
    }

    private Class getClassByName(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            LogUtil.d(TAG, "getClassByName() "+ name+ " failed !", e);
        }

        return null;
    }
}
