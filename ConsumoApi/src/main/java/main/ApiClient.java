package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ApiData;
import model.Photo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ApiClient {

    private static final String API_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=1000&api_key=DEMO_KEY";

    public List<Photo> fetchPhotos() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                ApiData apiData = objectMapper.readValue(connection.getInputStream(), ApiData.class);

                // Convertir las URLs de http a https
                apiData.getPhotos().forEach(photo -> {
                    String imageUrl = photo.getImg_src();
                    if (imageUrl != null && imageUrl.startsWith("http://")) {
                        photo.setImg_src(imageUrl.replaceFirst("http://", "https://"));
                    }
                });

                return List.copyOf(apiData.getPhotos());
            } else {
                throw new RuntimeException("Error al conectarnos a la API: " + responseCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
