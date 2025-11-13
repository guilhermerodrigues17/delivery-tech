# Delivery Tech

[![Delivery API - CI/CD pipeline](https://github.com/guilhermerodrigues17/delivery-tech/actions/workflows/ci-pipeline.yml/badge.svg)](https://github.com/guilhermerodrigues17/delivery-tech/actions/workflows/ci-pipeline.yml)

Um projeto de backend para um sistema de gerenciamento de entregas (delivery),
desenvolvido em Java 21 com Spring Boot 3.

## 📝 Descrição

Projeto desenvolvido durante a formação de Arquitetura de Sistemas da FAT. O
objetivo é criar um sistema robusto e escalável que sirva como a base para uma
aplicação de delivery, similar ao iFood. A API permite o gerenciamento de
clientes, restaurantes, produtos e pedidos.

---

## 🚀 Tecnologias Utilizadas

Este projeto foi construído com uma stack moderna de Java e DevOps:

**Aplicação (Core):**
* **Java 21 LTS**;
* **Spring Boot 3** (Web, Data JPA, Validation, Security, Test);

**Banco de Dados e Cache:**
* **PostgreSQL:** Banco de dados relacional de produção;
* **Redis:** Cache distribuído para otimização de performance;
* **H2 Database:** Banco em memória (apenas para testes automatizados).

**Observabilidade (Stack PLG/T):**
* **Prometheus:** Coleta de Métricas (ex: timers, counters);
* **Loki:** Coleta de Logs (JSON estruturado);
* **Grafana:** Interface de visualização (Dashboards) para Logs e Métricas;
* **Zipkin (via Tracing):** Rastreamento distribuído de requisições (Spans).

**DevOps (CI/CD e Empacotamento):**
* **Docker & Docker Compose:** Containerização e orquestração de todos os serviços;
* **GitHub Actions:** Pipeline de CI/CD para testes, build e deploy automatizados.

---

## ✨ Features

- **CRUD** completo de Clientes (``Consumer``);
- **CRUD** completo de Restaurantes (``Restaurant``);
- **CRUD** completo de Produtos (``Product``);
- Sistema de criação e atualização de status de Pedidos (``Order``);
- Endpoints de recursos administrativos, com relatórios de vendas, atividades, etc;
- Sistema de autenticação JWT e autorização RBAC;
- Tratamento de exceções e validações de dados;
- Documentação interativa no padrão OpenAPI, com Scalar UI;
- Endpoints de monitoramento de saúde da aplicação;
- **Observabilidade Completa**: a API expõe métricas de negócio (pedidos, transições de status) e de sistema (JVM) para o Prometheus.
- **Logging Estruturado:** logs são gerados em JSON (incluindo logs de AUDIT) e enviados para o Loki.
- **Tracing Distribuído:** requisições críticas (como validações de serviço) são rastreadas com Spans customizados no Zipkin.
- **Cache de Alta Performance:** métodos de leitura (ex: `findById`) são cacheados usando Redis para reduzir a carga no banco de dados.
- **Ambientes de Docker:** o projeto é totalmente containerizado, com perfis separados para `dev` (IntelliJ) e `prod` (Docker-nativo).

## 📋 Endpoints

### Health & Info

- ``GET /health`` - Status da aplicação (inclui versão Java)
- ``GET /info`` - Informações da aplicação
- ``GET /h2-console`` - Console do banco H2

### Consumers (Clientes)

- ``POST /consumers`` - Cria um novo cliente;
- ``GET /consumers/{id}`` - Busca um cliente pelo ID;
- ``GET /consumers/email/{email}`` - Busca um cliente pelo email;
- ``GET /consumers`` - Lista todos os clientes ativos;
- ``GET /consumers/{id}/orders`` - Lista os pedidos de um cliente específico;
- ``PUT /consumers/{id}`` - Atualiza os dados de um cliente;
- ``DELETE /consumers/{id}`` - Desativa um cliente (soft delete).

### Restaurants (Restaurantes)

- ``POST /restaurants`` - Cria um novo restaurante;
- ``GET /restaurants/{id}`` - Busca um restaurante pelo ID;
- ``GET /restaurants?name={name}&category={category}&active={active}`` - Lista todos os restaurantes a partir do filtro aplicado, de forma paginada;
- ``GET /restaurants/nearby?cep={cep}`` - Lista todos os restaurantes próximos;
- ``GET /restaurants/{id}/products`` - Lista os produtos de um restaurante específico;
- ``GET /restaurants/{id}/orders`` - Lista os pedidos de um restaurante específico;
- ``GET /restaurants/{id}/delivery-tax?cep={cep}`` - Calcula a taxa de entrega para um CEP;
- ``PUT /restaurants/{id}`` - Atualiza os dados de um restaurante;
- ``PATCH /restaurants/{id}/status`` - Atualiza o status de um restaurante.

### Products (Produtos)

- ``POST /products`` - Cria um novo produto para um restaurante;
- ``GET /products/search?name={name}&category={category}`` - Lista produtos baseados no filtro inserido na requisição, de forma paginada;
- ``GET /products/{id}`` - Busca um produto pelo ID;
- ``PUT /products/{id}`` - Atualiza os dados de um produto;
- ``PATCH /products/{id}/status`` - Altera a disponibilidade de um produto;
- ``DELETE /products/{id}`` - Deleta um produto.

### Orders (Pedidos)

- ``POST /orders`` - Cria um novo pedido;
- ``POST /orders/calculate`` - Simula o valor subtotal e total de um pedido, sem o salvar;
- ``GET /orders?status={status}&startDate={startDate}&endDate={endDate}`` - Lista pedidos baseados nos filtros usados na requisição, de forma paginada;
- ``GET /orders/{id}`` - Busca um pedido pelo ID;
- ``PATCH /orders/{id}`` - Atualiza o status de um pedido.
- ``DELETE /orders/{id}`` - Cancela um pedido.

### Reports (Relatórios - administrativo)

- ``GET /reports/sales-by-restaurant`` - Lista valor total em vendas por restaurante;
- ``GET /reports/top-selling-products`` - Lista produtos mais vendidos por restaurante;
- ``GET /reports/active-consumers`` - Lista clientes com mais pedidos;
- ``GET /reports/orders-by-period`` - Listar pedidos por período e status.

---

## ⚙️ Como Executar o Projeto

Existem dois modos de executar a aplicação. Ambos requerem o Docker Desktop (ou Docker Engine/Compose) instalado.

### 1. Ambiente de Desenvolvimento (Recomendado)

A API roda localmente (no IntelliJ/VSCode), conectando-se aos serviços de infra (Postgres, Redis, Grafana) que rodam no Docker.

```bash

# 1. (Primeira vez) Crie a pasta de logs (Permission denied fix)
sudo mkdir logs
sudo chown -R $USER:$USER logs

# 2. Inicie TODA a infraestrutura (DB, Cache, Observabilidade)
docker compose -f docker-compose.dev.yml up -d

# 3. Abra o projeto no seu IntelliJ/IDE e clique "Run" (Play).
# (A API irá carregar o perfil 'dev' e se conectar ao localhost:5432)
# Pode ser necessário fornecer um env para JWT_SECRET para a aplicação rodar corretamente
```

### 2. Ambiente de Produção (Simulação Completa)

Este modo simula o deploy real. Ele constrói a imagem Docker da API e roda tudo dentro de contêineres, usando o perfil prod.

```bash
# 1. (Primeira vez) Crie a pasta de logs
sudo mkdir logs
sudo chown -R $USER:$USER logs

# 2. (Primeira vez) Crie seus segredos locais
cp .env.example .env
nano .env # (Edite o .env com seus segredos de DB e JWT)

# 3. Construa as imagens e inicie todos os serviços
docker compose up --build
```

Após a inicialização, a aplicação estará disponível em `http://localhost:8080`
por padrão.

## 🧪 Testes e Qualidade

Para garantir a qualidade e a estabilidade da aplicação, o projeto é configurado com um conjunto robusto de testes unitários e de integração.

### Executando os testes

Os testes de integração (arquivos `*IT.java`) dependem de um segredo JWT fictício para simular a autenticação. 
Ao executar os testes, é **necessário** configurar a variável ``JWT_SECRET``.
```bash

# Executa todos os testes
./mvnw clean test

```

### Verificando cobertura de testes (JaCoCo)
O projeto está configurado com o JaCoCo para analisar a cobertura de testes. 
Nossa meta de qualidade exige pelo menos **80%** de cobertura de linhas na camada de serviços e controle (``service.impl`` e ``controller``).

Para rodar os testes e verificar a cobertura, utilize o comando verify. Este comando irá falhar a build 
(``BUILD FAILURE``) se a meta de cobertura não for atingida.

```bash

# Executa os testes e verifica a cobertura
./mvnw clean verify

```
Após a execução (mesmo que falhe), você pode visualizar o relatório HTML completo no seu navegador, abrindo o seguinte arquivo:
[`/target/site/jacoco/index.html`](/target/site/jacoco/index.html)

## 🚀 Pipeline de CI/CD

Este repositório usa **GitHub Actions** para automatizar todo o ciclo de vida da aplicação. O pipeline (definido em `.github/workflows/ci-pipeline.yml`) é acionado em todo `push` ou `pull_request` para a branch `main` e executa 3 jobs sequenciais:

1.  **Build, Teste e Verificação (Job 1):**
    * Configura o Java 21 e faz cache das dependências do Maven;
    * [Roda `./mvnw clean verify` com o perfil `test`, executando todos os testes unitários e de integração contra um banco H2 em memória.

2.  **Buildar e Publicar Imagem Docker (Job 2):**
    * (Se os testes passarem) Constrói a imagem Docker de produção usando o `Dockerfile` multi-stage;
    * Faz login no GitHub Container Registry (`ghcr.io`);
    * Publica a nova imagem tagueada (ex: `ghcr.io/guilhermerodrigues17/delivery-tech:latest`).

3.  **Deploy para Produção (Job 3):**
    * (Se a imagem for publicada) Conecta-se ao servidor via SSH usando os segredos do repositório;
    * Cria o arquivo `.env` no servidor com as credenciais do banco e JWT;
    * Executa `docker compose pull` e `docker compose up -d --force-recreate delivery-api` para atualizar a aplicação que está rodando.

---

## 📚 Documentação da API (Swagger UI)

Para facilitar o desenvolvimento e a integração, a API está 100% documentada usando o padrão OpenAPI.

Com a aplicação rodando localmente (na porta `8080`), você pode acessar a documentação interativa (Scalar UI) através da seguinte URL:
**[http://localhost:8080/scalar](http://localhost:8080/scalar)**

## 🤝 Como Contribuir

Contribuições são bem-vindas! Se você tiver ideias para melhorias ou encontrar
algum problema, sinta-se à vontade para abrir uma issue ou enviar um pull
request.

- Faça um fork do projeto;
- Crie uma nova branch (git checkout -b feature/sua-feature);
- Faça o commit de suas alterações (git commit -m 'Adiciona nova feature');
- Envie para a sua branch (git push origin feature/sua-feature);
- Abra um Pull Request.