package ca.team2706.scouting.mcmergemanager.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * This is a class defining a custom canvas view that will draw a time-bar graph of when cycles
 * started and finished.
 *
 * Created by mike on 22/01/17.
 */

public class CyclesDisplayView extends View {

    public static final double MATCH_DURATION = 135.0;  // s

    private static final Paint PURPLE_FILL = new Paint();
    private static final Paint RED_FILL    = new Paint();
    private static final Paint OUTLINE_PAINT = new Paint(Color.BLACK);


    // static initializer
    static {
        PURPLE_FILL.setColor(Color.rgb(102,51,153));
        PURPLE_FILL.setStyle(Paint.Style.FILL);
        RED_FILL.setColor(Color.RED);
        RED_FILL.setStyle(Paint.Style.FILL);

        OUTLINE_PAINT.setStyle(Paint.Style.STROKE);
        OUTLINE_PAINT.setStrokeWidth(10);
    }


    private int viewWidth, viewHeight;


    private ArrayList<CycleBar> cycleBars = new ArrayList<>();

    private class CycleBar {
        private double startTime, endTime;

        private Rect backgroundRect, outlineRect;
        private Paint fillPaint;

        CycleBar(double startTime, double endTime, boolean success) {
            this.startTime = startTime;
            this.endTime = endTime;

            if(success)
                fillPaint = PURPLE_FILL;
            else
                fillPaint = RED_FILL;
        }

        /**
         * Uses the viewWidth and viewHeight to calculate the pixel of the bars.
         */
        void calcPxDims() {
            if(viewWidth == 0 || viewHeight == 0)
                return;

            double scalingFactor = viewWidth / MATCH_DURATION;

            int startx = (int) (startTime * scalingFactor);
            int endx = (int) (endTime * scalingFactor);

            backgroundRect = new Rect(startx, 0, endx, viewHeight);

            outlineRect = new Rect(startx, 0, endx, viewHeight);
        }
    }


    /** Constructor **/
    public CyclesDisplayView(Context context) {
        super(context);
        localInit();
    }

    /** Constructor **/
    public CyclesDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        localInit();
    }

    private void localInit() {

    }


    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewWidth = w;
        viewHeight = h;

        for(CycleBar cycleBar : cycleBars)
            cycleBar.calcPxDims();
    }


    /**
     * Add a cycle bar to the bar graph.
     *
     * @param startTime the time-stamp of when the cycle started [0, MATCH_DURATION].
     * @param endTime the time-stamp of when the cycle ended [0, MATCH_DURATION]. If endTime < startTime, then nothing gets drawn.
     * @param success if true, we draw the cycle bar in 2706 purple, if false, we draw the cycle bar in red.
     *                Intended for cycles that were incomplete at the end of the match, but I'm sure we
     *                can find other uses.
     */
    public void addCycle(double startTime, double endTime, boolean success) {
        if (endTime < startTime) return;

        // just so they don't draw weird, clamp them to [0, MATCH_DURATION].
        if (startTime < 0) startTime = 0;
        if (startTime > MATCH_DURATION) startTime = MATCH_DURATION;
        if (endTime < 0) endTime = 0;
        if (endTime > MATCH_DURATION) endTime = MATCH_DURATION;


        // size the bar to the length of the cycle
        CycleBar cycleBar = new CycleBar(startTime, endTime, success);

        cycleBar.calcPxDims();
        cycleBars.add(cycleBar);


        // force a re-draw of this view.
        invalidate();
    }

    /**
     * All Views must have this, it gets called by the OS every time the screen redraws.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(CycleBar cycleBar : cycleBars) {

            // draw the cycleBar
            canvas.drawRect(cycleBar.backgroundRect, cycleBar.fillPaint);

            // draw an outlineRect to the box
            canvas.drawRect(cycleBar.outlineRect, OUTLINE_PAINT);
        }
    }
}
