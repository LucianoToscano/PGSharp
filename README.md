# PGSharp — FONTE CONSOLIDADA, TESTES E COMPILADOR EXPERIMENTAL

> **Consolidação — 22/11/2026:** o código-fonte próprio do antigo `PGSharp-Privado` foi integralmente incorporado a este repositório pelo [PR #1](https://github.com/LucianoToscano/PGSharp/pull/1). O repositório público foi renomeado para `PGSharp`. O workflow de testes e compilação agora usa a própria fonte local, sem fazer checkout ou buscar artefatos do repositório privado. O conteúdo anterior foi preservado nos backups do Drive e em `docs/legado/`. O modo APK continua experimental e requer a configuração separada de uma fonte autorizada de APK-base via secret; nenhuma instalação funcional foi validada.

Este repositório concentra **fonte própria, scripts de build, testes, documentação e a central de compilação experimental**; não armazena dados pessoais, credenciais, chaves nem APK original.

| Repositório | Função |
| --- | --- |
| [PGSharp](https://github.com/LucianoToscano/PGSharp) | Repositório público canônico, com fonte própria, testes, editor, documentação e build experimental |
| Acervo histórico (Google Drive) | Backups verificados de PGSharp-Pessoal e PGSharp-Privado, com históricos Git completos |

## Workflow

[Executar gerador](https://github.com/LucianoToscano/PGSharp/actions/workflows/gerar-apk.yml)

- **validar:** usa o código deste repositório e executa testes Python; não gera APK nem depende do repositório antigo.
- **apk:** usa a fonte deste repositório e um APK-base obtido de uma origem privada autorizada configurada por secret; tenta recompilar e assinar com sua própria chave.
- O APK gerado é colocado em **artefato 7z criptografado**, com retenção de 1 dia.
- Nenhum APK original, DAT, JSON de inventário ou segredo deve ser enviado ao repositório público.

## Configuração obrigatória (uma vez)

Em **Settings > Secrets and variables > Actions > New repository secret**, neste repositório público, crie:

1. PGSHARP_BASE_APK_URL — URL **HTTPS privada e autorizada** para baixar temporariamente o APK-base original no modo `apk`; configure somente após assegurar direitos de uso. O modo `validar` não precisa deste secret. Evite URLs públicas ou compartilhamentos permanentes.
2. PGSHARP_SIGNING_KEYSTORE_B64 — representação Base64 de **uma chave de assinatura criada por você**; a chave precisa ser mantida para todas as futuras atualizações.
3. PGSHARP_SIGNING_PASSWORD — senha da chave.
4. PGSHARP_SIGNING_ALIAS — alias, por exemplo pgspessoal.
5. PGSHARP_ARTIFACT_PASSWORD — senha longa de criptografia do arquivo 7z.

### Como criar sua chave localmente
Com JDK instalado:

~~~bash
keytool -genkeypair -v -keystore pgspessoal.jks -storetype PKCS12 -alias pgspessoal -keyalg RSA -keysize 3072 -validity 10000 -dname "CN=PGS Pessoal,O=Uso Particular,C=BR"
~~~

Copie o arquivo .jks para local seguro, sem enviá-lo a git. No PowerShell, converta-o para o segredo Base64:

~~~powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\caminho\pgspessoal.jks")) | Set-Clipboard
~~~

Cole a string no secret PGSHARP_SIGNING_KEYSTORE_B64. Para gerar atualizações instaláveis sobre **o próprio APK personalizado**, use sempre a **mesma chave**, e não uma chave nova a cada build. O APK modificado não poderá atualizar por cima a instalação original com assinatura diferente.

### APK-base privado
O modo `apk` lê `secrets.PGSHARP_BASE_APK_URL`. Aponte-o para um arquivo `original.apk` em HTTPS privado/autorizado, acessível ao runner durante a execução (por exemplo um link temporário autorizado que expire). **Não publique APK proprietário em um release público nem coloque senhas no YAML, nos logs ou no Git.** Se o secret estiver ausente, o modo `apk` falha de forma explícita; isso não afeta `validar`.

### Gerar
Abra **Actions > PGSharp - validação e APK experimental > Run workflow**:
1. modo validar — testa os scripts da fonte local e dispensa tokens de repositório privado.
2. modo apk — monta, assina e empacota o APK experimental.

Baixe o artefato criptografado da execução e extraia com a senha PGSHARP_ARTIFACT_PASSWORD. Cuidado: essa senha não é recuperável pelo GitHub.

## Situação atual
A fonte experimental está no repositório público canônico PGSharp. **Ainda não há APK compilado, instalado ou testado neste fluxo.** Só execute após configurar os segredos e o APK base. Modificações do aplicativo de terceiros podem deixar login, atualização, integração Unity e verificações de integridade inoperantes e trazer riscos para a conta de jogo.

## Continuação de 05/11/2026
- Correção no workflow público: utilização do `sdkmanager` já presente no runner Ubuntu, evitando a falha do pacote Android SDK `tools` removido.
- Histórico: testes Java + D8 + inserção em APK sintético foram aprovados em uma execução do antigo repositório privado (registro preservado no backup do Drive).
- Nenhuma execução pública de `gerar-apk.yml` foi encontrada no momento da auditoria.
- Uma origem autorizada de APK-base ainda precisa ser configurada via secret `PGSHARP_BASE_APK_URL` para que o modo `apk` possa completar.
- Não é possível verificar os valores dos GitHub Actions secrets por esta integração. Confirme-os nas configurações do repositório público.
- O editor atualmente edita uma cópia do DAT; **não** modifica de imediato os favoritos ativos do aplicativo.

## Pré-validação pública

O workflow `ambiente.yml` verifica Java, Android SDK 35, D8, zipalign, apksigner e Apktool, **sem** baixar o APK original ou acessar segredos. Um resultado verde significa somente que as ferramentas do runner estão disponíveis.

A configuração inicial dos secrets pode ser feita no PC Windows; o script histórico `scripts/primeira_compilacao.ps1` foi incorporado neste repositório, mas suas referências legadas à release privada devem ser adaptadas antes de usar a configuração automática. O script não transmite dados da conta para este repositório. Depois do disparo, acompanhe a execução de `gerar-apk.yml` e baixe o artefato 7z criptografado, caso o build seja bem-sucedido.

---
## Pesquisa consolidada sobre feeds e scanner — 22/11/2026

**Finalidade deste adendo:** preservar no repositório canônico PGSharp os achados deste chat para consulta futura, **sem criar um segundo arquivo mestre**, sem importar código fechado de terceiros, sem modificar o compilador experimental, sem configurar endpoint privado e sem gerar/publicar APK. A situação descrita no início do README continua válida. A existência de repositório público chamado PGSharp **não** significa afiliação ou permissão de uso do código/serviços proprietários de PGSharp.

### Achados por arquitetura

| Categoria | Projetos/documentação | Interpretação correta |
| --- | --- | --- |
| Scan ativo histórico | [spawnScan](https://github.com/TBTerra/spawnScan), [Monocle](https://github.com/ZeChrales/Monocle), [fork Monocle](https://github.com/Hydro74000/Monocle), [RocketMap](https://github.com/RocketMap/RocketMap), [RealDeviceMap](https://github.com/RealDeviceMap/RealDeviceMap), [MAD](https://github.com/Map-A-Droid/MAD) | Referências de geodados, expiração e workers; não provam funcionamento atual ou direito de interrogar backend do jogo. |
| Agregação de mapas | [PKMSpawn](https://github.com/ashokvarmamatta/pkmspawn), [ScanGO](https://github.com/pompobit/ScanGO), [Pogo-Scanner-Android](https://github.com/azlan194/Pogo-Scanner-Android) | UI, WebView, seleção e normalização; listas de fontes e dependência de tokens não constituem feed público operacional. |
| Feeder JSON/HTTP | [PSSnipeFeeder](https://github.com/walaoaaa1234/PSSnipeFeeder), [PogoLocationFeeder](https://github.com/5andr0/PogoLocationFeeder) | Modelos antigos de eventos/coordenadas com validade; não incluir como provedores reais sem nova verificação. |
| Pipeline modular | [Golbat](https://github.com/UnownHash/Golbat), [ReactMap](https://github.com/WatWowMap/ReactMap), [PoracleNG](https://github.com/jfberry/PoracleNG), [Koji](https://github.com/TurtIeSocks/Koji) | Entrada/normalização, armazenamento, mapa, geofence e alertas são serviços distintos; nenhum oferece sozinho API pública autorizada de spawns. ReactMap v1.59.0 de 19/11/2026 foi verificado. |
| Catálogo e teste | [PokéAPI](https://pokeapi.co/docs/v2), [POGOProtos](https://github.com/AeonLucid/POGOProtos), [Appium UiAutomator2](https://github.com/appium/appium-uiautomator2-driver) | PokéAPI é catálogo sem posição; Protobuf histórico não concede acesso; Appium serve para testes autorizados da UI própria. |

**Conclusão da pesquisa em 22/11/2026:** não foi validada uma API pública, documentada, autorizada e operacional de aparições ao vivo de Pokémon GO. Não afirmar risco nulo, fonte real, dados atuais, compatibilidade binária, acesso autorizado de terceiros ou suporte do PGSharp sem evidência contratual e teste específico. Citar produto proprietário ou repositório acadêmico **não** significa autorização para copiar APIs fechadas, endpoints, binários ou credenciais.

### Modelo e artefatos discutidos

Os anexos da conversa incluem `SpawnFeedKit.kt`, estudo HTML de arquitetura, `pesquisa-pokemon-go-android-arquitetura.zip`, `pesquisa-pokemon-go-android-validada.zip`, relatório `pesquisa_pokemon_go_scanners.zip` (Markdown sem código Android) e kit `PokeSearch_Observacoes_v2_2026-11-22.zip`. O kit de observações v2, **externo a este repositório**, fornece funções Kotlin puras para validação de coordenadas, Haversine, parsing de mapas JSON já obtidos, cache/deduplicação por fonte+ID, filtros e GPX com proveniência.

Contrato de referência: `speciesId`, `coordinate`, `observedAt`, `despawnsAt?`, `cp?`, `ivPercent?`, `form?`, `source`, `kind`. Estados `LIVE/HISTORICAL/ESTIMATED/SIMULATED` não são equivalentes; o consumidor deve distinguir `SOURCE_REPORTS_ACTIVE`, `RECENT_UNCONFIRMED`, `STALE`, `EXPIRED` etc. Ausência de despawn não significa spawn ativo. Nunca presumir IV, CP, forma, horário ou validade quando ausentes. Dois filtros possíveis para cliente próprio: espécies/atributos e distância/tempo/região; **apenas proposta**, sem implementação neste repositório.

**Evidência de QA:** `bash testar.sh` do kit externo retornou **37 testes Kotlin/JVM aprovados** em 22/11/2026. Sem testes Gradle, APK experimental, compatibilidade com jogo, conexão de feed ao vivo, interface física ou instalação. O kit não implementa teleporte, captura automática nem acesso à API privada do jogo.

**Preservação/segurança:** não alterar ou publicar `PGSData.dat`, `PGSStats.json`, APK-base, tokens, URLs secretas de fornecedores, certificados ou contas. Todo uso de fonte de terceiros deve respeitar contratos e licenças. Permanece necessário identificar a origem e a autorização, validar schemas e timestamps, fazer testes contra dados sintéticos e verificar em dispositivo próprio antes de marcar funcionalidade como pronta. Separar qualquer trabalho futuro em PR própria; esta alteração é somente documentação.


---

## Registro transversal de 22/11/2026 — APKs, menus, sprites, Quick Sniper 100 IV e linha PGBR

**Função deste registro:** acrescentar ao README canônico do repositório **próprio/experimental** PGSharp a pesquisa feita com APKs, ZIPs, vídeos e código de referência. Não transformar o repositório em distribuição do PGSharp comercial, não ligar endpoints sem contrato validado e não declarar que um plugin interno é APK instalável. Todos os arquivos originais do Drive e o workflow existente de compilação permanecem intocados.

### Bibliografia e acervo documentado
- [Pasta PGSharp no Drive](https://drive.google.com/drive/folders/1WAEmEpZmvUw9dDiapNcygtmZQq6XIB_j); [auditoria visual](https://drive.google.com/file/d/11_OSo_6hPaXeRB5GW-HLXbVSr5HyugyO/view) de **16 imagens inspecionadas e 20 vídeos por amostragem de frames**; [CSV de mídia](https://drive.google.com/file/d/1pvXnzt41spOD82q1u1_i5ta4Xnm0a2SO/view); [biblioteca de recursos extraídos](https://drive.google.com/drive/folders/1HHNySaQo9w6NU3XuoQX0KBbTUy6ds46O).
- [Auditoria 14 APK/ZIP e sprites](https://drive.google.com/file/d/1PjXJr87jK2F801_lz7A7x9OK0_Bhmyy9/view), [menus/layouts/código/licenças](https://drive.google.com/file/d/1aKp7Is0q6yFenLdBsiG2D582T6D_ha5u/view), [mapeamento binário profundo](https://drive.google.com/file/d/1XZbdk3E3EWuZI1UwAd-9xbbpXnseznUQ/view), [artigo de Romain Thomas](https://www.romainthomas.fr/post/21-11-pgsharp-analysis/), [repositório histórico de símbolos](https://github.com/romainthomas/pgsharp) e [slides Ekoparty 2021](https://www.romainthomas.fr/publication/21-ekoparty-mobile-hacking-space-pgsharp/21-10-ekoparty-mobile-hacking-space-pgsharp.pdf).
- Os vídeos **11/12/21** mostram Radar Próximos e Snipers I/II distintos, barras vertical/horizontal, filtros por espécie/IV/nível/forma/distância/prioridade, lista 100% IV no modo Free e limitação dos outros filtros. Vídeos **13/14** exibem HUD externo de PvP com **Ataques ágeis, Ataques carregados e Shield**; vídeos 9/10/16/17/18, joystick móvel e pulso ciano de toque; vídeo 15, ocultação visual do nome. Capturas não provam quais métodos executaram as ações, nem a versão exata do cliente.

### Inventário confirmado dos APKs/plugins
- APK completo **1.278.0**: **211.194.913 bytes**, 2.204 entradas de contêiner, 163 bundles, 8 `.assets`, 18 `.resource`; APK completo **1.277.2**: **211.243.984 bytes**, 2.203 entradas. Comparar sempre os APKs completos, plugins internos, ZIPs decompilados e o minipacote de `libmain.so` (4 entradas) como **tipos diferentes**; igualdade de nome não significa igualdade de conteúdo.
- Plugin interno 1.278.0 reportado: **3.777 entradas**, **1.973 PNG** — 1.577 Pokémon, 64 itens e 332 imagens Android; 1.277.2: 3.757 entradas, mesmos 1.973 PNG e hashes raster; 1.33.0: 3.094 entradas, 1.746 PNG, 2 GIF, 1 JPG. O pacote do decompilador 1.277.2 contém 297 sprites Unity e 425 outras texturas Unity; são recursos de jogo, não automaticamente overlay PGSharp.
- Recurso de menu **não equivale a PNG**: 369 `res/drawable/`, 237 `res/layout/`, 32 `res/anim/`, 25 `res/animator/` e outros XML compilados, com `hl_mainmenu.xml`, `hl_mainmenu_item.xml`, `hl_shortcut_menu_item.xml`, `hl_shortcut_orientation_item.xml`, `hl_feeds.xml`, `hl_feed_results.xml`, `ic_feed1_white_24.xml`, `ic_feed2_white_24.xml`. A extração de 1.973 PNG **não cobria** todos os vetores, layouts e transições. Os arquivos originais PT/EN incluídos no plugin contêm **533 posições de texto alinhadas** em catálogo CSV.
- Joystick, caminhos `res/drawable/ui_pic_joystick_*`: `arrow.png` **588×588**, `right_pad.png` **300×300**, `control_ball_mini.png` **50×50** são arte útil; `control_ball.png` e `left_pad.png` são **1×1 preto**. Os cinco arquivos são byte a byte iguais nos plugins 1.33.0, 1.277.2 e 1.278.0. O pulso ciano visto no toque **não foi identificado como PNG independente**; XML `hl_map_control_touch.xml`, vetores de toque e texturas genéricas Unity não provam o mecanismo ou a textura exata. PNGs reconstruídos de vídeos são **derivados**, não extrações originais. Guardar proveniência, dimensões, hashes e restrições de uso por arquivo.
- As classes próprias [`NativeMenuCoordinator`](https://github.com/LucianoToscano/PGSharp/blob/main/src/com/pgs/personal/NativeMenuCoordinator.java), `CustomShortcutsModel`, `LiveFavoritesOps` e `FavoritesOverlay` podem ser evoluídas no repositório próprio. [PGRazor](https://github.com/jpitty03/PGRazor) possui código MIT (como `Pad.java`, `SettingsPanel.java` e `Control.java`), **mas não concede licença MIT aos assets Pokémon de terceiros**. DEX/IL2CPP, texturas do jogo e APK proprietário não têm direitos de redistribuição automaticamente demonstrados.

### Pesquisa de componentes internos e feeds
- Estudos históricos 1.33.0: Lua customizada, scripts `location.luac`, `pgo.luac`, `pgodump.luac`, símbolos antigos de `hook_MapContentHandler__UpdateCells`, `hook_Map_PoiDirectoryService__AddPokemon/RemovePokemon` e `luaD_precall`; a ponte `me.underworld.helaplugin.PL` apresenta `NRL/NSMTC`, e a ação `plg.map.tp` (ID 3) foi reportada nesse contexto. **Offsets/semântica antigos não são intercambiáveis com versões novas**.
- Na 1.278.0, análise estática reportou `me.underw.hp.PL`, `NRL`, `NSMTC`, `PUL`, estados `FASTSNIPE`/`FASTSNIPE2`, classes `q4.d(...)` (consulta) e `q4.e(...)` (tratamento `lat/lng`), além dos caminhos textuais `f.pgsharp.com/peanuts` (Pokémon), `/walnut` (stops), `/macadamia` (quests), `/pistachio` (raids). Identificação de string **não prova API pública, contrato de autenticação, host acessível ou resultado 100 IV real**. As tentativas de instalar diretamente plugins recuperados falharam; dependem de carregador/assinatura/contexto do host.
- O `libmain.so` 1.278.0 analisado tem SHA-256 `912b8a2d064b82eed1fe68c704a7dee2e89394846bc16dcb3e71c46a292d60e3`; `JNI_OnLoad` candidato `0x1C9734`; strings `pvpCombatStats`, `battleAssistant`, `rocket.shield` e `WildMapPokemon` **não comprovam automação PvP**, seleção de golpe supereficaz, feed remoto ou compatibilidade de hooks em novos binários. Para monitorar módulos próprios, priorizar ADB/Logcat e inspeção Java com JADX; Ghidra e observação JNI autorizada podem ajudar a estabelecer cadeias de chamadas. Keygen não identifica funções nem substitui validação real de licença.
- O proprietário já declarou autorização para APKs/APIs e a registrou no [Guia Mestre PokeSearch LT364/LT365](https://github.com/LucianoToscano/PokeSearch/blob/main/docs/GUIA_MESTRE.md); **não repetir a mesma exigência de declaração**. Verificar escopo técnico, credenciais, protocolo documentado, autenticação de key, limites de serviço e permissão de redistribuição ao construir efetivamente um conector.

### O que foi implementado e publicado no projeto PGBR (projeto distinto)
- [PR #72](https://github.com/LucianoToscano/PGBR/pull/72): leitura **manual** de 100 IV via `hundos.json` do PGBR/**FaustusFoundry**, com dados validados, resultados, copiar coordenadas e teleporte confirmado; **não consome os mesmos resultados do PGSharp**. [PR #73](https://github.com/LucianoToscano/PGBR/pull/73): menu PGSharp **Free 100 IV / Standard All Coordinates**, separado, com cofre local da key (AES-GCM/AndroidKeyStore), **sem transmissão, autenticação ou liberação remota de filtros**. A presença da opção de interface **não ativa a API PGSharp**.
- [PR #74](https://github.com/LucianoToscano/PGBR/pull/74) integrou a **[LT034 QA](https://github.com/LucianoToscano/PGBR/releases/tag/v0.00.34-LT)**, APK sem Root, Root e módulo Zygisk publicados e verificados pelo workflow [#45013312358](https://github.com/LucianoToscano/PGBR/actions/runs/45013312358); [PR #75](https://github.com/LucianoToscano/PGBR/pull/75) e [PR #77](https://github.com/LucianoToscano/PGBR/pull/77) documentaram hashes, testes e downloads. A [**LT035 QA**](https://github.com/LucianoToscano/PGBR/releases/tag/v0.00.35-LT) é release mais recente verificada nesta consulta, com pesquisa/observação PvP separada; **não converteu** a chave PGSharp em login nem provou PvP completo em aparelho.
- **Projetos separados:** PGBR, PokeSearch, PvPPokeGo e este PGSharp têm Mestres, branches, aplicativos e releases próprios. Este adendo ao README é **somente documental**; nenhum APK comercial, script, código de runtime, key, servidor, release ou elemento Unity foi alterado. Próximos passos de desenvolvimento requerem PR independente e teste físico com parâmetros reproduzíveis.
