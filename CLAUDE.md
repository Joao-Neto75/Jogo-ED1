# Diretrizes do Projeto (Claude Code Config)

## 1. Otimização de Contexto e Economia de Tokens

- **Evite explicações prolixas:** Vá direto à solução técnica. Não repita o código inteiro que o usuário já possui nas respostas, mostre apenas os trechos modificados (diffs) ou blocos alterados.

- **Leitura seletiva:** Antes de carregar arquivos inteiros para a memória de contexto, utilize buscas direcionadas ou leia apenas as funções/métodos necessários.

- **Silêncio em confirmações:** Responda de forma concisa a comandos simples. Evite introduções longas do tipo "Com certeza, aqui está o código...".

## 2. Padrões de Qualidade de Código (Clean Code &amp; Arquitetura)

- **Tipagem Estrita:** Em Java, utilize tipagem explícita e evite tipos genéricos a menos que estritamente necessário.

- **Tratamento de Erros:** Todo bloco de código crítico deve conter tratamento defensivo de erros (exceções claras, logs descritivos e validação de nulos).

- **Boas Práticas de POO / Estruturas de Dados:** Mantenha o código modular, respeitando o princípio da responsabilidade única, encapsulamento e organização em camadas.

- **Padrão de Commit / Código Limpo:** Escreva códigos limpos, comentando apenas o "porquê" de lógicas complexas (evite comentários óbvios que poluem o código).

## 3. Notas específicas para Claude Code

- **LEITURA OBRIGATÓRIA:** Todo o contexto do projeto está no arquivo [`AGENTS.md`](http://AGENTS.md). Sempre leia este arquivo antes de escrever ou alterar qualquer código. Siga rigorosamente as regras do professor contidas lá.

- Antes de implementar algo grande, apresente um plano curto e espere confirmação.

- Prefira editar arquivos existentes a recriá-los.