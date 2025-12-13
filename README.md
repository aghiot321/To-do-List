# ToDoList - Gerenciador de Tarefas

Aplicação web para gerenciamento de tarefas desenvolvida com Spring Boot, MySQL e Docker. Inclui pipeline completo de CI/CD com provisionamento automático de infraestrutura via Terraform.

![CI/CD Status](https://github.com/aghiot321/To-do-List/actions/workflows/cicd-terraform.yml/badge.svg)

## Tecnologias

- Java 21
- Spring Boot 3.5.4
- MySQL 8.4.0
- Docker e Docker Compose
- Maven
- Terraform
- GitHub Actions

## Pré-requisitos

- Java 21 ou superior
- Maven 3.9 ou superior
- Docker e Docker Compose
- Git

## Estrutura do Projeto

```
todolist/
├── src/
│   ├── main/java/br/com/aghiot/todolist/
│   │   ├── user/              # Módulo de usuários
│   │   ├── task/              # Módulo de tarefas
│   │   ├── Filter/            # Filtros de autenticação
│   │   └── config/            # Configurações
│   └── test/                  # Testes unitários
├── terraform/                 # Infraestrutura como código
├── docker-compose.yml         # Ambiente de desenvolvimento
├── docker-compose.prod.yml    # Ambiente de produção
├── Dockerfile                 # Build da aplicação
└── pom.xml                    # Dependências Maven
```

## Instalação e Execução Local

### 1. Clonar o repositório

```bash
git clone https://github.com/aghiot321/To-do-List.git
cd To-do-List
```

### 2. Configurar variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```bash
cp .env.example .env
```

Edite o arquivo `.env` com suas configurações:

```env
MYSQL_ROOT_PASSWORD=root_password
MYSQL_DATABASE=todolist
MYSQL_USER=todolist_user
MYSQL_PASSWORD=todolist_password
MYSQL_PORT=3306
SPRING_PROFILE_ACTIVE=dev
SERVER_PORT=8080
JAVA_OPTS=-Xmx512m
```

### 3. Iniciar com Docker Compose

```bash
docker-compose up -d
```

A aplicação estará disponível em `http://localhost:8080`

### 4. Verificar status

```bash
# Status dos containers
docker-compose ps

# Logs da aplicação
docker-compose logs -f todolist-app

# Logs do MySQL
docker-compose logs -f mysql
```

### 5. Parar os serviços

```bash
docker-compose down
```

## Executar Testes

```bash
mvn test
```

Para gerar relatório de cobertura:

```bash
mvn test jacoco:report
```

O relatório estará disponível em `target/site/jacoco/index.html`

## Banco de Dados

### Conexão MySQL

```bash
docker-compose exec mysql mysql -u todolist_user -p todolist
```

### Estrutura

- **tb_users**: Armazena usuários do sistema
- **tb_tasks**: Armazena tarefas vinculadas aos usuários

### Backup e Restauração

Backup:
```bash
docker-compose exec mysql mysqldump -u todolist_user -p todolist > backup.sql
```

Restaurar:
```bash
docker-compose exec mysql mysql -u todolist_user -p todolist < backup.sql
```

## API Endpoints

### Usuários

- `POST /users/` - Criar usuário
- `GET /users/` - Listar todos os usuários
- `GET /users/{id}` - Buscar usuário por ID
- `PUT /users/{id}` - Atualizar usuário
- `DELETE /users/{id}` - Remover usuário

### Tarefas

- `POST /tasks/` - Criar tarefa (requer autenticação)
- `GET /tasks/` - Listar todas as tarefas
- `GET /tasks/{id}` - Buscar tarefa por ID
- `PUT /tasks/{id}` - Atualizar tarefa
- `DELETE /tasks/{id}` - Remover tarefa

### Autenticação

As rotas de tarefas requerem autenticação Basic Auth:
- Header: `Authorization: Basic <base64(username:password)>`

## CI/CD Pipeline

O projeto utiliza GitHub Actions e Terraform para automação completa de CI/CD.

### Etapas do Pipeline

1. **Testes (CI)**: Executa testes unitários e gera relatório de cobertura
2. **Provisionar Infraestrutura (IaC)**: Cria/atualiza VM no Google Cloud Platform via Terraform
3. **Build e Push**: Constrói imagem Docker e envia para Docker Hub
4. **Deploy**: Implanta aplicação no servidor automaticamente

### Fluxo do Pipeline

```
Push para main
    ↓
Executar testes
    ↓
Provisionar infraestrutura (Terraform) ← paralelo → Build imagem Docker
    ↓
Deploy automático no servidor
```

### Configuração de Secrets

Configure os seguintes secrets em `Settings > Secrets and variables > Actions`:

**GCP (Terraform)**
- `GCP_PROJECT_ID`: ID do projeto no Google Cloud
- `GCP_SA_KEY`: Chave JSON da Service Account
- `GCP_TERRAFORM_BUCKET`: Bucket para state do Terraform

**SSH**
- `SSH_USER`: Usuário SSH do servidor
- `SSH_PUBLIC_KEY`: Chave pública SSH
- `SSH_PRIVATE_KEY`: Chave privada SSH

**Docker Hub**
- `DOCKER_USERNAME`: Username do Docker Hub
- `DOCKER_PASSWORD`: Token de acesso do Docker Hub

### Configurar GCP Service Account

```bash
# Criar Service Account
gcloud iam service-accounts create terraform-sa \
  --display-name="Terraform Service Account"

# Adicionar permissões
gcloud projects add-iam-policy-binding SEU_PROJECT_ID \
  --member="serviceAccount:terraform-sa@SEU_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/compute.admin"

# Gerar chave JSON
gcloud iam service-accounts keys create ~/terraform-sa-key.json \
  --iam-account=terraform-sa@SEU_PROJECT_ID.iam.gserviceaccount.com
```

### Criar Bucket para Terraform State

```bash
gcloud storage buckets create gs://todolist-terraform-state \
  --project=SEU_PROJECT_ID \
  --location=us-west1 \
  --uniform-bucket-level-access
```

### Executar Terraform Localmente

```bash
cd terraform

# Copiar arquivo de exemplo
cp terraform.tfvars.example terraform.tfvars

# Editar variáveis
nano terraform.tfvars

# Configurar credenciais GCP
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/terraform-sa-key.json"

# Inicializar e aplicar
terraform init
terraform plan
terraform apply

# Ver IP público
terraform output public_ip
```

### Deploy Manual

Se necessário, execute o pipeline manualmente:

1. Acesse Actions no GitHub
2. Selecione "CI/CD Pipeline"
3. Clique em "Run workflow"
4. Selecione a branch `main`

## Comandos Úteis

### Docker Compose

```bash
# Ver status
docker-compose ps

# Logs em tempo real
docker-compose logs -f

# Reiniciar aplicação
docker-compose restart todolist-app

# Recriar containers
docker-compose up -d --force-recreate

# Remover volumes
docker-compose down -v
```

### Docker

```bash
# Listar imagens
docker images

# Remover imagens antigas
docker image prune -af

# Ver uso de recursos
docker stats
```

### Maven

```bash
# Limpar e compilar
mvn clean compile

# Executar aplicação
mvn spring-boot:run

# Gerar JAR
mvn package

# Pular testes
mvn package -DskipTests
```

### Produção

```bash
# Ver logs da aplicação
docker-compose -f docker-compose.prod.yml logs -f todolist-app

# Ver status dos containers
docker-compose -f docker-compose.prod.yml ps

# Reiniciar aplicação
docker-compose -f docker-compose.prod.yml restart todolist-app

# Atualizar para nova versão
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# Health check
curl http://localhost:8080/actuator/health
```

## Health Check

A aplicação expõe endpoints de health check via Spring Actuator:

```bash
# Health check básico
curl http://localhost:8080/actuator/health

# Health check detalhado
curl http://localhost:8080/actuator/health | jq
```

## Solução de Problemas

### Aplicação não inicia

1. Verifique se o MySQL está rodando: `docker-compose ps`
2. Verifique logs: `docker-compose logs mysql`
3. Confirme variáveis de ambiente no `.env`

### Erro de conexão com banco

1. Aguarde o MySQL estar pronto (health check)
2. Verifique credenciais no `.env`
3. Teste conexão: `docker-compose exec mysql mysqladmin ping`

### Permissão negada (Docker)

```bash
# Adicionar usuário ao grupo docker
sudo usermod -aG docker $USER

# Aplicar mudanças
newgrp docker
```

### Pipeline falha no deploy

1. Verifique secrets configurados no GitHub
2. Confirme IP do servidor acessível
3. Valide chave SSH funcionando
4. Revise logs em Actions

## Recursos do Terraform

O Terraform provisiona automaticamente:

- VM e2-micro no GCP (Free Tier)
- Regras de firewall (portas 8080 e 3309)
- Docker e Docker Compose
- Configuração SSH

Arquivos principais:
- `terraform/main.tf` - Recursos GCP (VM, firewall)
- `terraform/variables.tf` - Variáveis configuráveis
- `terraform/outputs.tf` - Outputs (IP público, URLs)
- `terraform/backend.tf` - Backend remoto (GCS)

## Custos (GCP Free Tier)

A infraestrutura utiliza o Free Tier permanente do Google Cloud:

- VM e2-micro (1 vCPU, 1GB RAM): GRATUITO
- 30 GB de disco SSD: GRATUITO
- IP Ephemeral: GRATUITO
- 1GB de tráfego/mês: GRATUITO

**Custo total: R$ 0,00/mês**

Requisitos para manter gratuito:
- Região: `us-west1`, `us-central1` ou `us-east1`
- Tipo de máquina: `e2-micro`
- Disco: máximo 30 GB
- IP: ephemeral (não reservar estático)

## Checklist de Deploy

- [ ] Service Account criada no GCP
- [ ] Bucket para state do Terraform criado (GCS)
- [ ] Secrets do GitHub configurados (9 secrets)
- [ ] Chave SSH gerada e adicionada aos secrets
- [ ] Terraform testado localmente (opcional)
- [ ] Push para `main` realizado
- [ ] Pipeline executado com sucesso
- [ ] VM criada automaticamente no GCP
- [ ] Aplicação acessível via health check

## Licença

Este projeto é de código aberto e está disponível para uso educacional.

## Autor

Desenvolvido por Aghiot
