package projeto.handlers;

import java.util.HashMap;
import java.util.Map;

public final class StatusCode {

    private StatusCode() {
    }

    public static final String OK = "200";
    public static final String CREATED = "201";
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    public static final String ALREADY_EXISTS = "409";
    public static final String GONE = "410";
    public static final String LENGTH_REQUIRED = "411";
    public static final String PAYLOAD_TOO_LARGE = "413";
    public static final String IM_A_TEAPOT = "418";
    public static final String UNPROCESSABLE_ENTITY = "422";
    public static final String INTERNAL_SERVER_ERROR = "500";

    private static final Map<String, String> messages = new HashMap<>();

    static {
        messages.put(OK, "Operacao realizada com sucesso");
        messages.put(CREATED, "Recurso criado com sucesso");
        messages.put(BAD_REQUEST, "Requisicao inválida");
        messages.put(UNAUTHORIZED, "Nao autorizado");
        messages.put(FORBIDDEN, "Acesso proibido");
        messages.put(NOT_FOUND, "Recurso nao encontrado");
        messages.put(ALREADY_EXISTS, "Recurso ja existe");
        messages.put(GONE, "Recurso nao esta mais disponível");
        messages.put(LENGTH_REQUIRED, "Tamanho do conteudo ncessario");
        messages.put(PAYLOAD_TOO_LARGE, "Payload muito grande");
        messages.put(IM_A_TEAPOT, "Eu sou um bule de cha ☕ (Easter Egg)");
        messages.put(UNPROCESSABLE_ENTITY, "Entidade nao processavel");
        messages.put(INTERNAL_SERVER_ERROR, "Erro interno no servidor");
    }

    public static String getMessage(String code) {
        return messages.getOrDefault(code, "Codigo desconhecido");
    }
}