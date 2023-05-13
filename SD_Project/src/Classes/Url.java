package src.Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilizada para guardar os dados dos urls.
 */

public class Url implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String title;
    private String quote;
    private ArrayList<String> urlTarget;
    private int totalTargetUrls;

    public void setTotalTargetUrls(int totalTargetUrls) {
        this.totalTargetUrls = totalTargetUrls;
    }

    /**
     * construtor da classe Url que recebe como parametros o nome,titulo e citação.
     * @param name url
     * @param title titulo da pagina
     * @param quote citação
     */
    public Url(String name, String title, String quote) {
        this.name = name;
        this.title = title;
        this.quote = quote;
        this.urlTarget = new ArrayList<String>();
        this.totalTargetUrls = 0;
    }

    /**
     * Metodo que adiciona um novo URL a lista de urls que apontam para o url
     * @param url url
     */
    public void addAimUrls(String url){
        this.urlTarget.add(url);
        this.totalTargetUrls+=1;
    }

    /***
     * Getter que retorna o nome do URL
     * @return nome
     */
    public String getName() {
        return name;
    }

    /**
     * Getter que retorna o titulo do URL
     * @return titulo
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter que retorna uma citação prensente no URL
     * @return citação
     */
    public String getQuote() {
        return quote;
    }

    /**
     * Getter que retorna a lista de de URL's que apontam para o URL
     * @return lista de URL's
     */
    public ArrayList<String> getTarget_url() {
        return urlTarget;
    }

    /**
     * Getter que retorna o total de URL's que apontam para o URL
     * @return total de URL's que apontam para o URL
     */
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
