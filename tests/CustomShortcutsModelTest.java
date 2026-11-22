import com.pgs.personal.CustomShortcutsModel;
import java.util.ArrayList;
import java.util.List;

public final class CustomShortcutsModelTest {
    private static void check(boolean ok, String description) {
        if (!ok) throw new AssertionError(description);
    }

    private static CustomShortcutsModel sample() {
        List<CustomShortcutsModel.Shortcut> source = new ArrayList<>();
        source.add(new CustomShortcutsModel.Shortcut("favorites","Favoritos",true,null));
        source.add(new CustomShortcutsModel.Shortcut("map","Mapa",true,null));
        source.add(new CustomShortcutsModel.Shortcut(CustomShortcutsModel.SNIPER_I,"Sniper Rápido I",true,"filtro100"));
        source.add(new CustomShortcutsModel.Shortcut(CustomShortcutsModel.SNIPER_II,"Sniper Rápido II",true,"pvp_top1"));
        return new CustomShortcutsModel(source);
    }

    public static void main(String[] args) {
        CustomShortcutsModel base = sample();
        CustomShortcutsModel reordered = base.move(CustomShortcutsModel.SNIPER_II,0);
        check(reordered.all().get(0).id.equals(CustomShortcutsModel.SNIPER_II), "arraste");
        check(base.all().get(3).id.equals(CustomShortcutsModel.SNIPER_II), "base imutável");
        check(reordered.get("favorites").enabled, "estado do toggle preservado");

        CustomShortcutsModel disabled = reordered.setEnabled("map", false);
        check(!disabled.get("map").enabled, "toggle individual");
        check(disabled.get("favorites").enabled, "outros toggles preservados");

        CustomShortcutsModel edited = disabled.setFeedFilter(CustomShortcutsModel.SNIPER_I,"novoFiltro");
        check(edited.get(CustomShortcutsModel.SNIPER_I).feedFilterId.equals("novoFiltro"),"filtro I atualizado");
        check(edited.get(CustomShortcutsModel.SNIPER_II).feedFilterId.equals("pvp_top1"), "filtro II independente");

        boolean rejected = false;
        try { edited.addExtraSnipers(false); } catch (UnsupportedOperationException e) { rejected = true; }
        check(rejected,"sem suporte nativo não exibir Sniper III/IV");

        CustomShortcutsModel expanded = edited.addExtraSnipers(true);
        check(expanded.all().size() == base.all().size() + 2, "dois slots extras");
        check(!expanded.get(CustomShortcutsModel.SNIPER_III).enabled, "III inicia desativado");
        check(!expanded.get(CustomShortcutsModel.SNIPER_IV).enabled, "IV inicia desativado");
        expanded = expanded.setFeedFilter(CustomShortcutsModel.SNIPER_III,"shiny")
            .setFeedFilter(CustomShortcutsModel.SNIPER_IV,"raids")
            .setEnabled(CustomShortcutsModel.SNIPER_IV,true);
        check(expanded.get(CustomShortcutsModel.SNIPER_III).feedFilterId.equals("shiny"),"III independente");
        check(expanded.get(CustomShortcutsModel.SNIPER_IV).feedFilterId.equals("raids"),"IV independente");
        check(expanded.get(CustomShortcutsModel.SNIPER_I).feedFilterId.equals("novoFiltro"),"I preservado");
        check(expanded.get(CustomShortcutsModel.SNIPER_II).feedFilterId.equals("pvp_top1"),"II preservado");
        check(expanded.addExtraSnipers(true).all().size() == expanded.all().size(),"idempotência dos slots");
        rejected = false;
        try {expanded.move("map",-1);}catch(IndexOutOfBoundsException e){rejected=true;}
        check(rejected,"posição inválida rejeitada");
        System.out.println("OK: mover, toggles, 4 filtros independentes, sem slots fictícios");
    }
}
