package nightsky.util.object;


public class Drag {
    private float x, y;
    private boolean dragging;
    private float deltaX, deltaY;

    public Drag(float x, float y) {
        this.x = x;
        this.y = y;
        this.dragging = false;
    }

    public void onDraw(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - deltaX;
            y = mouseY - deltaY;
        }
    }

    public void onClick(int mouseX, int mouseY, int button, boolean canDrag) {
        if (button == 0 && canDrag) {
            dragging = true;
            deltaX = mouseX - x;
            deltaY = mouseY - y;
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
