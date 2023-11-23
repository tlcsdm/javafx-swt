module javafx.swt {
    requires javafx.base;
    requires javafx.graphics;
    requires static org.eclipse.swt.win32.win32.x86_64;

    exports javafx.embed.swt;
}
