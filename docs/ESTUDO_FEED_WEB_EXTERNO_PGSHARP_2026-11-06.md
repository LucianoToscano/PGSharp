# Estudo: feed ORIGINAL do PGSharp em página web, sem abrir Pokémon GO

Data: 06/11/2026
**Objetivo único:** exibir no navegador os mesmos resultados dos feeds de Pokémon, raides e pesquisas do PGSharp, consultando o serviço PGSharp de forma autorizada, sem depender de executar o jogo. Não envolver PokeSearch, suas fontes de dados ou simples importação de filtros DAT.

## Evidência pública contemporânea
- [Site oficial](https://www.pgsharp.com/) atualmente apresenta PGSharp v1.276.2 e o recurso 100 IV Feed (Free) e All Coordinates Feed (Standard); a homepage possui um link de download hospedado sob `api.pgsharp.com`, mas isso **não demonstra API de feeds**.
- [Descrição oficial de recursos](https://www.pgsharp.com/features/): Quick Sniper I/II apresenta feeds de Pokémon, raids e quests e descreve filtros IV, espécie, gênero, forma, nível, distância, etc. Não descreve endpoint, credenciais para aplicações de terceiros, CORS ou documentação de integração.
- [Pesquisa de Romain Thomas, 07/11/2021](https://www.romainthomas.fr/post/21-11-pgsharp-analysis/): versão PGSharp 1.33.0; documenta comunicação servidor sobre HTTP/TLS com criptografia adicional e exemplos de POST para handshake, estado de jogador, reporte de raids e ativação. Os exemplos NÃO constituem endpoint de feed externo; a pesquisa é antiga, e não se deve supor que funciona em 2026.

## Inspeção estática local do APK fornecido (sem executar, interceptar tráfego ou acessar contas)
- APK `original.apk` corresponde ao PGSharp 1.276.2, com 2.203 entradas ZIP e três DEX (`classes.dex`, `classes2.dex`, `classes3.dex`).
- Nativa `lib/arm64-v8a/libmain.so`: 38.504.032 bytes descomprimidos, binário ELF AArch64 stripped, `DT_SONAME=libhela.so`. Exportação destacada `JNI_OnLoad` e sem funções de feed identificáveis pelos nomes exportados.
- A busca por strings ASCII identificou `https://www.pgsharp.com/download64`; termos `GETFEED`, `GETPOKEMON`, `hlfeeds`, `hlfastsnipe` e `api.pgsharp.com` não apareceram explicitamente na biblioteca. A substring `feeds` vista pertence a `buddyFeedStats`, e não a uma URL comprovada de busca.
- Esses são **resultados negativos de inspeção estática**, não prova de inexistência de endpoint. Binário nativo ofuscado pode usar outras representações e o servidor pode exigir autorização de usuário/dispositivo.
- O arquivo `PGSData.dat` é configuração de filtros, não resposta em tempo real ou credencial documentada da API.
- Não foi feita nenhuma tentativa de contornar criptografia, controle de acesso ou assinatura e não foram extraídos tokens de sessão.

## Veredicto técnico
- Confirmado: o app exibe feeds de Pokémon, raids e quests; há infraestrutura HTTP/servidor descrita historicamente.
- Não confirmado: a origem e protocolo atuais dos dados, se uma API para terceiros existe, URL funcional de query de feed, forma de autenticação/autorização, permissão para redistribuir resultados em site, possibilidade de uso independente do cliente de jogo.
- **Não há implementação pronta nem demonstração de feed em navegador.** Construir página agora implicaria resultados falsos ou outra fonte (fora do pedido).

## Caminho viável
1. Solicitar ao PGSharp documentação, API, licença ou permissão para leitura externa dos feeds, inclusive limites de requisição e autorização de contas.
2. Quando houver credenciais e documentação válidas, testar chamada oficial mínima fora do jogo, verificando se os resultados coincidem com a origem indicada.
3. Hospedar conector no backend (sem expor segredo ou licença em HTML/JavaScript), construir front-end de busca, cache curto, tratamento de limites e indicadores de atualização.
4. Se a empresa não fornecer acesso, explicar a limitação e não prometer usar o feed original fora do aplicativo; um serviço independente seria outro projeto e outra fonte.

## Status
**Investigação de viabilidade concluída; integração com API oficial/terceiros bloqueada por ausência de documentação e autorização; nenhuma página de resultados criada.**
