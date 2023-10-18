module javafx.swt {
    requires static org.eclipse.swt.win32.win32.x86_64;
    requires static org.eclipse.swt.gtk.linux.ppc64le;
    requires static org.eclipse.swt.cocoa.macosx.x86_64;
    requires javafx.base;
    requires javafx.graphics;

    exports javafx.embed.swt;
}
