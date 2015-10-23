package recyclerview.in.exoplayer.exoplayerinrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;


/**
 * 綺麗な正方形に表示できるようカスタマイズした特殊な{@link SquareImageView SquareCommonButton}。
 */
public class SquareImageView extends ImageButton {

    /**
     * {@inheritDoc}
     */
    public SquareImageView(Context context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
