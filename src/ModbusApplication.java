/*
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class ModbusApplication extends Application {
    private final ArrayList<String> dataList = new ArrayList<>();
    private String filename = "data.txt";

    @Override
    public void start(Stage primaryStage) throws Exception {

        try{
            readData();
        }catch(IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Problems reading data from text file!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }


    }

    public void readData() throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource(filename);

        try(InputStream in = url.openStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(in))){
            String line;
            while((line = input.readLine()) != null){
                dataList.add(line);
            }
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}
*/