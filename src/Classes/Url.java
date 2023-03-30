package src.Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Url implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String title;
    private String quote;
    private ArrayList<String> urlTarget;
    private int totalTargetUrls;

    public Url(String name, String title, String quote) {
        this.name = name;
        this.title = title;
        this.quote = quote;
        this.urlTarget = new ArrayList<String>();
        this.totalTargetUrls = 0;
    }

    public void addAimUrls(String url){
        this.urlTarget.add(url);
        this.totalTargetUrls+=1;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getQuote() {
        return quote;
    }

    public ArrayList<String> getTarget_url() {
        return urlTarget;
    }

    public int getTotalTargetUrls() {
        return totalTargetUrls;
    }

    @Override
    public String toString() {
        return "Url{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", quote='" + quote + '\'' +
                '}';
    }
}
