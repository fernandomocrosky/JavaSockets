package projeto.models;

import java.util.ArrayList;
import java.util.List;

public class Filme {
    public String id, titulo, diretor, ano, sinopse;
    public List<String> generos = new ArrayList<>();

    public Filme(String titulo, String diretor, String ano, String sinopse, List<String> generos) {
        this.titulo = titulo;
        this.diretor = diretor;
        this.ano = ano;
        this.sinopse = sinopse;
        this.generos = generos;
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
                ", generos=" + generos +
                '}';
    }
}
