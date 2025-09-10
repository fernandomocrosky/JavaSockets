package projeto.models;

import java.util.ArrayList;
import java.util.List;

public class Filme {
    public String id, titulo, diretor, ano, sinopse, nota, qtd_avaliacoes;
    public List<String> generos = new ArrayList<>();

    public Filme(String titulo, String diretor, String ano, String sinopse, List<String> generos) {
        this.titulo = titulo;
        this.diretor = diretor;
        this.ano = ano;
        this.sinopse = sinopse;
        this.generos = generos;
    }

    public String getId() {
        return id;
    }
    
    public String getTitulo() {
        return titulo;
    }

    public String getDiretor() {
        return diretor;
    }

    public String getAno() {
        return ano;
    }

    public String getSinopse() {
        return sinopse;
    }

    public List<String> getGeneros() {
        return generos;
    }

    public String getNota() {
        return nota;
    }

    public String getQtdAvaliacoes() {
        return qtd_avaliacoes;
    }

    public Filme() {
    }

    public String toString() {
        return "Filme{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", diretor='" + diretor + '\'' +
                ", ano=" + ano +
                ", sinopse='" + sinopse + '\'' +
                ", generos=" + generos + '\'' +
                ", nota=" + nota +
                '}';
    }
}
