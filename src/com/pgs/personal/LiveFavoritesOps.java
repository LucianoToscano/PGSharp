package com.pgs.personal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transformações puras dos favoritos. NÃO acessa, intercepta ou atualiza menus nativos.
 * Um adaptador para PGSharp precisa obter a lista ATIVA, persistir e redesenhar o menu.
 */
public final class LiveFavoritesOps {
    private LiveFavoritesOps() {}

    /** Move uma linha sem alterar o conteúdo nem os campos de outras linhas. */
    public static <T> List<T> reordered(List<T> rows, int from, int to) {
        if (rows == null) throw new IllegalArgumentException("Lista ausente");
        if (from < 0 || from >= rows.size() || to < 0 || to >= rows.size())
            throw new IndexOutOfBoundsException("Posição inválida");
        List<T> output = new ArrayList<>(rows);
        if (from != to) output.add(to, output.remove(from));
        return output;
    }

    /** Altera apenas o nome, preservando IV, espécie, prioridade e demais campos. */
    public static List<Map<String, Object>> renamed(List<Map<String, Object>> rows, int index, String name) {
        if (rows == null) throw new IllegalArgumentException("Lista ausente");
        if (index < 0 || index >= rows.size()) throw new IndexOutOfBoundsException("Posição inválida");
        if (name == null || name.trim().isEmpty() || name.length() > 120)
            throw new IllegalArgumentException("Nome deve conter de 1 a 120 caracteres");
        List<Map<String, Object>> output = new ArrayList<>(rows);
        Map<String, Object> original = rows.get(index);
        if (original == null || !original.containsKey("name"))
            throw new IllegalArgumentException("Linha sem nome");
        Map<String, Object> updated = new HashMap<>(original);
        updated.put("name", name.trim());
        output.set(index, updated);
        return output;
    }
}
