package javafx.embed.swt;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * FXCanvas that can be zoomed in and out and Composite of FXCanvas.
 *
 * <p><pre>${@code
 *      compositeImage = new Composite(sashForm, SWT.NONE);
 * 		compositeImage.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 * 		compositeImage.setLayout(new FillLayout());
 * 		fxCanvas = new FXCanvas(compositeImage, SWT.FILL) {
 *                        @Override
 *            public Point computeSize(int wHint, int hHint, boolean changed) {
 *              	return new Point(wHint, hHint);
 *            }
 *      };
 * 		fxCanvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 * 	...
 *
 * 	    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("example.fxml"));
 * 	    Parent fxRoot = fxmlLoader.load();
 * 	    configController = fxmlLoader.<configController>getController();
 * 	    fxZoomControl.setFxComposite(this);
 * 	    fxZoomControl.setFxScrollerPane((ScrollPane) fxRoot);
 * 	    fxZoomControl.fxCompositeChangeHandler(compositeImage);
 * 	    borderPane.setCenter(fxRoot);
 * 	    borderPane.setStyle("-fx-background-color: White;");
 * 	    Scene scene = new Scene(borderPane);
 * 	    scene.getStylesheets().add(getClass().getResource("/resource/default.css").toExternalForm());
 * 	    scene.setFill(Color.WHITE);
 * 	    fxCanvas.setScene(scene);
 * 	    fxZoomControl.getZoomChildren();
 * 		fxZoomControl.initZoomPaneActions();
 * 	    fxZoomControl.initScene();
 * 	    fxZoomControl.orignalUISize();
 * }
 * </pre>
 * </p>
 *
 * @author unknowIfGuestInDream
 */
public class FXZoomControl {

    private ScrollPane fxScrollerPane;
    private AnchorPane fxZoomPane;
    private AnchorPane fxStackPane;
    private StackPane fxsubStackPane;
    private Group fxScrollContent;

    private static final double SCALEDELTA = 1.1;
    private boolean controlKeyHold = false;
    private boolean scrollerHandled = false;
    private Composite fxComposite;

    private double iniFxHeight = 0;
    private double iniFxWidth = 0;

    private double xCurrRatio = 1;
    private double yCurrRatio = 1;
    private ZoomType fxZoomType = ZoomType.ORIGINAL;
    private EventHandler<MouseEvent> orignalEvent;
    private EventHandler<MouseEvent> widthEvent;
    private EventHandler<MouseEvent> heigthEvent;

    public FXZoomControl() {
        initializeEventHandler();
    }

    private void initializeEventHandler() {
        orignalEvent = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleZoomTypeChange(ZoomType.ORIGINAL);
            }
        };
        widthEvent = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleZoomTypeChange(ZoomType.TOWIDTH);
            }
        };
        heigthEvent = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleZoomTypeChange(ZoomType.TOHEIGHT);
            }
        };
    }

    public void orignalUISize() {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                orignalEvent.handle(null);
            }
        });
    }

    public void widthUISize() {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                widthEvent.handle(null);
            }
        });

    }

    public void heightUISize() {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                heigthEvent.handle(null);
            }
        });

    }

    public ObservableList<Node> getZoomChildren() {
        fxScrollContent = (Group) fxScrollerPane.getContent();
        fxStackPane = (AnchorPane) fxScrollContent.getChildren().get(0);
        fxStackPane.setScaleX(0.75);
        fxStackPane.setScaleY(0.75);
        Group group = (Group) fxStackPane.getChildren().get(0);
        fxsubStackPane = (StackPane) group.getChildren().get(0);
        fxZoomPane = (AnchorPane) fxsubStackPane.getChildren().get(0);
        return fxZoomPane.getChildrenUnmodifiable();
    }

    public void setFxComposite(Composite fxComposite) {
        this.fxComposite = fxComposite;
    }

    public void initZoomPaneActions() {
        fxScrollerPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.CONTROL) {
                    controlKeyHold = true;
                }

            }
        });
        fxScrollerPane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.CONTROL) {
                    controlKeyHold = false;
                }
            }
        });
        fxScrollContent.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if (controlKeyHold && !scrollerHandled) {
                    paneOnScroll(event);
                }
                scrollerHandled = false;
            }
        });
        fxScrollerPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                scrollerHandled = true;
                if (controlKeyHold) {
                    paneOnScroll(event);
                } else {
                    event.consume();
                }
            }
        });

        fxScrollerPane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                fxStackPane.setMinSize(newValue.getWidth(), newValue.getHeight());
            }
        });

        // Panning via drag....
        final ObjectProperty<Point2D> lastMouseCoordinates = new SimpleObjectProperty<>();
        fxStackPane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                lastMouseCoordinates.set(new Point2D(event.getX(), event.getY()));
            }
        });

        fxStackPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double deltaX = event.getX() - lastMouseCoordinates.get().getX();
                double extraWidth = fxStackPane.getLayoutBounds().getWidth()
                        - fxScrollerPane.getViewportBounds().getWidth();
                if (extraWidth > 0) {
                    double deltaH = deltaX * (fxScrollerPane.getHmax() - fxScrollerPane.getHmin()) / extraWidth;
                    double desiredH = fxScrollerPane.getHvalue() - deltaH;
                    fxScrollerPane.setHvalue(Math.max(0, Math.min(fxScrollerPane.getHmax(), desiredH)));
                }

                double deltaY = event.getY() - lastMouseCoordinates.get().getY();
                double extraHeight = fxStackPane.getLayoutBounds().getHeight()
                        - fxScrollerPane.getViewportBounds().getHeight();
                if (extraHeight > 0) {
                    double deltaV = deltaY * (fxScrollerPane.getHmax() - fxScrollerPane.getHmin()) / extraHeight;
                    double desiredV = fxScrollerPane.getVvalue() - deltaV;
                    fxScrollerPane.setVvalue(Math.max(0, Math.min(fxScrollerPane.getVmax(), desiredV)));
                }
            }
        });

    }

    private void paneOnScroll(ScrollEvent event) {
        if (Double.compare(event.getDeltaY(), 0) == 0) {
            return;
        }
        fxZoomType = (event.getDeltaY() > 0) ? ZoomType.ZOOMIN : ZoomType.ZOOMOUT;
        double scaleFactor = (event.getDeltaY() > 0) ? SCALEDELTA : 1 / SCALEDELTA;
        fxStackPane.setScaleX(fxStackPane.getScaleX() * scaleFactor);
        fxStackPane.setScaleY(fxStackPane.getScaleY() * scaleFactor);
        xCurrRatio = scaleFactor * xCurrRatio;
        yCurrRatio = scaleFactor * yCurrRatio;

    }

    public void fxCompositeChangeHandler(Composite fxComposite) {
        fxComposite.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (fxZoomType != ZoomType.ORIGINAL && fxZoomType != ZoomType.ZOOMIN
                        && fxZoomType != ZoomType.ZOOMOUT) {
                    double curHeight = fxComposite.getSize().y - 70.0;
                    double curWidth = fxComposite.getSize().x - 30.0;
                    double yRatio = curHeight / iniFxHeight;
                    double xRatio = curWidth / iniFxWidth;
                    if (fxZoomType == ZoomType.TOWIDTH) {
                        yRatio = xRatio;
                    } else {
                        xRatio = yRatio;
                    }
                    paneParameterSetting(xRatio, yRatio);
                }
            }
        });
    }

    public void handleZoomTypeChange(ZoomType type) {
        if (type.equals(ZoomType.ORIGINAL)) {
            paneParameterSetting(1, 1, ZoomType.ORIGINAL);
        } else if (type.equals(ZoomType.TOHEIGHT)) {
            double yRatio = (fxComposite.getSize().y - 70) / iniFxHeight;
            double xRatio = yRatio;
            paneParameterSetting(xRatio, yRatio, ZoomType.TOHEIGHT);
        } else if (type.equals(ZoomType.TOWIDTH)) {
            double xRatio = (fxComposite.getSize().x - 30) / iniFxWidth;
            double yRatio = xRatio;
            paneParameterSetting(xRatio, yRatio, ZoomType.TOWIDTH);
        }
    }

    private void paneParameterSetting(double xRatio, double yRatio) {
        // amount of scrolling in each direction in scrollContent coordinate units
        fxZoomPane.setScaleX(fxZoomPane.getScaleX() / xCurrRatio * xRatio);
        fxZoomPane.setScaleY(fxZoomPane.getScaleY() / yCurrRatio * yRatio);
        // move viewport so that old center remains in the center after the scaling
        xCurrRatio = xRatio;
        yCurrRatio = yRatio;

    }

    private void paneParameterSetting(double xRatio, double yRatio, ZoomType zoomType) {
        fxZoomPane.setScaleX(fxZoomPane.getScaleX() / xCurrRatio * xRatio);
        fxZoomPane.setScaleY(fxZoomPane.getScaleY() / yCurrRatio * yRatio);
        xCurrRatio = xRatio;
        yCurrRatio = yRatio;

        if (zoomType != null) {
            fxZoomType = zoomType;
        }
    }

    public void clearClockViewSettings() {
        fxZoomPane = null;
        fxScrollerPane = null;
        xCurrRatio = 1;
        yCurrRatio = 1;
    }

    public void setFxScrollerPane(ScrollPane fxRoot) {
        fxScrollerPane = fxRoot;
    }

    public ZoomType getZoomType() {
        return fxZoomType;
    }

    public void initScene() {
        iniFxHeight = fxZoomPane.getLayoutBounds().getHeight();
        iniFxWidth = fxZoomPane.getLayoutBounds().getWidth();

        // update to fit to the width
        double xRatio = (fxComposite.getSize().x - 30) / iniFxWidth;
        double yRatio = xRatio;
        paneParameterSetting(xRatio, yRatio, ZoomType.TOWIDTH);
    }

    public void enableClockPageVisibility(boolean visible) {
        if (fxComposite != null) {
            fxComposite.setVisible(visible);
        }
        if (fxComposite != null) {
            fxComposite.layout();
        }
    }
}
