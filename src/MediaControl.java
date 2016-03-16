import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.event.ActionEvent;
import javafx.scene.media.MediaPlayer.Status;
import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.InvalidationListener;

public class MediaControl extends BorderPane{
  private MediaPlayer mediaPlayer;
  private MediaView mediaView;
  private Duration duration;
  private Slider timeSlider;
  private Label playTime;
  private Slider volumeSlider;
  private HBox mediaBar;

  private final boolean repeat = false;
  private boolean stopRequested = false;
  private boolean atEndOfMedia = false;

  public MediaControl (final MediaPlayer mediaPlayer) {
    this.mediaPlayer = mediaPlayer;
    setStyle("-fx-background-color: #bfc2c7");
    mediaView = new MediaView(mediaPlayer);
    Pane mvPane = new Pane() {};
    mvPane.getChildren().add(mediaView);
    mvPane.setStyle("-fx-background-color: black");
    setCenter(mvPane);

    mediaBar = new HBox();
    mediaBar.setAlignment(Pos.CENTER);
    mediaBar.setPadding(new Insets(5,10,5,10));
    BorderPane.setAlignment(mediaBar, Pos.CENTER);

    final Button playButton = new Button(">");

    playButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
        Status status = mediaPlayer.getStatus();

        if(status == Status.UNKNOWN || status == Status.HALTED){
          return;
        }
        if(status == Status.PAUSED || status == Status.READY || status == Status.STOPPED){
          if(atEndOfMedia){
            mediaPlayer.seek(mediaPlayer.getStartTime());
            atEndOfMedia = false;
          }
          mediaPlayer.play();
        } else {
          mediaPlayer.pause();
        }
      }
    });

    mediaBar.getChildren().add(playButton);

    mediaPlayer.currentTimeProperty().addListener(new InvalidationListener(){
      public void invalidated(Observable ov){
        updateValues();
      }
    });

    mediaPlayer.setOnPlaying(new Runnable(){
      public void run(){
        if(stopRequested){
          mediaPlayer.pause();
          stopRequested = false;
        } else {
          playButton.setText("||");
        }
      }
    });

    mediaPlayer.setOnPaused(new Runnable() {
      public void run(){
        System.out.println("onPaused");
        playButton.setText(">");
      }
    });

    mediaPlayer.setOnReady(new Runnable() {
      public void run(){
        duration = mediaPlayer.getMedia().getDuration();
        updateValues();
      }
    });

    mediaPlayer.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
    mediaPlayer.setOnEndOfMedia(new Runnable() {
      public void run(){
        if(!repeat){
          playButton.setText(">");
          stopRequested = true;
          atEndOfMedia = true;
        }
      }
    });



    //spacer
    Label spacer = new Label("  ");
    mediaBar.getChildren().add(spacer);

    //time Label
    Label timeLabel = new Label("Time: ");
    mediaBar.getChildren().add(timeLabel);

    //time Slider
    timeSlider = new Slider();
    HBox.setHgrow(timeSlider, Priority.ALWAYS);
    timeSlider.setMinWidth(50);
    timeSlider.setMaxWidth(Double.MAX_VALUE);

    timeSlider.valueProperty().addListener(new InvalidationListener(){
      public void invalidated(Observable ov){
        if(timeSlider.isValueChanging()){
          //multiply duration by percentage of slider posistion
          mediaPlayer.seek(duration.multiply(timeSlider.getValue() / 100.0));
        }
      }
    });

    mediaBar.getChildren().add(timeSlider);

    //play Label
    playTime = new Label();
    playTime.setPrefWidth(130);
    playTime.setMinWidth(50);
    mediaBar.getChildren().add(playTime);

    //volume Label
    Label volumeLabel = new Label("Vol: ");
    mediaBar.getChildren().add(volumeLabel);

    //volume Slider
    volumeSlider = new Slider();
    volumeSlider.setPrefWidth(70);
    volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
    volumeSlider.setMinWidth(30);

    volumeSlider.valueProperty().addListener(new InvalidationListener(){
      public void invalidated(Observable ov){
        if(volumeSlider.isValueChanging()){
          mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
        }
      }
    });

    mediaBar.getChildren().add(volumeSlider);

    setBottom(mediaBar);

  }

  protected void updateValues(){
    if(playTime != null && timeSlider != null && volumeSlider != null){
      Platform.runLater(new Runnable() {
        public void run(){
          Duration currentTime = mediaPlayer.getCurrentTime();
          playTime.setText(formatTime(currentTime, duration));
          timeSlider.setDisable(duration.isUnknown());
          if(!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()){
            double tempTime = duration.toMillis();
            timeSlider.setValue(currentTime.divide(tempTime).toMillis() * 100.0);
          }
          if(!volumeSlider.isValueChanging()){
            volumeSlider.setValue((int)Math.round(mediaPlayer.getVolume() * 100));
          }
        }
      });
    }
  }

  private static String formatTime(Duration elapsed, Duration duration) {
    int intElapsed = (int)Math.floor(elapsed.toSeconds());
    int elapsedHours = intElapsed / (60 * 60);
    if (elapsedHours > 0) {
      intElapsed -= elapsedHours * 60 * 60;
    }

    int elapsedMinutes = intElapsed / 60;
    int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

    if (duration.greaterThan(Duration.ZERO)) {
      int intDuration = (int)Math.floor(duration.toSeconds());
      int durationHours = intDuration / (60 * 60);
      if (durationHours > 0) {
        intDuration -= durationHours * 60 * 60;
      }

      int durationMinutes = intDuration / 60;
      int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
      if (durationHours > 0) {
        return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds, durationHours, durationMinutes, durationSeconds);
      } else {
          return String.format("%02d:%02d/%02d:%02d", elapsedMinutes, elapsedSeconds,durationMinutes, durationSeconds);
      }

    } else {
      if (elapsedHours > 0) {
        return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
      } else {
        return String.format("%02d:%02d",elapsedMinutes, elapsedSeconds);
      }
    }
  }
}
