package com.pgs.personal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modelo de atalhos para futura integração com a tela ORIGINAL do PGSharp.
 *
 * Não injeta controles no Unity e não grava dados do PGSharp.
 * III e IV só devem ficar visíveis quando um adaptador nativo oferecer
 * instâncias REAIS, independentes e persistentes dos respectivos feeds.
 */
public final class CustomShortcutsModel {
    public static final String SNIPER_I = "hlfastsnipe";
    public static final String SNIPER_II = "hlfastsnipe2";
    public static final String SNIPER_III = "hlfastsnipe3";
    public static final String SNIPER_IV = "hlfastsnipe4";

    public static final class Shortcut {
        public final String id;
        public final String label;
        public final boolean enabled;
        public final String feedFilterId;

        public Shortcut(String id, String label, boolean enabled, String feedFilterId) {
            if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("id vazio");
            if (label == null || label.trim().isEmpty()) throw new IllegalArgumentException("nome vazio");
            this.id = id;
            this.label = label;
            this.enabled = enabled;
            this.feedFilterId = feedFilterId;
        }

        Shortcut withEnabled(boolean active) {
            return new Shortcut(id, label, active, feedFilterId);
        }

        Shortcut withFeedFilter(String newFilterId) {
            return new Shortcut(id, label, enabled, newFilterId);
        }
    }

    private final List<Shortcut> items;

    public CustomShortcutsModel(List<Shortcut> shortcuts) {
        if (shortcuts == null) throw new IllegalArgumentException("lista ausente");
        Set<String> used = new HashSet<>();
        List<Shortcut> safe = new ArrayList<>();
        for (Shortcut shortcut : shortcuts) {
            if (shortcut == null || !used.add(shortcut.id)) {
                throw new IllegalArgumentException("atalho nulo ou identificador duplicado");
            }
            safe.add(shortcut);
        }
        items = Collections.unmodifiableList(safe);
    }

    /** A lista exibida deve vir do adaptador da interface ativa, nunca de um DAT externo. */
    public List<Shortcut> all() {
        return items;
    }

    public CustomShortcutsModel move(String id, int targetIndex) {
        int source = indexOf(id);
        if (targetIndex < 0 || targetIndex >= items.size()) {
            throw new IndexOutOfBoundsException("destino fora da lista");
        }
        List<Shortcut> reordered = new ArrayList<>(items);
        Shortcut moved = reordered.remove(source);
        reordered.add(targetIndex, moved);
        return new CustomShortcutsModel(reordered);
    }

    public CustomShortcutsModel setEnabled(String id, boolean enabled) {
        int index = indexOf(id);
        List<Shortcut> edited = new ArrayList<>(items);
        edited.set(index, edited.get(index).withEnabled(enabled));
        return new CustomShortcutsModel(edited);
    }

    public CustomShortcutsModel setFeedFilter(String id, String filterId) {
        if (!isSniper(id)) throw new IllegalArgumentException("atalho não é um Sniper");
        if (filterId == null || filterId.trim().isEmpty()) {
            throw new IllegalArgumentException("filtro obrigatório");
        }
        int index = indexOf(id);
        List<Shortcut> edited = new ArrayList<>(items);
        edited.set(index, edited.get(index).withFeedFilter(filterId));
        return new CustomShortcutsModel(edited);
    }

    /**
     * Não cria Snipers fictícios: o adaptador do Unity deve provar suporte a 4
     * widgets de feed independentes antes de chamar com true.
     */
    public CustomShortcutsModel addExtraSnipers(boolean nativeBackendSupportsFourSlots) {
        if (!nativeBackendSupportsFourSlots) {
            throw new UnsupportedOperationException("Sniper III/IV requerem integração nativa");
        }
        List<Shortcut> expanded = new ArrayList<>(items);
        if (!contains(SNIPER_III)) expanded.add(new Shortcut(SNIPER_III, "Sniper Rápido III", false, null));
        if (!contains(SNIPER_IV)) expanded.add(new Shortcut(SNIPER_IV, "Sniper Rápido IV", false, null));
        return new CustomShortcutsModel(expanded);
    }

    public static boolean isSniper(String id) {
        return SNIPER_I.equals(id) || SNIPER_II.equals(id)
            || SNIPER_III.equals(id) || SNIPER_IV.equals(id);
    }

    public Shortcut get(String id) {
        return items.get(indexOf(id));
    }

    private int indexOf(String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(id)) return i;
        }
        throw new IllegalArgumentException("atalho desconhecido: " + id);
    }

    private boolean contains(String id) {
        for (Shortcut shortcut : items) if (shortcut.id.equals(id)) return true;
        return false;
    }
}
