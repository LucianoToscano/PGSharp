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


<!-- REGISTRO PGSHARP 2026-11-22 — auditoria multimídia, engenharia reversa e integração -->
## Registro técnico transversal — PGSharp: binários, rastreamentos, vídeos e decisões (22/11/2026)

**Tipo:** registro cumulativo de pesquisa e referência visual, **não** comprovação de build, API autorizada, APK funcional, teleporte operacional ou PvP automatizado. **Origem:** investigação de chat com arquivos locais, material histórico de Romain Thomas e pasta PGSharp do Drive. O inventário de mídia foi lido do Drive e revisado. Todas as afirmações de endereços e fluxos binários exigem reprodução ao integrar em outra versão.

### Fontes e proveniência
- [Pasta PGSharp](https://drive.google.com/drive/folders/1WAEmEpZmvUw9dDiapNcygtmZQq6XIB_j); [auditoria visual de 36 arquivos](https://drive.google.com/file/d/11_OSo_6hPaXeRB5GW-HLXbVSr5HyugyO/view); [inventário CSV com IDs e descrições](https://drive.google.com/file/d/1pvXnzt41spOD82q1u1_i5ta4Xnm0a2SO/view); [dossiê ZIP](https://drive.google.com/file/d/12_EigP7_mA7pY7tXz4B4QsLiQ4pVyh3x/view).
- [Artigo de Romain Thomas sobre PGSharp (2021)](https://www.romainthomas.fr/post/21-11-pgsharp-analysis/); [apresentação Ekoparty](https://www.romainthomas.fr/publication/21-ekoparty-mobile-hacking-space-pgsharp/21-10-ekoparty-mobile-hacking-space-pgsharp.pdf); [repositório e `libmain.symbols`](https://github.com/romainthomas/pgsharp); [artigo Pokémon GO anti-Frida iOS](https://www.romainthomas.fr/post/21-07-pokemongo-anti-frida-jailbreak-bypass/) — o último é contexto histórico iOS, não mapa de funções Android atual.
- [Correlação histórica de símbolos e binários](https://drive.google.com/file/d/13gF5iC0aZgEabsnco3o72uIG-CYg4kmU/view); [extração textual ELF/JNI](https://drive.google.com/file/d/19fAxpqtCppc9gX-IX0eYcAg0oNDs-BCA/view); [mapeamento aprofundado v2](https://drive.google.com/file/d/1XZbdk3E3EWuZI1UwAd-9xbbpXnseznUQ/view).

### Auditoria visual — 16 imagens e 20 vídeos
As **16 imagens foram inspecionadas visualmente**; os **20 MP4 foram examinados por amostragem de quadros distribuídos na duração**, não exaustivamente quadro a quadro nem por instrumentação dinâmica. Os originais, caminhos e direitos de terceiros foram preservados. As gravações não identificam inequivocamente a versão PGSharp.
- **Quick Sniper I e II:** módulos e alternâncias independentes; orientação Vertical/Horizontal separada; o vídeo 12 mostra criação de filtros com espécies, IV, nível, gênero, forma, tamanho, distância, prioridade, notificações e barras de resultados coexistindo no mapa. Não confundir com o **Radar Próximos**, que tem filtros e notificações próprios (vídeo 11).
- **PvP:** vídeos 13 e 14 exibem três comandos de HUD distintos — **Ataques ágeis**, **Ataques carregados** e **Shield** —, golpes e trocas durante uma partida. A visualização **não prova execução automática**, decisão de escudo, seleção de ataque supereficaz ou disparo por hook nativo.
- **Movimento/localização:** favoritos, rotas/GPX, teleporte, velocidade, joystick flutuante com variantes claro/escuro e manopla arrastável, toque no mapa com pulso ciano, cooldown e histórico.
- **UI e privacidade:** menu flutuante e atalhos personalizáveis (ícones apenas ou ícone + texto; orientação; switches individuais); descarte de itens por regras; seleção de Pokémon com sprites/possíveis brilhantes; alternância de ocultação de nome apenas visual. Não alterar login/RPC/identidade real do treinador. A imagem promocional PGSharp_Config_LOT.png é cartaz ilustrado, não evidência de função, e seus QR codes não foram validados.

### Registro técnico reverso — versões e limites
- Estudo de 2021: PGSharp **1.33.0** com VM Lua customizada e bytecode `.luac` (`location.luac`, `pgodump.luac`, `pgo.luac`), ponte JNI, hooks IL2CPP/Unity. O arquivo `libmain.symbols` antigo nomeia `luaD_precall`, `init_lua_jni`, `hook_MapContentHandler__UpdateCells`, `hook_Map_PoiDirectoryService__AddPokemon/RemovePokemon` e `hook_Map_MapPokemon__TryCapture`. Os offsets de 2021 **não são reutilizáveis automaticamente em 2026**; símbolos `il2cpp_...` podem nomear *slots de ponteiro*, não instruções executáveis.
- PGSharp **1.278.0 / Pokémon GO 0.437.0**, biblioteca `libmain.so` ARM64 (SHA-256 `912b8a2d064b82eed1fe68c704a7dee2e89394846bc16dcb3e71c46a292d60e3`), `JNI_OnLoad=0x1C9734`, ELF parcialmente stripped. Inventários anteriores catalogaram 1.616 funções dinâmicas definidas na moderna e 1.595 na antiga, mais métodos `native` do jogo/SDKs; **não são contagens de recursos PGSharp**. Sem `mapping.txt` original ou reconstrução completa JNI → C validada.
- Extrações históricas deste chat reportaram APKs auxiliares embutidos nas `libmain.so` 1.33, 1.277.2 e 1.278.0, incluindo DEX da classe `PL`. Em 1.278 foram reportados XOR `0x35`, 3.777 entradas e classes `me.underw.hp.PL`; 1.277.2 XOR `0x23`, 3.757 entradas. Esses APKs **são componentes internos**, não instaladores completos, e tentativas no Android mostraram **“App não instalado”**. Assinatura e dependências de carregador faltantes precisam ser verificadas antes de qualquer uso.
- Referências DEX/strings reportadas na 1.278.0: `FASTSNIPE`, `FASTSNIPE2`, `hlfastsnipe*`, `pokemonFeed`, `FEEDS_BASIC/ADVANCED`, `SCANNEARBY`; `q4.d(...)` (preparação de consulta) e `q4.e(...)` (leitura `lat/lng`), `PL.NRL`, `PL.NSMTC`, `PL.PUL`, `HLVM`, `GL`. URLs literais reportadas em cliente: `f.pgsharp.com/peanuts` (Pokémon), `/walnut` (stops), `/macadamia` (quests), `/pistachio` (raids). **Nenhuma URL constitui API pública, contrato licenciado ou comprovação de acesso; nunca testar com credenciais de terceiros ou contornar autenticação.**
- Ponte histórica 1.33: ação 3 `plg.map.tp` via `PL.b(3, coords)` / `NRL`; o formato observado concatena componentes de `LatLng` com vírgula. **Não presumir comportamento ou assinatura equivalentes na moderna.** Hooks JNI de GPS 1.278 reportados na região `0x7ADE5C`, para `nativeAddLocationProviders`, `nativeLocationUpdate` e `nativeGpsStatusUpdate`; esses endereços exigem validação de endereço de execução, ABI e cliente-base em cada build.
- Correção de interpretação: `searchFilterPreferenceBase64` apareceu como preferência de dados de jogador, e `WildMapPokemon` como tipo do objeto no mapa; referências a strings **não demonstram** motor Quick Sniper, feed remoto ou endpoint funcional. Chaves `rocket.fast/charge/shield`, `pvpCombatStats` e `battleAssistant` **não provam** executor PvP automático.

### Procedimento de pesquisa e integração
Usar JADX/DEX e Ghidra para rastreio estático, ADB/Logcat/Perfetto e, apenas em ambiente autorizado, observação dinâmica Java/JNI/ARM64. Registrar versão, ABI, SHA, origem, timestamps, método tocado, entrada/saída e logs sem dados pessoais; separar Quick Sniper I/II, Radar Próximos, Rocket, PvP, ginásio/reide e Dynamax. Não usar keygen: não serve como rastreador de funções. Sem root, ADB é útil para logs; PC + Android de teste permite análise mais profunda; mesmo com root não há autorização implícita para APIs privadas.
**Prioridade:** (1) identificar callbacks de filtros e origem autorizada dos resultados; (2) confirmar seleção → `LatLng` → controle de localização; (3) validar independentemente HUD, golpes e escudos em testes controlados; (4) adaptar UI/joystick, privacidade e itens sem perder funcionalidades. Para assets, verificar licença e não importar sprites de capturas como se fossem PNGs transparentes originais.
**Resultado desta anotação:** documentação/referências; nenhum APK, feed ao vivo, batalha automática, assinatura ou autorização de backend foi criado ou homologado. Não há modificação no aplicativo comercial PGSharp, nem alteração da fonte oficial de outros projetos.
<!-- FIM REGISTRO PGSHARP 2026-11-22 -->

**Aplicação específica repositório PGSharp:** preservar a fonte própria e o compilador experimental independentes do cliente comercial; manter fontes históricas e dossiês sob atribuição. O `README.md` é a referência geral na `main`; não registrar componente extraído como APK instalável, não presumir parceria, licença ou disponibilidade de feed privado, e não embarcar dados privados no Git.
