package ch.obermuhlner.astro.javafx;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialog {

  public static void show(String title, String message, Runnable runnable) {
    Stage dialogStage = new Stage();
    dialogStage.initStyle(StageStyle.UTILITY);
    dialogStage.setResizable(false);
    dialogStage.initModality(Modality.APPLICATION_MODAL);
    dialogStage.setTitle(title);

    BorderPane borderPane = new BorderPane();

    Label label = new Label(message);
    label.setWrapText(true);
    label.setPrefWidth(360);
    borderPane.setTop(label);

    ProgressIndicator progress = new ProgressIndicator(-1);
    borderPane.setCenter(progress);

    Scene scene = new Scene(borderPane);
    dialogStage.setScene(scene);

    dialogStage.show();

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        runnable.run();
        return null;
      }
    };

    task.setOnSucceeded(event -> {
      dialogStage.close();
    });

    new Thread(task).start();
  }
}
