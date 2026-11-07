package com.ucop.desktop;

import com.ucop.desktop.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UcopApplication extends Application {
    private AnnotationConfigApplicationContext context;

    @Override
    public void start(Stage stage) throws Exception {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(context::getBean);
        stage.setTitle("UCOP - Quản lý bán lẻ");
        stage.setScene(new Scene(loader.load(), 520, 360));
        stage.setMinWidth(520);
        stage.setMinHeight(360);
        stage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
