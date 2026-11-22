package com.pgs.personal;

import java.util.List;
import java.util.Map;

/**
 * Contrato exigido para alterar o menu ORIGINAL do PGSharp.
 *
 * Essa classe não é um adaptador Unity: um port concreto precisa obter a lista
 * nativa ativa, gravar no armazenamento real e invalidar o menu. A ausência
 * desse adaptador DEVE impedir anunciar um APK como suporte a arraste nativo.
 */
public final class NativeMenuCoordinator {

    public interface FavoritesPort {
        List<Map<String, Object>> readActiveRows();
        boolean saveActiveRows(List<Map<String, Object>> rows);
        void refreshVisibleRows();
    }

    public interface ShortcutsPort {
        CustomShortcutsModel readActiveShortcuts();
        boolean saveActiveShortcuts(CustomShortcutsModel shortcuts);
        void refreshVisibleShortcuts();
        /** Somente true após comprovar quatro feeds independentes no motor nativo. */
        boolean supportsFourIndependentSniperFeeds();
    }

    private final FavoritesPort favorites;
    private final ShortcutsPort shortcuts;

    public NativeMenuCoordinator(FavoritesPort favorites, ShortcutsPort shortcuts) {
        if (favorites == null || shortcuts == null)
            throw new IllegalArgumentException("É obrigatório ter os adaptadores NATIVOS");
        this.favorites = favorites;
        this.shortcuts = shortcuts;
    }

    public synchronized void moveFavorite(int from, int to) {
        List<Map<String, Object>> before = requireRows(favorites.readActiveRows());
        List<Map<String, Object>> after = LiveFavoritesOps.reordered(before, from, to);
        saveAndRefreshFavorites(before, after);
    }

    public synchronized void renameFavorite(int index, String newName) {
        List<Map<String, Object>> before = requireRows(favorites.readActiveRows());
        List<Map<String, Object>> after = LiveFavoritesOps.renamed(before, index, newName);
        saveAndRefreshFavorites(before, after);
    }

    public synchronized void moveShortcut(String id, int to) {
        CustomShortcutsModel before = requireShortcuts(shortcuts.readActiveShortcuts());
        CustomShortcutsModel after = before.move(id, to);
        saveAndRefreshShortcuts(before, after);
    }

    public synchronized void setShortcutEnabled(String id, boolean enabled) {
        CustomShortcutsModel before = requireShortcuts(shortcuts.readActiveShortcuts());
        CustomShortcutsModel after = before.setEnabled(id, enabled);
        saveAndRefreshShortcuts(before, after);
    }

    public synchronized void enableExtraSniperSlots() {
        if (!shortcuts.supportsFourIndependentSniperFeeds()) {
            throw new UnsupportedOperationException("Motor nativo não comprovou Sniper III/IV");
        }
        CustomShortcutsModel before = requireShortcuts(shortcuts.readActiveShortcuts());
        CustomShortcutsModel after = before.addExtraSnipers(true);
        saveAndRefreshShortcuts(before, after);
    }

    private void saveAndRefreshFavorites(List<Map<String, Object>> before,
                                         List<Map<String, Object>> after) {
        if (!favorites.saveActiveRows(after)) {
            throw new IllegalStateException("Salvamento real dos favoritos falhou");
        }
        try {
            favorites.refreshVisibleRows();
        } catch (RuntimeException failure) {
            try {
                if (favorites.saveActiveRows(before)) favorites.refreshVisibleRows();
                else failure.addSuppressed(new IllegalStateException("Rollback falhou"));
            } catch (RuntimeException rollback) {
                failure.addSuppressed(rollback);
            }
            throw failure;
        }
    }

    private void saveAndRefreshShortcuts(CustomShortcutsModel before,
                                         CustomShortcutsModel after) {
        if (!shortcuts.saveActiveShortcuts(after)) {
            throw new IllegalStateException("Salvamento real dos atalhos falhou");
        }
        try {
            shortcuts.refreshVisibleShortcuts();
        } catch (RuntimeException failure) {
            try {
                if (shortcuts.saveActiveShortcuts(before)) shortcuts.refreshVisibleShortcuts();
                else failure.addSuppressed(new IllegalStateException("Rollback falhou"));
            } catch (RuntimeException rollback) {
                failure.addSuppressed(rollback);
            }
            throw failure;
        }
    }

    private static List<Map<String, Object>> requireRows(List<Map<String, Object>> rows) {
        if (rows == null) throw new IllegalStateException("Lista original não disponível");
        return rows;
    }

    private static CustomShortcutsModel requireShortcuts(CustomShortcutsModel rows) {
        if (rows == null) throw new IllegalStateException("Atalhos originais não disponíveis");
        return rows;
    }
}
