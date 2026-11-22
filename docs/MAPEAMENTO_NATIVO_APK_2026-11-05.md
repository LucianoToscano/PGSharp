# Diagnóstico técnico para upgrade dos menus nativos do PGSharp
Data: 05/11/2026

## Pedido e condições inegociáveis
- Inserir alças ☰ DENTRO das páginas originais **Favoritos** e **Atalhos Customizados**.
- Segurar/arrastar/soltar com salvamento persistente dos favoritos ou atalhos ativos.
- Renomear dentro da lista; não criar WebView, overlay flutuante, menu paralelo ou editor DAT.
- Incluir Sniper Rápido III e IV somente se cada um possuir backend de feed independente, não apenas cópias visuais do I e II.

## Inspeção estática efetuada no APK disponibilizado pelo usuário
Arquivo: `pgs1.276.2_0.435.1_CCsqE(arm64) (1).apk` (APK não versionado; não disponibilizar publicamente).

- APK base ~201 MiB.
- `lib/arm64-v8a/libmain.so`: aproximadamente 36,7 MB. ELF AArch64, `DT_SONAME=libhela.so`. Contém a URL `https://www.pgsharp.com/download64`, sendo um possível componente de integração PGSharp, mas sem prova de que seja controlador de menu.
- `lib/arm64-v8a/libil2cpp.so`: ~241 MB não comprimidos, engine IL2CPP.
- `lib/arm64-v8a/libunity.so`: ~23,5 MB não comprimidos.
- A tabela de símbolos dinâmicos de `libmain.so` expõe `JNI_OnLoad` mas não nomes reconhecíveis de APIs de lista `Favorites`, `Custom Shortcuts` ou `Sniper`.
- Procura textual direta pelas chaves do DAT e nomes dos menus não recuperou um controlador de favoritos nas bibliotecas/DEX; UI e persistência podem estar nativos, ofuscados, gerados ou em outro arquivo. Esse resultado não autoriza concluir onde estão implementados.
- O `global-metadata.dat` IL2CPP contém símbolos genéricos relacionados a `Favorite` e `Shortcut`, mas nenhum mapeamento comprovado para os widgets PGSharp.

## Código disponível e lacuna fundamental
- `LiveFavoritesOps.java`, `CustomShortcutsModel.java` e `NativeMenuCoordinator.java` implementam transformações de dados e lógica de persistência abstrata.
- A classe `FavoritesOverlay.java` abre outro editor (explicitamente rejeitado pelo usuário).
- **Não existe** implementação concreta de `NativeMenuCoordinator.FavoritesPort` e `ShortcutsPort` que acesse a interface ativa do PGSharp.
- Os testes automatizados atuais são unitários e fazem uso de portas simuladas; não demonstram modificação real em Unity.
- A tentativa de pré-carga no PlayerPrefs não alterou o menu, conforme captura do usuário. Copiar dados do DAT para dentro do APK não equivale a conectar o motor de menus.

## Critério mínimo para liberar APK com essas melhorias
1. Identificar controlador de lista nativa ativo, mecanismo real de salvamento e eventos de toque.
2. Criar integração dentro da hierarquia de UI usada pelo PGSharp, sem inventar uma segunda tela.
3. Testar o gesto ☰, edição e persistência após fechar/reabrir e reiniciar o app.
4. Mostrar dois feeds adicionais REALMENTE independentes antes de rotular III e IV.
5. Testar no dispositivo Android e registrar resultado. Assinatura/apktool/DEX válidos, sozinhos, não cumprem esse critério.

## Estado
**Bloqueado para build de funcionalidades solicitadas.** Não declarar um APK recompilado por troca de DEX ou PlayerPrefs como edição nativa concluída, pois isso reproduziria o defeito anterior.
