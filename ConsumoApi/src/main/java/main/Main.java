package main;

import model.Photo;
import view.MainFrame;

import javax.swing.*;
import java.util.List;

/**
 * @ Angelo Pujota
 * @ Consumo de una Api
 */

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ApiClient apiClient = new ApiClient();
            List<Photo> photos = apiClient.fetchPhotos();
            new MainFrame(photos);
        });
    }
}
