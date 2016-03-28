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
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.stage.FileChooser;

public class UMSimulator extends Application implements IBlockFactory {
    private Timeline timeline;
    private Image bg_image;
    private Stage primaryStage;
    private Desk desk;
    private final List<Block> blocks = new ArrayList<>();
    private final Map<Integer,Image> res = new HashMap<>();
    private final String solverPath = "F:\\solver\\UnlockMe.jar";
    
    private final String solverName = "UnlockMe.jar";
    private final String okExtension = "txt";
    List<Step> lstStep = new ArrayList<>();
    File fin;
    GameState gs;
    ExecutorService exe = Executors.newSingleThreadExecutor();
    private Alert alert;
    
    private Slider slider;
    
    private final String[] algorithmList = 
        new String[]{"Breadth first seach", "Depth first search", "A-star"};
    
    private void init(Stage primaryStage) {
        // Curreent path
        String currentPath = System.getProperty("user.dir");
        java.nio.file.Path path = Paths.get(currentPath, "testcase");
        File folder = path.toFile();
        File[] testcases = folder.listFiles((File pathname) -> pathname.isFile() && pathname.getAbsoluteFile()
                .toString().toLowerCase().endsWith(okExtension));
        
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
        
        final DialogPane value = new DialogPane();
        value.setPrefWidth(400);
        value.setContent(new ProgressBar());
        
        // Load solution
        //lstStep = state.loadSolution("F:\\sol.txt");
        solveButton.setOnAction((ActionEvent actionEvent) -> {
            // choice algorithm
            ChoiceDialog<String> choiceDialog = createChoiceAlgorithm();
            Optional<String> choice = choiceDialog.showAndWait();
            String algorithm;
            if(choice.isPresent()){
                algorithm = choice.get();
            }
            else return;
            
            // call solver
            File fout = new File(Paths.get(fin.getParent(), "..", "solution" , fin.getName()).toString());
            alert = createWaitAlert();
            try {
                fout.createNewFile();
                solverServiceTask = new SolverTask(algorithm);
                // Call solver
                solverServiceTask.init(Paths.get(".","solver","UnlockMe.jar").toString(), fin, fout);
                
                solverServiceTask.setOnSucceeded((WorkerStateEvent wse) -> {
                    System.out.println(solverServiceTask.getValue());
                    
                    alert.getDialogPane().setContent(new Label("Solution has been found!"));
                    
                });
                solverServiceTask.setOnFailed((WorkerStateEvent wse) -> {
                    alert.setAlertType(AlertType.ERROR);
                    alert.getDialogPane().setContent(new Label("Solver has failed!"));
                    
                });
                
                // Show alert
                alert.setOnCloseRequest((DialogEvent de) -> {
                    System.out.println("Shutdown all");
                    if(solverServiceTask.isRunning())
                        solverServiceTask.cancel();
                });
                
                exe.execute(solverServiceTask);
                alert.showAndWait();
                // Recovery alert pane
                value.setContent(new ProgressBar());
                
            } catch (IOException ex) {
                ex.printStackTrace();
                //Logger.getLogger(UMSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
            lstStep = gs.loadSolution(fout.getAbsolutePath());
            // Step k
            SequentialTransition sequentialTransition = new SequentialTransition();
            lstStep.stream().map((k) -> {
                Block block = (Block)k.getBlock();
                int deltaX = k.getDx();
                int deltaY = k.getDy();
                int x0 = block.getX();
                int y0 = block.getY();
                Path path1 = new Path();
                Point2D pt1 = block.getPosition(x0, y0);
                Point2D pt2 = block.getPosition(x0 + deltaX, y0 + deltaY);
                //block.setTranslateX(pt1.getX());
                //block.setTranslateY(pt1.getY());
                path1.getElements().add(new MoveTo(pt1.getX(), pt1.getY()));
                path1.getElements().add(new LineTo(pt2.getX(), pt2.getY()));
                block.setXY(x0 + deltaX, y0 + deltaY);
                PathTransition pathTransition = new PathTransition(Duration.seconds(1), path1, block);
                
                return pathTransition;
            }).forEach((pathTransition) -> {
                sequentialTransition.getChildren().add(pathTransition);
            });
            sequentialTransition.play();
            sequentialTransition.setRate(2);
            //timeline.playFromStart();
        });
        // Button load state
        Button loadStatebtn = new Button("Load...");
        loadStatebtn.setStyle("-fx-font-size: 2em;");
        loadStatebtn.setOnAction((ActionEvent event) -> {
            UMSimulator.this.showOpenDialog();
        });
        slider = new Slider(0, 100, 2);
        slider.setPrefWidth(400);
        
        
        HBox buttonBox = new HBox(8);
        buttonBox.getChildren().addAll(shuffleButton, solveButton, loadStatebtn, slider);
        // create vbox for desk and buttons
        HBox myboard = new HBox();
        ListView<File> lv = new ListView<>();
        lv.setPrefSize(200, 400);
        
        lv.getItems().addAll(Arrays.asList(testcases));
        // Set cellview
        lv.setCellFactory((ListView<File> param) -> {
            return new FileListCell();
        });
        // Set event handler
        lv.setOnMouseClicked((MouseEvent event) -> {
            File filein = lv.getSelectionModel().getSelectedItem();
            UMSimulator.this.loadTest(filein);
        });
        
        myboard.getChildren().addAll(desk, lv);
        
        VBox vb = new VBox(10);
        vb.getChildren().addAll(myboard,buttonBox);
        root.getChildren().addAll(vb);
        
    }
    
    public Alert createWaitAlert(){
        Alert malert;
        malert = new Alert(AlertType.INFORMATION);
        malert.setTitle("Hello");
        malert.setHeaderText("Information Alert");
        String s ="This is an example of JavaFX 8 Dialogs... ";
        malert.setContentText(s);
        // Create Pane
        DialogPane value = new DialogPane();
        value.setPrefWidth(400);
        value.setContent(new ProgressBar());
        malert.getDialogPane().setContent(value);
        return malert;
    }
    

    
    public ChoiceDialog<String> createChoiceAlgorithm(){
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(algorithmList[0], algorithmList);
        choiceDialog.setTitle("Unlock me simulation");
        choiceDialog.setHeaderText("Choice a algorithm in the list below!");
        
        return choiceDialog;
        
    }

    private void showOpenDialog(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("txt file", "txt"));
        File file = fileChooser.showOpenDialog(this.primaryStage);
        // Load state
        loadTest(file);
    }
    private void loadTest(File fin){
        if(fin == null) return;
        this.fin = fin;
        desk.getChildren().removeAll(blocks);
        blocks.clear();
        lstStep.clear();
        gs = GameState.loadFromFile(fin.getAbsolutePath(), this);
        desk.getChildren().addAll(blocks);
    }
    
    private class SolverTask extends Task<Integer> {
        private String solverPath;
        private File fin, fout;
        private String algorithmName;
        public SolverTask(){
            super();
            this.algorithmName = algorithmList[0];
        }
        
        public SolverTask(String algorithm){
            super();
            this.algorithmName = algorithm;
        }
        
        public void init(String solverPath, File fin, File fout){
            this.fin = fin;
            this.fout = fout;
            this.solverPath = solverPath;
        }
        @Override
        protected Integer call() throws Exception {
            final String[] alg = new String[]{"bfs", "dfs","as"};
            String algorithm = alg[0];
            for (int i = 0; i < alg.length; i++) {
                if(algorithmName.equalsIgnoreCase(algorithmList[i])){
                    algorithm = alg[i];
                    break;
                }
            }
            
            int exitCode =  UMSimulator.this.callSolver(solverPath, fin, fout, algorithm);
            if (exitCode != 0)
                throw new Exception("There are some thing wrong.");
            else this.succeeded();
            
           
            return exitCode;
        }
        
    };
    
    SolverTask solverServiceTask = new SolverTask();
    
    private int callSolver(String path, File fin, File out) throws IOException{
        return callSolver(path, fin, out, "bfs");
    }
    
    private int callSolver(String path, File fin, File fout, String algorithm) throws IOException{
        System.out.println(fin.getAbsoluteFile());
        try {
            File test = new File(path);
            // Use default value
            if(!test.isFile()){
                path = Paths.get(".","solver","UnlockMe.jar").toString();
            }
            
            List<String> lst = new ArrayList<>();
            // $ java -jar [solver path] -s [bfs|dfs|chs] -f [input file]
            lst.add("java");
            lst.add("-jar");
            lst.add(path);
            lst.add("-s");
            lst.add(algorithm);
            lst.add("-f");
            lst.add(fin.getAbsolutePath());
            ProcessBuilder procBuilder = new ProcessBuilder(lst);
            procBuilder.redirectInput(fin);
            procBuilder.redirectOutput(fout);
            Process proc = procBuilder.start();
            
            proc.waitFor();
            return proc.exitValue();
        } catch (InterruptedException ex) {
            Logger.getLogger(UMSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
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

    @Override
    public void stop() throws Exception {
        super.stop();
        exe.shutdownNow();
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

    private static class FileListCell extends ListCell<File> {

        @Override
        protected void updateItem(File item, boolean empty) {
            super.updateItem(item, empty);
            if(item != null) setText(item.getName());
        }
        
    }
}
