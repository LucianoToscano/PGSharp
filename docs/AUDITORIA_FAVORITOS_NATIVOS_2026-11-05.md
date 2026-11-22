# Auditoria: favoritos da página original do PGSharp

**Data:** 05/11/2026
**Referência:** captura do menu Favoritos → Pontos enviada pelo usuário, mostrando a lista original inalterada depois de instalar o APK "Preferências Nativas Experimental".

## Resultado observado
- A interface continuou sem alças ☰ ou gesto nativo de arraste.
- Os favoritos já existentes continuaram visíveis, na ordem anterior.
- O APK experimental anterior somente acrescentava valores `hlfeeds`, `hlfeedsquest`, `hlfeedsraid`, `hlfavor`, `hlfavorRoute` **se ausentes** nas preferências que ele presumiu serem lidas pelo PGSharp.
- Não foi localizado nem integrado o controlador de lista ativa (Unity/IL2CPP/PGSharp), a função de salvar ordenação nem a edição inline do nome.
- A ausência de diferença visual é portanto esperada e **não é prova de integração nativa**. A hipótese de armazenamento por PlayerPrefs não foi validada no dispositivo.

## Auditoria estática do APK de referência
- APK referencial: `pgs1.276.2_0.435.1_CCsqE(arm64) (1).apk`, fornecido pelo usuário, não versionado.
- Varredura dos arquivos contidos no APK, inclusive DEX, bibliotecas nativas e metadata IL2CPP: as chaves textuais `hlfeeds`, `hlfeedsquest`, `hlfeedsraid`, `hlfavor`, `hlfavorRoute` **não aparecem em texto claro**.
- `assets/bin/Data/Managed/Metadata/global-metadata.dat` contém diversas ocorrências genéricas de `Favorites`, mas nenhuma delas localiza com segurança o controlador de favoritos PGSharp.
- Esse resultado não prova ausência da funcionalidade: ela pode usar nomes hashados, código ofuscado ou outro mecanismo de armazenamento.

## Requisito correto do usuário
1. No menu original **Favoritos → Pontos / Rotas** e nos filtros do PGSharp, mostrar alça ☰ **dentro de cada linha**, preferencialmente antes da lixeira.
2. Permitir segurar uma linha por ~600 ms, arrastar e soltar, sem abrir editor, importador ou exportador.
3. Ao soltar, chamar a gravação **efetiva da lista ativa** e atualizar sua posição no próprio menu.
4. Renomear diretamente no menu, mantendo os dados de coordenadas, IVs, espécies, horários, prioridades, etc.
5. Preservar gesto normal de toque e rolagem quando não estiver no modo de edição.
6. Persistir depois de fechar/reabrir menu e reiniciar o app; testar no Android físico.

## Restrições técnicas atuais
- `LiveFavoritesOps.java` só reordena objetos **que tenham sido fornecidos explicitamente**; não lê, não modifica, nem persiste a lista ativa do PGSharp.
- O editor HTML/DAT não atende ao pedido de edição inline.
- A implementação de pré-carga experimental por Unity PlayerPrefs não altera os widgets da página original.
- Não é possível demonstrar esse upgrade apenas com um patch em `UnityMainActivity`. Sem mapear o controlador real da lista e a função de persistência, qualquer novo APK "com alças" seria enganoso.
- Não modificar sistemas de login, pagamento, licença ou defesa do aplicativo; manter os dados privados fora do repositório.

## Próxima etapa técnica antes de gerar APK
Identificar onde ficam as linhas ativas e o armazenamento de ordem e nome. Só então implementar o adaptador nativo `getRows -> move/rename -> save -> refresh` com teste de persistência. Não declarar concluído nem oferecer build como upgrade nativo sem a aceitação real.

## Situação
**Diagnóstico e critérios de aceitação documentados; integração nativa não implementada.**
