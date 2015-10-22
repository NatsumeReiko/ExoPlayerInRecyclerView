package recyclerview.in.exoplayer.exoplayerinrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class HeightFixedRatioFrameLayout extends FrameLayout {

    public HeightFixedRatioFrameLayout(Context context) {
        super(context);
    }

    public HeightFixedRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
