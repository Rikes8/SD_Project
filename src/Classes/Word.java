package src.Classes;

import java.io.Serializable;

public class Word implements Serializable {
    private final String word;
    private int count;

    //inicialização
    public Word(String word) {
        this.word = word;
        this.count = 0;
    }

    //Leitura
    public Word(String word, int count) {
        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount(){
        this.count+=1;
    }
}
