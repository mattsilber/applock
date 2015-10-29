package com.guardanis.applock.pin;

import android.graphics.Canvas;
import android.graphics.Paint;

public class PINItemView {

    private float[] position;
    private int intendedRadius;
    private int currentRadius;
    private int smallestRadius;
    private Paint backgroundPaint;

    private float[] textPosition;
    private Paint textPaint;

    private PINItemAnimator.ItemAnimationDirection animationDirection = PINItemAnimator.ItemAnimationDirection.OUT;

    public PINItemView(float[] position, int intendedRadius, Paint textPaint, Paint backgroundPaint) {
        this.position = position;
        this.intendedRadius = intendedRadius;
        this.smallestRadius = intendedRadius / 5;
        this.currentRadius = smallestRadius;

        this.textPaint = textPaint;
        this.textPosition = new float[]{
                position[0],
                position[1] - ((textPaint.descent() + textPaint.ascent()) / 2)
        };

        this.backgroundPaint = backgroundPaint;
    }

    public void draw(Canvas canvas, String textValue) {
        canvas.drawCircle(position[0], position[1], currentRadius, backgroundPaint);
        canvas.drawText(textValue, textPosition[0], textPosition[1], textPaint);
    }

    public void setAnimationDirection(PINItemAnimator.ItemAnimationDirection animationDirection){
        this.animationDirection = animationDirection;
    }

    public void onAnimationUpdate(float percentCompleted) {
        this.currentRadius = (int) (intendedRadius * percentCompleted);
    }

    public boolean isAnimatedOut() {
        return animationDirection == PINItemAnimator.ItemAnimationDirection.OUT;
    }

}
