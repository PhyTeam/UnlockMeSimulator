package ai.simulator;

import java.util.ArrayList;
import java.util.List;;
import java.io.IOException;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;
import ai.simulator.GameState.Step;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;

public class UMSimulator extends Application implements IBlockFactory {
    private Timeline timeline;
    private Image bg_image;
    private Stage primaryStage;
    private Desk desk;
    private final List<Block> blocks = new ArrayList<>();
    private final Map<Integer,Image> res = new HashMap<>();
    private final String solverPath = "F:\\solver\\UnlockMe.jar";
    List<Step> lstStep = new ArrayList<>();
    File fin;
    GameState gs;
    private void init(Stage primaryStage) {
        // load puzzle image
        Image image = new Image(getClass().getResourceAsStream(
            "resource/wood-wallpaper-3.jpg"));
        Image image2 = new Image(getClass().getResourceAsStream(
                "resource/wood _texture3173.jpg"));
        res.put(-1, image2);
        res.put(0, image);
        this.bg_image = image;
        
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        int numOfColumns =  6;//(int) (image.getWidth() / Piece.SIZE);
        int numOfRows = 6; //(int) (image.getHeight() / Piece.SIZE);
        // create desk
        desk = new Desk(numOfColumns, numOfRows);
        //final GameState state = GameState.loadFromFile("F:\\m.txt", this);
        //desk.getChildren().addAll(blocks);
        

        // create button box
        Button shuffleButton = new Button("Shuffle");
        shuffleButton.setStyle("-fx-font-size: 2em;");
        shuffleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {

            }
        });
        Button solveButton = new Button("Solve");
        solveButton.setStyle("-fx-font-size: 2em;");
        
        // Load solution
        //lstStep = state.loadSolution("F:\\sol.txt");
        solveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                // call solver
                File fout = new File(fin.getParent() +  "\\solution.txt");
                try {
                    fout.createNewFile();
                    callSolver(solverPath, fin, fout);
                } catch (IOException ex) {
                    Logger.getLogger(UMSimulator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UMSimulator.class.getName()).log(Level.SEVERE, null, ex);
                }
                lstStep = gs.loadSolution(fout.getAbsolutePath());
                // Step k
                SequentialTransition sequentialTransition = new SequentialTransition();
                for(Step k : lstStep){
                    Block block = (Block)k.getBlock();
                    int deltaX = k.getDx();
                    int deltaY = k.getDy();
                    int x0 = block.getX();
                    int y0 = block.getY();
                    
                    
                    Path path = new Path();
                    Point2D pt1 = block.getPosition(x0, y0);
                    Point2D pt2 = block.getPosition(x0 + deltaX, y0 + deltaY);
                    //block.setTranslateX(pt1.getX());
                    //block.setTranslateY(pt1.getY());
                    path.getElements().add(new MoveTo(pt1.getX(), pt1.getY()));
                    path.getElements().add(new LineTo(pt2.getX(), pt2.getY()));

                    block.setXY(x0 + deltaX, y0 + deltaY);
                    PathTransition pathTransition = new PathTransition(Duration.seconds(1), path, block);
                    sequentialTransition.getChildren().add(pathTransition);
                }
                sequentialTransition.play();
                //timeline.playFromStart();
            }
        });
        // Button load state
        Button loadStatebtn = new Button("Load...");
        loadStatebtn.setStyle("-fx-font-size: 2em;");
        loadStatebtn.setOnAction((ActionEvent event) -> {
            UMSimulator.this.showOpenDialog();
        });
        HBox buttonBox = new HBox(8);
        buttonBox.getChildren().addAll(shuffleButton, solveButton, loadStatebtn);
        // create vbox for desk and buttons
        HBox myboard = new HBox();
        ListView<String> lv = new ListView<>();
        lv.setPrefSize(200, 400);
        lv.getItems().add("Step 1");
        lv.getItems().add("Step 1");lv.getItems().add("Step 1");lv.getItems().add("Step 1");
                
        myboard.getChildren().addAll(desk, lv);
        
        VBox vb = new VBox(10);
        vb.getChildren().addAll(myboard,buttonBox);
        root.getChildren().addAll(vb);
    }

    private void showOpenDialog(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("txt file", "txt"));
        File fin = fileChooser.showOpenDialog(this.primaryStage);
        // Load state
        if(fin == null) return;
        this.fin = fin;
        desk.getChildren().removeAll(blocks);
        blocks.clear();
        lstStep.clear();
        gs = GameState.loadFromFile(fin.getAbsolutePath(), this);
        desk.getChildren().addAll(blocks);
    }
    
    private void callSolver(String path, File fin, File fout) throws IOException, InterruptedException{
        List<String> lst = new ArrayList<>();
        lst.add("java");
        lst.add("-jar");
        lst.add(path);
        lst.add(fin.getAbsolutePath());
        //lst.add(fout.getAbsolutePath());
        ProcessBuilder procBuilder = new ProcessBuilder(lst);
        procBuilder.redirectInput(fin);
        procBuilder.redirectOutput(fout);
        Process proc = procBuilder.start();
        proc.waitFor();
    }
    
    @Override
    public IBlock createBlock(int x, int y, int uvx, int uvy, int index, int length) {
        
        Block t =  new Block(res,x,y, index ,length , (uvx == 1),  desk.getWidth(), desk.getHeight());
        t.setActive();
        blocks.add(t);
        return t;
    }

    /**
     * Node that represents the playing area/ desktop where the block sit
     */
    public static class Desk extends Pane {
        Desk(int numOfColumns, int numOfRows) {
            setStyle("-fx-background-color: #cccccc; " +
                    "-fx-border-color: #464646; " +
                    "-fx-effect: innershadow( two-pass-box , rgba(0,0,0,0.8) , 15, 0.0 , 0 , 4 );");
            double DESK_WIDTH = Block.SIZE * numOfColumns;
            double DESK_HEIGHT = Block.SIZE * numOfRows;
            setPrefSize(DESK_WIDTH,DESK_HEIGHT);
            setMaxSize(DESK_WIDTH, DESK_HEIGHT);
            autosize();
            // create path for lines
            Path grid = new Path();
            grid.setStroke(Color.rgb(70, 70, 70));
            getChildren().add(grid);
            // create vertical lines
             for (int col = 0; col < numOfColumns - 1; col++) {
                 grid.getElements().addAll(
                     new MoveTo(Block.SIZE + Block.SIZE * col, 5),
                     new LineTo(Block.SIZE + Block.SIZE * col, Block.SIZE * numOfRows - 5)
                 );
            }
            // create horizontal lines
            for (int row = 0; row < numOfRows - 1; row++) {
                 grid.getElements().addAll(
                     new MoveTo(5, Block.SIZE + Block.SIZE * row),
                     new LineTo(Block.SIZE * numOfColumns - 5, Block.SIZE + Block.SIZE * row)
                 );
            }
        }
        @Override protected void layoutChildren() {}
    }

    @Override public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Unlock me simulator!");
        init(primaryStage);
        
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX 
     * application. main() serves only as fallback in case the 
     * application can not be launched through deployment artifacts,
     * e.g., in IDEs with limited FX support. NetBeans ignores main().
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        launch(args);
    }
}
