# Atalhos Customizados: reordenar inline e Sniper III/IV

Data de referência: 05/11/2026. Base: captura do menu original "Atalhos Customizados" enviada pelo usuário e análise do `PGSData.dat` recebido (somente leitura, arquivo não enviado ao GitHub).

## Problema e comportamento solicitado
- A tela original lista atalhos com ícone à esquerda, título ao centro e controle de ativar/desativar à direita. Existem "Sniper Rápido I" e "Sniper Rápido II".
- Pedido: poder modificar diretamente a ordem pelo gesto de segurar ~600 ms ou pelo pequeno marcador **☰** na mesma linha; salvar após soltar.
- Adicionar **Sniper Rápido III e IV**, cada um com ativação, posição e filtro próprios, **somente se o motor nativo suportar dois feeds adicionais reais**.
- Não criar menu flutuante ou editor de DAT. Manter "Somente Ícones" e controles de cada atalho, sem interceptar os toques dos switches.

## Descoberta concreta no DAT
A chave top-level `hlshortcuts` é uma **string JSON**. Seu conteúdo possui:
- `items`: mapa `identificador → boolean` dos atalhos exibidos;
- `title`: boolean;
- `orientation`: número.

Identificadores observados para os Snipers:
- `hlfastsnipe` = Sniper Rápido I;
- `hlfastsnipe2` = Sniper Rápido II.

Na seção observada não havia `hlfastsnipe3`, `hlfastsnipe4` ou um campo explícito com a ordem das linhas. Logo, **a configuração exportada não oferece uma maneira demonstrada de criar novos widgets nem de alterar a ordem do menu original**. Inserir campos desconhecidos no JSON não prova suporte do motor nativo.

A pesquisa por esses identificadores em texto simples nos arquivos DEX e bibliotecas nativas do APK referencial não encontrou os símbolos: o código relevante pode estar ofuscado, compactado ou em outro formato. Não temos um vínculo identificado com o código que constrói a UI, nem com a persistência ativa do PGSharp.

## Plano visual
1. Alça **☰** discreta ao lado do título, **antes do switch**, sem encurtar demais o texto; também habilitar toque prolongado sobre a linha.
2. Durante arraste, elevar visualmente a linha, mover a prévia e rolar automaticamente ao aproximar das extremidades.
3. Ao soltar, chamar persistência original e reconstruir o menu preservando toggles, configurações e ordem.
4. Não mover o cabeçalho "Somente Ícones", que é uma preferência global, nem alterar os valores dos switches.
5. Se o usuário soltar fora da lista, restaurar a posição anterior.
6. Testar reabertura e reinicialização para assegurar que a ordenação persistiu.
7. Só depois de identificar e integrar os widgets reais, acrescentar Snipers III e IV com filtros independentes e estado inicialmente desligado, evitando duplicar por engano um dos dois feeds existentes.

## Código criado — e limite técnico
- `src/com/pgs/personal/CustomShortcutsModel.java`: núcleo de mudanças de posição, toggle e filtro por ID; ids reais para I e II, IDs reservados para III e IV.
- `tests/CustomShortcutsModelTest.java`: testa imutabilidade, movimento, independência dos filtros, estados, idempotência e bloqueio de III/IV sem suporte nativo.
- `src/com/pgs/personal/LiveFavoritesOps.java`: operações de listas, igualmente sem adaptador nativo.

**Nenhuma dessas classes altera por si só a tela Unity/PGSharp.** Não existe neste projeto a rotina comprovada para captar as linhas ativas, criar as alças nem salvar sua ordem dentro da interface original.

## Critérios de entrega de um APK correto
- ☰ visível na tela "Atalhos Customizados" original e favoritos originais, sem panel HTML;
- arraste funcional em aparelho com menus originais;
- persistência após reiniciar;
- switches sem regressões;
- dois painéis novos realmente independentes, se III/IV forem anunciados;
- preservação de login, configuração, localização, rotas e filtros;
- build assinado e testado no Android, não apenas estrutura ZIP.

### Situação atual
Requisitos e modelo testável preparados, integração nativa de interface e motor de feeds **pendente**. Não gerar versão como "corrigida" enquanto não satisfizer os critérios acima.
