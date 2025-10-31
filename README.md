# Delivery Tech

Um projeto de backend para um sistema de gerenciamento de entregas (delivery),
desenvolvido em Java 21 com Spring Boot 3.

## 📝 Descrição

Projeto desenvolvido durante a formação de Arquitetura de Sistemas da FAT. O
objetivo é criar um sistema robusto e escalável que sirva como a base para uma
aplicação de delivery, similar ao iFood. A API permite o gerenciamento de
clientes, restaurantes, produtos e pedidos.

## 🚀 Tecnologias Utilizadas

- **Java 21 LTS** (versão mais recente)
- **Spring Boot 3.5.x**
    - **Spring Web:** Para a construção de APIs REST;
    - **Spring Data JPA:** Para a persistência de dados;
    - **Spring Boot Validation:** Para validação dos dados de entrada;
    - **Spring Security:** Para autenticação e autorização;
    - **Spring Boot DevTools:** Para facilitar o desenvolvimento.
- **H2 Database:** Banco de dados em memória para desenvolvimento e testes;
- **Lombok:** Para reduzir código boilerplate em classes de modelo;
- **MapStruct:** Para mapeamento de DTOs e entidades;
- **Spring Doc:** Para documentação interativa no padrão OpenAPI;
- **Maven:** Gerenciador de dependências e build do projeto.

## ✨ Features

- CRUD completo de Clientes (``Consumer``);
- CRUD completo de Restaurantes (``Restaurant``);
- CRUD completo de Produtos (``Product``);
- Sistema de criação e atualização de status de Pedidos (``Order``);
- Endpoints de recursos administrativos, com relatórios de vendas, atividades, etc;
- Sistema de autenticação JWT e autorização RBAC;
- Tratamento de exceções e validações de dados;
- Documentação interativa no padrão OpenAPI, com Scalar UI;
- Endpoints de monitoramento de saúde da aplicação.

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

## ⚙️ Como executar o projeto

Siga os passos abaixo para rodar o projeto no seu ambiente de desenvolvimento.

**Pré-requisitos:** JDK 21 instalado

```Bash

# 1. Clone o repositório
git clone https://github.com/guilhermerodrigues17/delivery-tech.git

# 2. Acesse a pasta do projeto
cd delivery-tech

# 3. Instale as dependências
./mvnw clean install

# 4. Execute a aplicação spring boot
./mvnw spring-boot:run

```

Após a inicialização, a aplicação estará disponível em `http://localhost:8080`
por padrão.

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