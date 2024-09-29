package mg.itu.prom16.model;

import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mg.itu.prom16.serializer.LocalDateJson;

public class MyJson {
    private Gson gson;

    public Gson getGson() {
        return gson;
    }

    public MyJson() {
        GsonBuilder gBuilder = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateJson());
        this.gson = gBuilder.create();
    }

    
}