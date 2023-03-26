package src.Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Url implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String title;
        private String quote;
        private List<String> url_apointed;
        private List<String> aim_url;
        private int totalUrls;

        public Url(String name, String title, String quote,List<String> urls) {
            this.name = name;
            this.title = title;
            this.quote = quote;
            this.url_apointed = urls;
            this.aim_url = new ArrayList<>();
            this.totalUrls = 0;
        }

        public void addAimUrls(String url){
            this.aim_url.add(url);
            this.totalUrls+=1;
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
        public int getTotalUrls() {
            return totalUrls;
        }

        @Override
        public String toString() {
            return "Url{" + "name=" + name + ", title=" + title + ", keywords=" + ", urls=" + url_apointed + ", totalUrls=" + totalUrls + '}';
        }



}

