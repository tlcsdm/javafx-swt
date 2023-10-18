module javafx.swt {
    requires transitive static org.eclipse.swt.win32.win32.x86_64;
    requires javafx.base;
    requires javafx.graphics;

    exports javafx.embed.swt;
}
