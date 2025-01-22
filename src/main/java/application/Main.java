package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author Stephan
 */
public class Main extends Application {

    public static void main(String[] args) throws IOException{
        launch(args);
    }

    /**
     * @author Stephan
     * @param primaryStage
     * @throws IOException
     * loads the FXML file and opens the window
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Start.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Log in");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("images/board/blue_1.png").openStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double maxX = bounds.getMaxX();
        double maxY = bounds.getMaxY();
        double font = maxY / 90;
        if(maxY < 900) {
            root.styleProperty().set("-fx-font-size: 10px;");
        } else {
            root.styleProperty().set("-fx-font-size: " + font + "px;");
        }


        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            primaryStage.close();
            System.exit(0); //stop programm
        });
    }
}
