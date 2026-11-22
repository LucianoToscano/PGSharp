# Favoritos diretamente na página do PGSharp — substitui o editor separado

Data: 05/11/2026. Pedido: atualizar **a própria página de favoritos existente**, não inserir um aplicativo/editor HTML separado.

## Experiência exigida
- Exibir uma alça **☰ discreta no canto direito de cada linha** quando o modo de edição estiver ativo.
- **Segurar por aproximadamente 600 ms** uma linha também deve permitir começar a arrastar (opcional em configurações).
- Durante arraste: acompanhar o dedo, pré-visualizar posição, rolar perto das bordas e não disparar a ação normal de abrir o filtro.
- Soltar: reordenar, persistir imediatamente e redesenhar **a mesma lista ativa**.
- Botão pequeno para renomear no próprio menu, preservando emojis, espécie, IV, prioridade e filtros.
- Manter favoritos de Pokémon, missões, raides, locais e rotas separados.
- Permitir desfazer a última mudança sem exigir importar/exportar arquivos.
- Quando o modo de edição estiver desligado, toque e rolagem original permanecem intactos.
- Não adicionar uma janela flutuante ★, WebView, seletor DAT ou painel separado ao fluxo normal.

## Evidência técnica da versão inspecionada
Arquivo de referência: `pgs1.276.2_0.435.1_CCsqE(arm64) (1).apk` fornecido pelo usuário (não versionado).
- APK usa Unity/IL2CPP e possui `lib/arm64-v8a/libmain.so`, `lib/arm64-v8a/libil2cpp.so` e `lib/arm64-v8a/libunity.so`.
- Pesquisas nos três DEX de nomes `hlfeeds`, `hlfeedsquest`, `hlfeedsraid`, `hlfavor`, `hlfavorRoute` não encontraram esses identificadores.
- Esses identificadores são chaves das cinco listas do **arquivo exportado DAT**, não prova de que o aplicativo as mantém como preferências Java acessíveis.
- `libmain.so` contém referências ao domínio do PGSharp, mas isso não identifica as funções de UI ou persistência.
- A atividade Java `UnityMainActivity` somente permite acrescentar um painel; o gancho antigo **não** acessa a lista nativa de favoritos.
- Testes atuais demonstram edição/exportação DAT e compilação do painel Java; **não demonstram edição em tempo real no PGSharp**.

## Contrato de integração necessário
O adaptador para a lista nativa precisa:
1. Identificar a tela de favoritos e obter os objetos de linha presentes em memória com identidade estável.
2. Identificar gesto de toque/arraste no sistema de eventos dessa tela e desenhar a alça ☰ dentro de cada linha.
3. Mover/renomear no modelo de dados **ativo**, evitando simular um toque em outra interface.
4. Acionar mecanismo de salvamento nativo e invalidar/recarregar a lista.
5. Verificar a posição após fechar/reabrir a lista e reiniciar o app.
6. Falhar com segurança se o método não estiver identificado nessa versão do PGSharp.

`LiveFavoritesOps` implementa somente as **transformações testáveis** de mover/renomear listas. **Ainda não existe o adaptador nativo.**

## Critérios de aceitação antes de gerar outro APK
- A alça ☰ aparece nas linhas da página original do PGSharp.
- O arraste move linhas diante do usuário sem abrir outro editor.
- A alteração persiste após fechar/reabrir favoritos e reiniciar o jogo.
- Renomear funciona no próprio menu, incluindo emojis Unicode.
- Todos os campos não alterados são preservados e há proteção contra perda de dados.
- Teste em Android real com backup e relatório de falhas.
- Nada é declarado como implementado até comprovação do comportamento real.

## Próximos dados úteis para mapear a interface
Captura da **página real de favoritos PGSharp aberta** e, para diagnóstico de integração nativa, dados de logcat do dispositivo durante entrar/sair do menu, sem credenciais ou informações da conta. Esses elementos ajudam a localizar os controles, mas não substituem a identificação do código proprietário e do mecanismo de persistência.
