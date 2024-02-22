package javafx.embed.swt;

/**
 * @author unknowIfGuestInDream
 */
public enum ZoomType {

    TOWIDTH(2), TOHEIGHT(1), ORIGINAL(0), ZOOMIN(3), ZOOMOUT(4);

    private int value;

    private ZoomType(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
    }

}