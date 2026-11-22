import com.pgs.personal.CustomShortcutsModel;
import com.pgs.personal.NativeMenuCoordinator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeMenuCoordinatorTest {
    static void check(boolean value, String desc) {
        if (!value) throw new AssertionError(desc);
    }
    static Map<String,Object> row(String name, double lat) {
        Map<String,Object> v = new HashMap<>();
        v.put("name",name);v.put("lat",lat);v.put("lng",-38.50);return v;
    }
    static class FavoriteFake implements NativeMenuCoordinator.FavoritesPort {
        List<Map<String,Object>> data = new ArrayList<>();
        boolean failSave, failRefresh;
        int saves, renders;
        public List<Map<String,Object>> readActiveRows(){return new ArrayList<>(data);}
        public boolean saveActiveRows(List<Map<String,Object>> next) {
            saves++;
            if(failSave)return false;
            data = new ArrayList<>(next);return true;
        }
        public void refreshVisibleRows() {
            renders++;
            if(failRefresh){failRefresh=false;throw new RuntimeException("render caiu");}
        }
    }
    static class ShortcutFake implements NativeMenuCoordinator.ShortcutsPort {
        CustomShortcutsModel model;
        boolean supports, failSave, failRefresh;
        int saves;
        ShortcutFake() {
            model = new CustomShortcutsModel(Arrays.asList(
                new CustomShortcutsModel.Shortcut("favorites","Favoritos",true,null),
                new CustomShortcutsModel.Shortcut(CustomShortcutsModel.SNIPER_I,"Sniper I",true,"filtro1"),
                new CustomShortcutsModel.Shortcut(CustomShortcutsModel.SNIPER_II,"Sniper II",false,"filtro2")
            ));
        }
        public CustomShortcutsModel readActiveShortcuts(){return model;}
        public boolean saveActiveShortcuts(CustomShortcutsModel next){
            saves++;if(failSave)return false;model=next;return true;
        }
        public void refreshVisibleShortcuts(){
            if(failRefresh){failRefresh=false;throw new RuntimeException("menu caiu");}
        }
        public boolean supportsFourIndependentSniperFeeds(){return supports;}
    }
    public static void main(String[]args) {
        FavoriteFake f = new FavoriteFake();
        f.data.add(row("🇯🇵 Tóquio",35.68)); f.data.add(row("🇳🇿 Auckland",-36.85));
        ShortcutFake s = new ShortcutFake();
        NativeMenuCoordinator nativeUi = new NativeMenuCoordinator(f,s);

        nativeUi.moveFavorite(1,0);
        check(f.data.get(0).get("name").equals("🇳🇿 Auckland"),"arraste na lista ativa");
        nativeUi.renameFavorite(0,"🇳🇿 Nova Zelândia");
        check(f.data.get(0).get("name").equals("🇳🇿 Nova Zelândia"),"nome ativo alterado");
        check(f.data.get(0).get("lat").equals(-36.85),"latitude preservada");
        check(f.saves==2 && f.renders==2,"persistiu e atualizou duas vezes");

        nativeUi.moveShortcut(CustomShortcutsModel.SNIPER_II,0);
        check(s.model.all().get(0).id.equals(CustomShortcutsModel.SNIPER_II),"atalhos reordenados");
        nativeUi.setShortcutEnabled(CustomShortcutsModel.SNIPER_II,true);
        check(s.model.get(CustomShortcutsModel.SNIPER_II).enabled,"toggle nativo");
        check(s.model.get(CustomShortcutsModel.SNIPER_I).feedFilterId.equals("filtro1"),"configuração antiga preservada");

        boolean rejected=false;
        try{ nativeUi.enableExtraSniperSlots(); }catch(UnsupportedOperationException e){rejected=true;}
        check(rejected,"bloqueia Sniper III/IV sem motor real");
        s.supports=true;
        nativeUi.enableExtraSniperSlots();
        check(s.model.all().size()==5,"só adiciona III e IV após comprovar motor");

        f.failSave=true; rejected=false;
        try{nativeUi.renameFavorite(0,"NÃO DEVE SALVAR");}catch(IllegalStateException e){rejected=true;}
        check(rejected && !f.data.get(0).get("name").equals("NÃO DEVE SALVAR"),"salvamento falhou sem substituir a lista");
        f.failSave=false;

        String before=(String)f.data.get(0).get("name");
        f.failRefresh=true;rejected=false;
        try{nativeUi.renameFavorite(0,"Temp");}catch(RuntimeException e){rejected=true;}
        check(rejected && before.equals(f.data.get(0).get("name")),"rollback da lista quando menu falhou");
        System.out.println("OK: port nativo simulado, salvar, renderizar, rollback, toggles e bloqueio III/IV");
    }
}
