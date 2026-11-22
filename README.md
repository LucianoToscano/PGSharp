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

## REGISTRO CUMULATIVO — Pesquisa PoGo-Battle-Research / Auto Battle / Auto Shield — 22/11/2026

**Origem documental:** conversa de pesquisa e auditoria de 22/11/2026, arquivo anexo `PoGo-Battle-Research.zip` e `AUDITORIA_POGO_BATTLE_RESEARCH_2026-11-22.md`. Complemento preservado no [Google Drive](https://docs.google.com/document/d/1CqsipH09xVX0ECHjHKAq8fhJvryIBkmoCrQ3hmUjOnQ/edit). Esta seção é documentação, não implantação. Conserva integralmente todo o conteúdo anterior.

**Objeto do estudo:** ataque rápido, escolha de golpes carregados, isca de escudos, Auto Shield, decisão tática por energia/HP/tipos/DPE, contagem de turnos, leitura de interface Unity por visão computacional, latência, idempotência, sincronização e diagnóstico. A arquitetura de pesquisa desacopla 5 camadas: percepção visual → máquina de estados → motor de decisão → controlador usado somente em app sintético de QA → confirmação/telemetria. O tempo de turno PvP é 500 ms; Team GO Rocket usa ritmo de turnos com atrasos/regras de IA diferentes; raids/ginásios exigem motor PvE separado.

**Fontes e inventário:** RELATORIO.md do ZIP afirma varredura de 418+ repositórios e registra metadados de 24 em `data/repos.json` com referência a GitHub API; a auditoria independente do ZIP não refez essas 24 consultas. Referências prioritárias: [PvPoke](https://github.com/pvpoke/pvpoke), [GoBattleSim](https://github.com/biowpn/GoBattleSim), [PVPokeLossBot](https://github.com/CodeWithMa/PVPokeLossBot), [PogoPVPLossBot](https://github.com/daminokun/PogoPVPLossBot), [GoIV](https://github.com/farkam135/GoIV), [openatx/uiautomator2](https://github.com/openatx/uiautomator2), [pypogo](https://github.com/Erotemic/pypogo) e [artigo de IA do PvPoke](https://pvpoke.com/articles/development/developing-trainer-battle-ai/). Referência de código aberto e fonte de dados não é evidência de bot live homologado.

**Pacote reproduzido:** ZIP com 1.038.084 bytes, 133 entradas incluindo diretórios, ~9,4 MB descompactados; `RELATORIO.md` (~55 KB), módulos `python/pogob`, harness Kotlin/JVM, quatro demos, exemplo Android não compilado, 357 golpes em JSON, 30 espécies e 109 CPMs. SHA-256 do ZIP: `a092c808046277d791539a6b3764775093ad103950e80e7abfde06efac8b7b48`. Fonte original deve ser preservada com acesso restrito e manifesto de procedência, sem publicar o ZIP inteiro automaticamente.

**Evidência reproduzida em ambiente isolado:** `python -m pytest tests -q`: **39/39 passaram** (28,38 s). `bash kotlin/build.sh` compilou, e `java -jar kotlin/build/pogob.jar` emitiu **31 PASS** e nenhuma falha; o material diz 32/32, número não reproduzido. Demos 01 (simulação), 02 (matriz de política) e 03 (visão) foram concluídas; `demo_04_loop.py` ultrapassou limite de 120 s e sua vazão não foi confirmada. Não houve execução Android físico, instrumentação no jogo, APK, integração de UI ou comprovação de desempenho ao vivo. O relatório HTML mais antigo citava 8/8 testes, mas a suíte de oito não estava presente no ZIP anterior investigado; não confundir com os 39 testes efetivamente reproduzidos no novo pacote.

**Bugs/riscos da auditoria:**
1. **P1 FSM Python:** `python/pogob/fsm.py` pode retornar estado `CHARGE_MENU` mas manter `self.state` antigo; reproduzido quando `charge_button_available=True`. Corrigir transição e escrever teste de regressão.
2. **P1 direitos:** `NOTICE.md` diz não copiar código do bot sem licença, mas `data/raw/code/` inclui `loss_bot.py`, `loss_decision.py`, `loss_adb.py`, `loss_action.py`, `loss_test.py`, entre outros. Revisar titularidade/autorização antes de redistribuir e incluir avisos/licença MIT dos dados PvPoke onde aplicável. Manter original histórico, criar variante sanitizada para distribuição.
3. **P1 execução Android:** `kotlin/android/BattleA11yService.kt` oferece `dispatchGesture`/`performGlobalAction`, posições fixas e observação incondicional de batalha. Não importar este exemplo para produto; em QA autorizar somente app próprio/fictício, validar janela/estado e bloquear por padrão.
4. **P2 robustez/benchmark:** harness Kotlin imprime 31 PASS, não 32; `kotlin/build.sh` tem pipeline `|| true` que pode mascarar falhas ao reutilizar JAR anterior; `demo_04_loop` não terminou na reprodução. Corrigir contagem, propagação de erro e medir CPU/latência.
5. **P2 interpretação estatística:** 84,2% vitória do shield por valor esperado, 4,2% heurístico e 0,0% nunca escudar são **taxas do simulador e cenário sintético específico**, não previsão de GBL ou evidência de superioridade universal. Benchmarks de OpenCV/latência não foram aferidos em celular.

**Decisão de aproveitamento:** aprovado para pesquisa, protótipos offline, fixtures, modelagem de dano, energia, escudo, scheduler com deduplicação/confirmar ação e logs JSONL. **Não homologado** para importação integral em produção, publicação de fontes sem licença, captura automática no cliente oficial, operação de gestos, APIs privadas nem APK. Primeiro reconciliar com motores e Game Master atuais, corrigir P1, testar casos diversos, manter camada de decisão separada do executor e validar em dispositivo com consentimento. Respeitar o PROTOCOLO GLOBAL — DESENVOLVIMENTO PARALELO SEM PERDAS: mestre e HEAD antes de alterar, branch por tarefa, registro de diff/testes, PR draft, sem merge nem substituição de concorrentes.

**Aplicação ao projeto:** PGSharp: pesquisar comportamentos e referências visuais para perfis, testes e diagnóstico; separar fonte própria e acervo autorizado de SDK/cliente comercial. Este ZIP NÃO prova acesso a backend privado, código de filtros do PGSharp nem compatibilidade com o cliente. Não incorporar o adaptador de gestos no APK comercial, não alterar `PGSData.dat`, credenciais, APK-base ou assinaturas, não criar clone/release. Manter o compilador experimental, documentação e revisão de direitos independentes.
