# todolist

## Docker Compose - Configuração

Configuração Docker Compose com dois serviços:
- MySQL 8.4.0 (banco de dados)
- Spring Boot Application (aplicação)

### Variáveis de Ambiente

As seguintes variáveis estão configuradas em `.env`:

- MYSQL_ROOT_PASSWORD - Senha root do MySQL
- MYSQL_DATABASE - Nome do banco de dados
- MYSQL_USER - Usuário do banco
- MYSQL_PASSWORD - Senha do usuário
- MYSQL_PORT - Porta do MySQL (padrão: 3306)
- SPRING_PROFILE_ACTIVE - Perfil da aplicação
- SERVER_PORT - Porta da aplicação (padrão: 8080)
- JAVA_OPTS - Opções JVM

### Como Executar

Iniciar os serviços:
```bash
docker-compose up -d
```

Parar os serviços:
```bash
docker-compose down
```

Ver status:
```bash
docker-compose ps
```

Ver logs:
```bash
docker-compose logs -f
```

### Acessar os Serviços

- Aplicação: http://localhost:8080
- MySQL: localhost:3306

Credenciais MySQL:
- Usuário: todolist_user
- Senha: todolist_password
- Banco: todolist

### Testar Conexão com MySQL

```bash
docker-compose exec mysql mysql -u todolist_user -p todolist
```

Dentro do MySQL:
```sql
show tables;
describe user;
describe task;
```

### Volumes

Dados persistem em `./mysql_data`. Para remover dados:
```bash
docker-compose down -v
```

### Rede

Containers se comunicam através da rede `todolist-network`.

### Health Checks

- MySQL: verificação a cada 10 segundos
- Aplicação: verificação a cada 30 segundos

### Backup e Restauração

Backup:
```bash
docker-compose exec mysql mysqldump -u todolist_user -p todolist > backup.sql
```

Restaurar:
```bash
docker-compose exec mysql mysql -u todolist_user -p todolist < backup.sql
```

### Solução de Problemas

Logs da aplicação:
```bash
docker-compose logs todolist-app
```

Logs do MySQL:
```bash
docker-compose logs mysql
```

---

## CI/CD Pipeline

![CI/CD Status](https://github.com/aghiot321/To-do-List/actions/workflows/cicd.yml/badge.svg)

Este projeto utiliza **GitHub Actions** para automação completa de CI/CD (Integração Contínua e Entrega Contínua).

### 📋 Visão Geral do Pipeline

O pipeline é executado automaticamente a cada push na branch `main` e consiste em 3 etapas principais:

#### 1️⃣ **Testes Unitários (CI)**
- Executa todos os testes unitários com Maven
- Gera relatório de cobertura de código com JaCoCo
- Armazena relatórios como artefatos (disponíveis por 30 dias)
- **Bloqueio**: Se os testes falharem, o pipeline é interrompido

#### 2️⃣ **Build e Push da Imagem Docker (CD)**
- Constrói a imagem Docker da aplicação
- Faz o push para o Docker Hub com tags:
  - `main-<SHA>` (SHA do commit para rastreabilidade)
  - `latest` (última versão estável)
- Utiliza cache para otimizar builds subsequentes
- **Só executa se**: os testes passarem

#### 3️⃣ **Deploy Automático (CD)**
- Conecta ao servidor de produção via SSH
- Atualiza o código do repositório
- Baixa a nova imagem do Docker Hub
- Para os containers antigos
- Sobe os novos containers com a versão atualizada
- Verifica o health check da aplicação
- Limpa imagens antigas para economizar espaço
- **Só executa se**: o build e push forem bem-sucedidos

### 🔐 Secrets Necessários no GitHub

Para o pipeline funcionar, configure os seguintes secrets em `Settings > Secrets and variables > Actions`:

| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `DOCKER_USERNAME` | Seu username do Docker Hub | `seu_usuario` |
| `DOCKER_PASSWORD` | Senha ou token do Docker Hub | `dckr_pat_xxxxx` |
| `SERVER_HOST` | IP ou domínio do servidor | `192.168.1.100` ou `todolist.com` |
| `SERVER_USER` | Usuário SSH do servidor | `ubuntu` ou `root` |
| `SERVER_PORT` | Porta SSH (padrão: 22) | `22` |
| `SSH_PRIVATE_KEY` | Chave privada SSH para autenticação | `-----BEGIN OPENSSH PRIVATE KEY-----...` |

### 🖥️ Configuração Inicial do Servidor

Antes do primeiro deploy, execute manualmente no servidor:

```bash
# 1. Instalar Docker e Docker Compose (se ainda não instalados)
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# 2. Clonar o repositório
cd ~
git clone https://github.com/aghiot321/To-do-List.git todolist
cd todolist

# 3. Criar arquivo .env com as variáveis de produção
cp .env.example .env
nano .env  # Edite com suas credenciais reais

# 4. Garantir que o usuário pode executar Docker sem sudo
sudo usermod -aG docker $(whoami)
newgrp docker

# 5. (Opcional) Configurar firewall
sudo ufw allow 8080/tcp
sudo ufw allow 3309/tcp
```

**⚠️ Importante**: O arquivo `.env` NÃO deve ser commitado no repositório. Ele contém credenciais sensíveis!

### 📊 Monitorando o Pipeline

1. Acesse a aba **Actions** no GitHub
2. Visualize o status de cada execução
3. Clique em uma execução para ver logs detalhados
4. Cada job (Test, Build, Deploy) pode ser expandido

### 🚀 Executando o Deploy Manualmente

Se necessário, você pode executar o pipeline manualmente:

1. Vá para **Actions** > **CI/CD Pipeline**
2. Clique em **Run workflow**
3. Selecione a branch `main`
4. Clique em **Run workflow**

### 📦 Estrutura de Tags das Imagens

As imagens Docker seguem o seguinte padrão:

```
seu_usuario/todolist-app:main-abc123def456  # SHA do commit
seu_usuario/todolist-app:latest              # Última versão
```

Isso permite rastreabilidade completa: você sempre sabe qual versão do código está rodando em produção.

### 🔧 Arquivos Importantes do CI/CD

- `.github/workflows/cicd.yml` - Definição do pipeline
- `docker-compose.prod.yml` - Configuração para produção
- `.env.example` - Template de variáveis de ambiente
- `Dockerfile` - Build multi-stage da aplicação

### 📝 Logs e Troubleshooting

Se algo der errado:

**No GitHub Actions:**
```
1. Vá para Actions > selecione a execução falhada
2. Expanda o job e step que falhou
3. Analise os logs vermelhos
```

**No Servidor:**
```bash
# Ver logs da aplicação
docker-compose -f docker-compose.prod.yml logs todolist-app

# Ver status dos containers
docker-compose -f docker-compose.prod.yml ps

# Verificar health check
curl http://localhost:8080/actuator/health
```

### ✅ Checklist de Deploy

- [ ] Servidor configurado com Docker e Docker Compose
- [ ] Repositório clonado no servidor
- [ ] Arquivo `.env` criado com credenciais reais
- [ ] Secrets configurados no GitHub
- [ ] Chave SSH configurada para acesso sem senha
- [ ] Portas 8080 e 3309 abertas no firewall
- [ ] Usuário do servidor no grupo `docker`

---
