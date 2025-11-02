package nightsky.util;

public class DragUtil {
    private float x;
    private float y;
    private float startX;
    private float startY;
    private boolean dragging;

    public DragUtil(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void onClick(int mouseX, int mouseY, int button, boolean canDrag) {
        if (button == 0 && canDrag) {
            dragging = true;
            startX = mouseX - x;
            startY = mouseY - y;
        }
    }

    public void onDraw(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;
        }
    }

    public void onRelease(int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }
}
