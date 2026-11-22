import com.pgs.personal.LiveFavoritesOps;
import java.util.*;

public class LiveFavoritesOpsTest {
    static void require(boolean ok, String msg) { if (!ok) throw new AssertionError(msg); }
    static Map<String,Object> row(String name, int iv) {
        Map<String,Object> m = new HashMap<>();
        m.put("name",name);m.put("minIV",iv);m.put("pokemons",Arrays.asList(25,150));
        m.put("priority",0);return m;
    }
    public static void main(String[] args) {
        List<Map<String,Object>> original = new ArrayList<>();
        original.add(row("Geral 🌎",100));original.add(row("PvP 🥇",90));original.add(row("Mega ⚡",80));
        List<Map<String,Object>> moved = LiveFavoritesOps.reordered(original,2,0);
        require(moved.get(0).get("name").equals("Mega ⚡"),"Mover 2→0");
        require(original.get(0).get("name").equals("Geral 🌎"),"Não alterar lista original");
        require(LiveFavoritesOps.reordered(moved,0,2).get(2).get("name").equals("Mega ⚡"),"Mover 0→2");
        List<Map<String,Object>> renamed = LiveFavoritesOps.renamed(moved,1,"  Especial ☰ ⚔️  ");
        require(renamed.get(1).get("name").equals("Especial ☰ ⚔️"),"Nome e emojis");
        require(renamed.get(1).get("minIV").equals(100),"Preservar IV");
        require(renamed.get(1).get("priority").equals(0),"Preservar prioridade");
        require(moved.get(1).get("name").equals("Geral 🌎"),"Sem efeito colateral");
        boolean rejected=false;
        try {LiveFavoritesOps.reordered(original,-1,0);}catch(IndexOutOfBoundsException ex){rejected=true;}
        require(rejected,"Posição inválida");
        rejected=false;
        try {LiveFavoritesOps.renamed(original,0," ");}catch(IllegalArgumentException ex){rejected=true;}
        require(rejected,"Nome vazio");
        System.out.println("OK: mover, renomear, emojis, integridade e validações");
    }
}
