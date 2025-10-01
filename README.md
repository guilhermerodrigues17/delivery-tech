# Delivery Tech
Um projeto para gerenciamento de entregas.

## 📝 Descrição
Projeto desenvolvido durante a formação de Arquitetura de Sistemas da FAT. O objetivo 
é criar um sistema robusto e escalável de entregas via Delivery.


## 🚀 Tecnologias Utilizadas
- **Java 21 LTS** (versão mais recente)
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- H2 Database
- Maven

## 📋 Endpoints
- GET /health - Status da aplicação (inclui versão Java)
- GET /info - Informações da aplicação
- GET /h2-console - Console do banco H2

## ⚙️ Como executar o projeto
Siga os passos abaixo para rodar o projeto no seu ambiente de desenvolvimento.

**Pré-requisitos:** JDK 21 instalado

```Bash

# 1. Clone o repositório
git clone https://github.com/guilhermerodrigues17/delivery-tech.git

# 2. Acesse a pasta do projeto
cd delivery-tech

# 3. Instale as dependências
mvn clean install

# 4. Execute a aplicação spring boot
./mvnw spring-boot:run

# 5. Inicie o servidor após build
java -jar target/delivery-api-version-SNAPSHOT.jar
```
## 🤝 Como Contribuir
Contribuições são bem-vindas! Se você tiver ideias para melhorias ou encontrar algum problema, sinta-se à vontade para abrir uma issue ou enviar um pull request.

- Faça um fork do projeto.

- Crie uma nova branch (git checkout -b feature/sua-feature).

- Faça o commit de suas alterações (git commit -m 'Adiciona nova feature').

- Envie para a sua branch (git push origin feature/sua-feature).

- Abra um Pull Request.