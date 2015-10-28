package guardanis.applock.pin;

public class PINItemAnimator extends Thread {

    public enum ItemAnimationDirection {
        IN, OUT;
    }

    private static final float MIN_SIZE_PERCENT = .2f;
    private static final float ANIMATION_EXPONENTIAL_FACTOR = 3.5f;
    private static final int ANIMATION_DURATION = 350;
    private static final int UPDATE_RATE = 25;

    private PINInputView inputView;
    private PINItemView itemView;
    private ItemAnimationDirection animationDirection;

    private long startTime;
    private boolean canceled = false;

    public PINItemAnimator(PINInputView inputView, PINItemView itemView, ItemAnimationDirection animationDirection) {
        this.inputView = inputView;
        this.itemView = itemView;
        this.animationDirection = animationDirection;
    }

    @Override
    public void run() {
        this.startTime = System.currentTimeMillis();

        try{
            if(animationDirection == ItemAnimationDirection.IN)
                animateIn();
            else animateOut();
        }
        catch(Exception e){ e.printStackTrace(); }
    }

    private void animateIn() throws Exception {
        float percent = MIN_SIZE_PERCENT;
        while(percent < 1 && !canceled){
            percent = Math.min(MIN_SIZE_PERCENT + calculatePercentComplete(), 1);
            updateView(percent);
            Thread.sleep(UPDATE_RATE);
        }
    }

    private void animateOut() throws Exception {
        float percent = 1 - calculatePercentComplete();
        while(MIN_SIZE_PERCENT < percent && !canceled){
            percent = Math.max(1 - calculatePercentComplete(), MIN_SIZE_PERCENT);
            updateView(percent);
            Thread.sleep(UPDATE_RATE);
        }
    }

    private float calculatePercentComplete() {
        return (float) Math.pow(((float) (System.currentTimeMillis() - startTime)) / ANIMATION_DURATION, ANIMATION_EXPONENTIAL_FACTOR);
    }

    private void updateView(final float percent) {
        inputView.post(new Runnable() {
            public void run() {
                itemView.onAnimationUpdate(percent);
                inputView.invalidate();
            }
        });
    }

    public void cancel() {
        this.canceled = true;
    }

}
