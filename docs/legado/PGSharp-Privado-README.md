# PGSharp Privado — desenvolvimento

**Repositório privado de código-fonte e customizações pessoais.**

## Arquitetura acordada
- **Este repositório (privado):** código, interface, testes, scripts e ajustes dos favoritos.
- **LucianoToscano/PGSharp-Pessoal (público):** somente o workflow de compilação. O runner temporário baixa a fonte privada com token de acesso de leitura.
- Nunca publicar o `PGSData.dat`, `PGSStats.json`, APK base, senhas, dados de conta ou chaves de assinatura no git.

## Funções planejadas
1. Renomear filtros, corrigir emojis e mover favoritos por arraste.
2. Buscar, organizar por categoria e fixar filtros.
3. Importar e exportar cópias do DAT localmente, com backup.
4. Testar a integração do painel Android experimental no APK que o usuário possui.
5. Futuro: verificar viabilidade da edição direta da lista ativa, somente após identificação e teste da persistência.

## Estado em 05/11/2026
A versão inicial do módulo (Java + editor HTML + scripts) já foi enviada a este repositório.
O GitHub Actions executou os testes de scripts com sucesso em 05/11/2026. A compilação Android ainda não foi executada.
**Nenhum APK recompilado/assinado foi validado em aparelho.** Não confundir edição de arquivo de backup com edição da lista de favoritos ativa.

## Política de distribuição
O APK original de terceiros e os arquivos pessoais devem ficar fora deste repositório.
O workflow público só pode publicar artefatos protegidos, e nunca expor código privado, chaves, backup ou credenciais em logs.
A assinatura própria do build não substitui a assinatura original do aplicativo.

## Requisitos de configuração do compilador público
São necessários segredos do GitHub Actions (ver README do PGSharp-Pessoal). O acesso ao repositório privado deve ser por token com **somente Contents: Read** e validade limitada.

## Auditoria técnica — 05/11/2026 (continuação)
- APK de referência analisado localmente: SHA-256 `db38fc01e0148e56a707fca5b08163f2171b2268a667a76428124dc2b069cd83` (somente hash; arquivo **não** versionado).
- Detectados três DEX originais e a classe `UnityMainActivity` esperada pelo gancho.
- Corrigido o acionamento do painel: o botão ★ é instalado após a chamada de criação da tela Unity.
- CI atualizado para Java 17 / Android SDK 35 / D8; inclui prova de inserção do quarto DEX em **APK sintético**, sem conter APK de terceiros.
- Execução aprovada: https://github.com/LucianoToscano/PGSharp-Privado/actions/runs/41975671312
- Script de recompilação inclui verificações adicionais de integridade, editor e DEX no APK resultante.
- **Falta** uma execução real do recompilador com APK-base privado, assinatura própria e teste no aparelho. A validação em APK sintético não substitui esses passos.

## Melhorias e testes de regressão — continuação de 05/11/2026
- `tests/test_dat_editor.js`: verifica as cinco listas do editor com dados fictícios, preservação das propriedades dos filtros, nomes Unicode e strings longas.
- Teste local adicional executado com o `PGSData.dat` fornecido: reordenação e renomeação de filtro, leitura de volta da seção alterada e preservação lógica das outras quatro listas. **O DAT original não foi modificado ou enviado ao GitHub**.
- `scripts/insert_payload.py`: inclusão dinâmica no próximo slot DEX livre; rejeita lacunas, nomes duplicados e sobrescrita do APK de entrada.
- `tests/test_insert_payload.py`: testa comportamento com três ou quatro DEX originais, preservação de arquivos e casos de erro, usando APKs sintéticos.
- `build_ci.sh`: verificação final do nome dinâmico do DEX e da presença do editor e manifest.
- CI verde para código Java, D8, testes Smali, empacotamento, regressões do codec e scripts: https://github.com/LucianoToscano/PGSharp-Privado/actions/runs/41977170018
- **Pendente**: disponibilizar `base-apk` em release privada, configurar secrets, executar o workflow público e validar o APK instalado em dispositivo. Nenhum APK completo foi gerado.

## Preparar primeira compilação com a base já auditada

Foi adicionado `scripts/primeira_compilacao.ps1` para Windows. Ele confere SHA-256 do APK fornecido, verifica a privacidade dos dois repositórios, envia `original.apk` apenas como asset da release **privada** `base-apk`, cria ou reutiliza uma chave local, cadastra os cinco GitHub Actions secrets via entrada oculta e aciona o compilador público.

Requisitos: `gh auth login`, JDK (`keytool`) e token fine-grained criado pelo titular apenas para `PGSharp-Privado` com `Contents: Read`. Executar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\primeira_compilacao.ps1 -ApkPath "C:\caminho\original.apk"
```

A senha de assinatura e o keystore devem ser guardados para futuras versões. Este script **não** foi executado na máquina do usuário. A criação de release e secrets exige uma sessão local autenticada.

## Pedido prioritário: editar favoritos na tela original (05/11/2026)
**Substituir o fluxo anterior com WebView/DAT:** o usuário quer mover os favoritos tocando nos próprios itens da lista PGSharp. Preferências: segurar cerca de 600 ms e arrastar, ou ativar alças ☰ discretas à direita. Renomear dentro da mesma tela, salvar automaticamente sem importar/exportar DAT.

Entregue nesta etapa:
- `src/com/pgs/personal/LiveFavoritesOps.java`: operações independentes de mudança de posição/nomes, preservando os outros dados.
- `tests/LiveFavoritesOpsTest.java`: verificação com emojis, validação de índices, ausência de efeitos colaterais, campos IV e prioridade.
- `docs/FAVORITOS_DIRETO_NO_MENU.md`: requisitos funcionais e critérios de aceitação.
- CI aprovado: https://github.com/LucianoToscano/PGSharp-Privado/actions/runs/41984849003

**Atenção: isso ainda NÃO altera a tela nativa do PGSharp.** A identificação do controlador de favoritos em Unity/IL2CPP e seu mecanismo de gravação está pendente. O novo APK não deve ser anunciado como correção concluída até o teste de arraste direto e persistência na lista original.

## Correção de status após teste no aparelho (05/11/2026)
A captura do usuário confirma que **PGSharp Pessoal — Preferências Nativas Experimental NÃO modificou a tela de Favoritos**. A pré-carga experimental de `hlfavor`/`hlfeeds` no PlayerPrefs é apenas hipótese, executada somente para listas ausentes. **Não foram adicionadas as alças ☰ e não existe persistência de arraste direto**. Não distribuir novas versões como se tivessem a função enquanto a integração nativa não estiver comprovada. Detalhes: [auditoria da tela original](docs/AUDITORIA_FAVORITOS_NATIVOS_2026-11-05.md).

## Atalhos Customizados — análise e implementação parcial (05/11/2026)
O usuário solicitou reorganizar a tela nativa de **Atalhos Customizados** e verificar Sniper Rápido III/IV, além da edição direta de favoritos.

- Pela análise local do arquivo DAT, a seção `hlshortcuts` contém um objeto `items` de flags booleanas (não uma lista de posições), além de `title` e `orientation`. Os IDs nativos dos dois Snipers presentes são `hlfastsnipe` e `hlfastsnipe2`.
- A tela original do APK fornecido exibe Sniper I e II; a referência oficial do PGSharp também só documenta esses dois.
- Adicionado `CustomShortcutsModel.java` com arraste lógico por ID e quatro estados/filtros independentes, mas III/IV não podem ser ativados sem o suporte REAL do motor nativo.
- Teste adicionado em `tests/CustomShortcutsModelTest.java`, integrado à CI.
- Plano de UI, gesto longo, ☰, preservação dos switches e persistência: `docs/ATALHOS_CUSTOMIZADOS_NATIVE_2026-11-05.md`.

**Ainda não implementado**: adição de ☰ ao menu PGSharp original, persistência de ordem nessa UI, dois feeds adicionais reais. Não lançar novo APK anunciando essas funcionalidades enquanto não estiverem verificadas em aparelho.
