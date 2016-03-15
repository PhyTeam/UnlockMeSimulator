/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.simulator;

import java.util.Map;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 *
 * @author bbphuc
 */
public class Block extends Parent implements IBlock{
    public static final int SIZE = 100;
    private double startDragX;
    private double startDragY;
    
    private final double correctX;
    private final double correctY;
    
    private int x;
    private int y;
    private int index;
    private int length;
    private boolean isHorizontal;
    
    @Override
    public void setXY(int x, int y){
        this.x = x;
        this.y = y;
    }

    private final Shape pieceStroke;
    private final Shape pieceClip;
    private final ImageView imageView = new ImageView();
    private Point2D dragAnchor;

    public final Point2D getPosition(int x, int y){
        // Height
        double cx = y * 100 + SIZE / 2.0f * ((isHorizontal)? 1 : length );
        double cy = x * 100 + SIZE / 2.0f * ((isHorizontal)? length : 1);
        return new Point2D(cx, cy);
    }
    
    public Block(Map<Integer,Image> res, int cx, int cy, int index , final int length, final boolean isHorizontal,
                 final double deskWidth, final double deskHeight) {
        this.length = length;
        this.isHorizontal = isHorizontal;
        this.index = index;
        
        correctX = cx;
        correctY = cy;
        
        this.x = cx;
        this.y = cy;
        
        Point2D pt = getPosition(x, y);
        this.setTranslateX(SIZE * cy + 5);
        this.setTranslateY(SIZE * cx + 5);
        // Setup image
        Image image;
        if(index == -1)
            image = res.get(-1);
        else image = res.get(0);
        // configure clip
        pieceClip = createPiece();
        pieceClip.setFill(Color.WHITE);
        pieceClip.setStroke(null);
        // add a stroke
        pieceStroke = createPiece();
        pieceStroke.setFill(null);
        pieceStroke.setStroke(Color.BLACK);
        // create image view
        imageView.setImage(image);
        imageView.setClip(pieceClip);
        setFocusTraversable(true);
        getChildren().addAll(imageView, pieceStroke);
        // turn on caching so the jigsaw piece is fasr to draw when dragging
        setCache(true);
        // start in inactive state
        //setInactive();
        // add listeners to support dragging
        setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                toFront();
                startDragX = getTranslateX();
                startDragY = getTranslateY();
                dragAnchor = new Point2D(me.getSceneX(), me.getSceneY());
            }
        });
        setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (getTranslateX() < (10) && getTranslateX() > (- 10) &&
                    getTranslateY() < (10) && getTranslateY() > (- 10)) {
                    setTranslateX(0);
                    setTranslateY(0);
                    //setInactive();
                }
            }
        });
        setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                double newTranslateX = startDragX
                                        + me.getSceneX() - dragAnchor.getX();
                double newTranslateY = startDragY
                                        + me.getSceneY() - dragAnchor.getY();
                double minTranslateX =  0f;
                double maxTranslateX = (deskWidth) - Block.SIZE * length + 20f;//+ 50f ) - correctX;
                double minTranslateY =  0f;
                double maxTranslateY = (deskHeight) - Block.SIZE * length + 20f; // - Block.SIZE + 70f ) - correctY;
                if ((newTranslateX> minTranslateX ) &&
                        (newTranslateX< maxTranslateX) &&
                        (newTranslateY> minTranslateY) &&
                        (newTranslateY< maxTranslateY)) {
                    if(!isHorizontal) 
                        setTranslateX(newTranslateX);
                    else 
                        setTranslateY(newTranslateY);
                }
                
            }
        });
     

    }

    private Shape createPiece() {
        Shape shape = createPieceRectangle();
        shape.setTranslateX(correctX);
        shape.setTranslateY(correctY);
        shape.setLayoutX(50f);
        shape.setLayoutY(50f);
        return shape;
        
    }

    private Rectangle createPieceRectangle() {
        Rectangle rec = new Rectangle();
        rec.setX(-50);
        rec.setY(-50);
        rec.setWidth(SIZE * ((isHorizontal) ? 1 : this.length) - 15f);
        rec.setHeight(SIZE * ((isHorizontal)? this.length : 1) - 15f);
        return rec;
    }


    public void setActive() {
        setDisable(false);
        setEffect(new DropShadow());
        toFront();
    }

    public void setInactive() {
        setEffect(null);
        setDisable(true);
        toBack();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getLength() {
        return length;
    }
}
