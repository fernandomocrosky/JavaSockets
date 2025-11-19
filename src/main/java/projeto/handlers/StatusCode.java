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
    public static final String METHOD_NOT_ALLOWED = "405";
    public static final String ALREADY_EXISTS = "409";
    public static final String GONE = "410";
    public static final String LENGTH_REQUIRED = "411";
    public static final String PAYLOAD_TOO_LARGE = "413";
    public static final String IM_A_TEAPOT = "418";
    public static final String UNPROCESSABLE_ENTITY = "422";
    public static final String INTERNAL_SERVER_ERROR = "500";

    private static final Map<String, String> messages = new HashMap<>();

    static {
        messages.put(OK, "Sucesso: operação realizada com sucesso");
        messages.put(CREATED, "Sucesso: Recurso cadastrado");
        messages.put(BAD_REQUEST, "Erro: Operação não encontrada ou inválida");
        messages.put(UNAUTHORIZED, "Erro: Token inválido");
        messages.put(FORBIDDEN, "Erro: sem permissão");
        messages.put(NOT_FOUND, "Erro: Recurso inexistente");
        messages.put(METHOD_NOT_ALLOWED, "Erro: Campos inválidos, verifique o tipo e quantidade de caracteres");
        messages.put(ALREADY_EXISTS, "Erro: Recurso ja existe");
        messages.put(GONE, "Recurso nao esta mais disponível");
        messages.put(LENGTH_REQUIRED, "Tamanho do conteudo ncessario");
        messages.put(PAYLOAD_TOO_LARGE, "Payload muito grande");
        messages.put(IM_A_TEAPOT, "Eu sou um bule de cha ☕ (Easter Egg)");
        messages.put(UNPROCESSABLE_ENTITY, "Erro: sem permissão");
        messages.put(INTERNAL_SERVER_ERROR, "Erro: Falha interna do servidor");
    }

    public static String getMessage(String code) {
        return messages.getOrDefault(code, "Codigo desconhecido");
    }
}