package mg.itu.prom16.model;

import java.io.InputStream;

public class MyFile {
    private InputStream inputStream;
    private String filename;

    public MyFile(InputStream inputStream, String filename) {
        this.inputStream = inputStream;
        this.filename = filename;
    }
    public MyFile() {
    }
    public InputStream getInputStream() {
        return inputStream;
    }
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
}
