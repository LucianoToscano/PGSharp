# Auditoria inicial conservadora — PGSharp

> **Data de referência:** 21/11/2026 (America/Fortaleza), 22/11/2026 UTC.  
> **Estado:** inventário inicial e documentação; NÃO é auditoria recursiva exaustiva, nem conclusão funcional.  
> **Repositório oficial:** `LucianoToscano/PGSharp` / `main`.  
> **Commit-base verificado:** `6b76c6240f70b94e0f87af2112907d17af948506`.  
> **Branch QA:** `qa/protocolo-global-sem-perdas-20261121`.  
> **Protocolo:** [Desenvolvimento paralelo sem perdas](PROTOCOLO_GLOBAL_DESENVOLVIMENTO_PARALELO_SEM_PERDAS.md).

## Evidências da inspeção inicial

- **Base oficial:** `main` com fonte própria, scripts, `src`, `tests`, `assets` e workflow experimental; não foram encontrados `AGENTS.md` nem `docs/GUIA_MESTRE.md`.
- **README atual:** relata consolidação de antigo código próprio via PR #1 e validação separada do modo APK. APK-base é externo e depende de origem autorizada; nenhum APK funcional validado foi declarado.
- **Último commit-base:** endurecimento da verificação SHA-256 do APK-base privado.
- **Documento técnico:** `docs/MAPEAMENTO_NATIVO_APK_2026-11-05.md` registra ausência de ponte comprovada para controladores nativos de favoritos/atalhos e impede alegar função corrigida só por DEX/PlayerPrefs.
- **Drive:** pastas de instaladores, dados, guias, engenharia reversa, capturas, vídeos, experimentos e scripts; não mover material sensível sem auditoria de origem.
- **Testes não executados:** integração nativa, instalação, script de build, assinatura e testes de dispositivo.
- **Cuidados de licença:** não incluir código fechado, segredos ou APK proprietário no repositório público.

## Classificação preliminar A/B/C — sem exclusão

| Classe | Candidato | Estado |
|---|---|---|
| A | Duplicatas exatas ou temporários com referências verificadas | **Nenhum item aprovado ou excluído.** A confirmação de hash e dependências ainda é necessária. |
| B | Documentação histórica/atual com conteúdo sobreposto | **Analisar antes de propor mesclagem.** Preservar textos e decisões únicos. |
| C | Mestres, código, assets, builds funcionais e versões históricas | **Manter.** Nenhuma exclusão ou movimentação realizada. |

## Proteção de projetos paralelos

Não houve modificação de código-fonte, assets, Mestre, Drive, workflow, assinatura, APK ou release neste PR. Antes de integrar, comparar o SHA da `main` novamente, reavaliar PRs concorrentes e validar conflitos. O arquivo criado é uma anotação técnica de QA, subordinada à documentação canônica existente.

## Validação desta proposta

- Verificado: existência do repositório e da branch `main`, identidade do commit-base, estrutura inicial, documentação de entrada e pastas de Drive relacionadas.
- Não verificado: hash de todos os arquivos, dependências recursivas, execução de aplicativo, testes em aparelho, compilação e resultados de redes/APIs em produção.
- Rollback: fechar o PR sem merge; nenhum arquivo anterior foi substituído.
- Próximo passo: inventário recursivo e matriz de regressão por projeto, sem operações destrutivas.

