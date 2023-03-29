package src.Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Url implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String title;
    private String quote;
    private ArrayList<String> words;
    private int totalTargetUrls;

    public Url(String name, String title, String quote, ArrayList<String> words) {
        this.name = name;
        this.title = title;
        this.quote = quote;
        this.words = words;
        this.totalTargetUrls = 0;
    }

    public void addAimUrls(String url){
        //this.target_url.add(url);
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

    public ArrayList<String> getWords() {
        return words;
    }

    public List<String> getTarget_url() {
        return target_url;
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
                ", words=" + words +
                ", target_url=" + target_url +
                ", totalTargetUrls=" + totalTargetUrls +
                '}';
    }
}
