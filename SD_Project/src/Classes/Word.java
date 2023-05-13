package src.Classes;

import java.io.Serializable;

/**
 * Classe usada para guardar as palavras encontradas e o numero de vezes que estas foram pesquisadas
 */
public class Word implements Serializable {
    private final String word;
    private int count;

    /**
     * Construtor da classe Word que recebe como paramentro a palavra.
     * @param word palavra
     */
    public Word(String word) {
        this.word = word;
        this.count = 0;
    }

    /**
     * Construtor da classe Word que recebe como paramentros a palavra e o numero de pesquisas.
     * @param word palavra
     * @param count numero de pesquisas da palavra
     */
    public Word(String word, int count) {
        this.word = word;
        this.count = count;
    }

    /**
     * Getter que retorna a palavra
     * @return palavra
     */
    public String getWord() {
        return word;
    }

    /**
     * Getter que retorna o numero de pesquisas.
     * @return numero de pesquisas
     */
    public int getCount() {
        return count;
    }

    /**
     * Setter que coluca o numero de pesquisas igual ao count.
     * @param count numero de pesquisas
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Metodo que adiciona um nova pesquisa a uma Word
     */
    public void addCount(){
        this.count+=1;
    }
    @Override
    public String toString() {
        return this.word;
    }
}
