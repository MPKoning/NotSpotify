import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.application.Application;
import javafx.scene.media.MediaView;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.event.*;
import javafx.scene.*;


public class TestPlayer extends Application{

    @Override
    public void start(Stage primaryStage)
    {
        //Add a scene
        Group root = new Group();
        Scene scene = new Scene(root, 540, 241);

        Media pick = new Media("file:///C:/Users/Mike/git/bullshit/vaporwave.mp3");
        MediaPlayer player = new MediaPlayer(pick);
        player.play();

        //show the stage
        primaryStage.setTitle("Media Player");
        primaryStage.setScene(scene);

        MediaControl mediaControl = new MediaControl(player);
        scene.setRoot(mediaControl);
        primaryStage.show();
    }
    public static void main(String[] args) {
         launch(args);
    }
}
