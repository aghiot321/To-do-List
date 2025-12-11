# 📝 Comandos Úteis - CI/CD e Deploy

Este documento contém comandos úteis para gerenciar o pipeline de CI/CD e a aplicação em produção.

---

## 🖥️ Comandos no Servidor de Produção

### Gerenciamento de Containers

```bash
# Ver status dos containers
docker-compose -f docker-compose.prod.yml ps

# Ver logs da aplicação
docker-compose -f docker-compose.prod.yml logs -f todolist-app

# Ver logs do MySQL
docker-compose -f docker-compose.prod.yml logs -f mysql

# Ver logs de todos os serviços
docker-compose -f docker-compose.prod.yml logs -f

# Reiniciar aplicação
docker-compose -f docker-compose.prod.yml restart todolist-app

# Reiniciar MySQL
docker-compose -f docker-compose.prod.yml restart mysql

# Parar todos os serviços
docker-compose -f docker-compose.prod.yml down

# Iniciar todos os serviços
docker-compose -f docker-compose.prod.yml up -d

# Recriar containers (força rebuild)
docker-compose -f docker-compose.prod.yml up -d --force-recreate
```

### Verificação de Health

```bash
# Verificar health da aplicação
curl http://localhost:8080/actuator/health

# Verificar health detalhado
curl http://localhost:8080/actuator/health | jq

# Verificar se MySQL está respondendo
docker-compose -f docker-compose.prod.yml exec mysql mysqladmin ping -h localhost -u root -p

# Testar conexão com o banco
docker-compose -f docker-compose.prod.yml exec mysql mysql -u todolist_user -p todolist -e "SELECT 1;"
```

### Gerenciamento de Imagens Docker

```bash
# Listar imagens
docker images

# Remover imagens antigas (manter apenas as 3 mais recentes)
docker image prune -af --filter "until=72h"

# Baixar nova versão manualmente
docker pull seu_usuario/todolist-app:latest

# Ver histórico de uma imagem
docker history seu_usuario/todolist-app:latest

# Inspecionar imagem
docker inspect seu_usuario/todolist-app:latest
```

### Atualização Manual

```bash
# Navegar para o diretório do projeto
cd ~/todolist

# Atualizar código do repositório
git pull origin main

# Definir variáveis de ambiente
export IMAGE_NAME=seu_usuario/todolist-app
export IMAGE_TAG=latest

# Baixar nova imagem
docker pull ${IMAGE_NAME}:${IMAGE_TAG}

# Parar serviços antigos
docker-compose -f docker-compose.prod.yml down

# Iniciar com nova versão
docker-compose -f docker-compose.prod.yml up -d

# Verificar logs
docker-compose -f docker-compose.prod.yml logs -f
```

### Backup e Restauração

```bash
# Criar backup do banco de dados
docker-compose -f docker-compose.prod.yml exec mysql mysqldump \
  -u todolist_user -p todolist > backup_$(date +%Y%m%d_%H%M%S).sql

# Restaurar backup
docker-compose -f docker-compose.prod.yml exec -T mysql mysql \
  -u todolist_user -p todolist < backup_20231210_153000.sql

# Backup dos volumes Docker
docker run --rm -v todolist_mysql_data:/data -v $(pwd):/backup \
  alpine tar czf /backup/mysql_data_backup_$(date +%Y%m%d).tar.gz -C /data .

# Restaurar volume
docker run --rm -v todolist_mysql_data:/data -v $(pwd):/backup \
  alpine tar xzf /backup/mysql_data_backup_20231210.tar.gz -C /data
```

### Limpeza e Manutenção

```bash
# Limpar containers parados
docker container prune -f

# Limpar imagens não utilizadas
docker image prune -af

# Limpar volumes não utilizados (CUIDADO!)
docker volume prune -f

# Limpar tudo (containers, imagens, volumes, networks)
docker system prune -af --volumes

# Ver uso de espaço do Docker
docker system df

# Ver uso detalhado
docker system df -v
```

---

## 💻 Comandos Locais (Desenvolvimento)

### Testes

```bash
# Executar todos os testes
mvn test

# Executar testes de uma classe específica
mvn test -Dtest=TaskControllerTest

# Executar teste específico
mvn test -Dtest=TaskControllerTest#testCreateTask

# Executar testes com cobertura
mvn clean test jacoco:report

# Ver relatório de cobertura
open target/site/jacoco/index.html  # Mac
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html  # Windows
```

### Build Local

```bash
# Build da aplicação
mvn clean package

# Build pulando testes
mvn clean package -DskipTests

# Build e executar localmente
mvn spring-boot:run

# Build da imagem Docker localmente
docker build -t todolist-app:local .

# Executar imagem local
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3309/todolist \
  -e SPRING_DATASOURCE_USERNAME=todolist_user \
  -e SPRING_DATASOURCE_PASSWORD=todolist_password \
  todolist-app:local
```

### Docker Compose Local

```bash
# Iniciar ambiente de desenvolvimento
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar ambiente
docker-compose down

# Parar e remover volumes (limpar banco)
docker-compose down -v

# Rebuild da aplicação
docker-compose up -d --build
```

---

## 🐙 Comandos GitHub e Git

### Gerenciamento de Branches

```bash
# Ver branch atual
git branch

# Criar nova branch
git checkout -b feature/nova-funcionalidade

# Mudar para branch main
git checkout main

# Atualizar branch local
git pull origin main

# Fazer merge de feature na main
git checkout main
git merge feature/nova-funcionalidade
```

### Commits e Push

```bash
# Ver status
git status

# Adicionar arquivos
git add .

# Commit
git commit -m "feat: adicionar nova funcionalidade"

# Push para main (dispara CI/CD)
git push origin main

# Push de outra branch
git push origin feature/nova-funcionalidade

# Ver histórico
git log --oneline --graph --all
```

### Tags

```bash
# Criar tag
git tag -a v1.0.0 -m "Release v1.0.0"

# Enviar tag para GitHub
git push origin v1.0.0

# Listar tags
git tag -l

# Deletar tag local
git tag -d v1.0.0

# Deletar tag remota
git push origin --delete v1.0.0
```

---

## 🔐 Gerenciamento de Secrets

### Testar Secrets Localmente

```bash
# Login no Docker Hub
echo "SEU_TOKEN" | docker login -u SEU_USERNAME --password-stdin

# Testar SSH
ssh -i ~/.ssh/github_actions_todolist usuario@servidor

# Testar SSH com porta customizada
ssh -i ~/.ssh/github_actions_todolist -p 2222 usuario@servidor

# Copiar chave pública para servidor
ssh-copy-id -i ~/.ssh/github_actions_todolist.pub usuario@servidor
```

### Gerar Chaves SSH

```bash
# Gerar nova chave ED25519 (recomendado)
ssh-keygen -t ed25519 -C "github-actions@todolist" -f ~/.ssh/github_actions_todolist

# Gerar chave RSA (compatibilidade)
ssh-keygen -t rsa -b 4096 -C "github-actions@todolist" -f ~/.ssh/github_actions_rsa

# Ver chave pública
cat ~/.ssh/github_actions_todolist.pub

# Ver chave privada (para adicionar no Secret)
cat ~/.ssh/github_actions_todolist

# Copiar chave para clipboard
# Mac
cat ~/.ssh/github_actions_todolist | pbcopy
# Linux
cat ~/.ssh/github_actions_todolist | xclip -selection clipboard
# Windows (PowerShell)
Get-Content ~\.ssh\github_actions_todolist | clip
```

---

## 🐳 Docker Hub

### Gerenciamento de Imagens

```bash
# Login
docker login

# Push manual de imagem
docker tag todolist-app:local seu_usuario/todolist-app:v1.0.0
docker push seu_usuario/todolist-app:v1.0.0

# Pull de imagem
docker pull seu_usuario/todolist-app:latest

# Listar tags de uma imagem (requer API ou site)
# Acesse: https://hub.docker.com/r/seu_usuario/todolist-app/tags

# Deletar tag local
docker rmi seu_usuario/todolist-app:v1.0.0

# Deletar tag no Docker Hub (via site ou API)
# Site: https://hub.docker.com/r/seu_usuario/todolist-app/tags
```

---

## 📊 Monitoramento

### Logs do GitHub Actions

```bash
# Ver runs do workflow
gh run list --workflow=cicd.yml

# Ver detalhes de um run
gh run view <run-id>

# Ver logs de um run
gh run view <run-id> --log

# Cancelar run em andamento
gh run cancel <run-id>

# Re-executar run falhado
gh run rerun <run-id>
```

### Métricas do Servidor

```bash
# Ver uso de CPU e memória dos containers
docker stats

# Ver uso de disco
df -h

# Ver processos Docker
docker ps -a

# Ver uso de rede
docker network inspect todolist-network

# Ver logs do sistema
journalctl -u docker -f
```

---

## 🔧 Troubleshooting

### Problemas Comuns

```bash
# Container não inicia
docker-compose -f docker-compose.prod.yml logs todolist-app
docker-compose -f docker-compose.prod.yml ps

# Aplicação não conecta ao MySQL
docker-compose -f docker-compose.prod.yml exec todolist-app env | grep SPRING_DATASOURCE

# Verificar conectividade entre containers
docker-compose -f docker-compose.prod.yml exec todolist-app ping mysql

# Entrar no container para debug
docker-compose -f docker-compose.prod.yml exec todolist-app sh

# Verificar variáveis de ambiente
docker-compose -f docker-compose.prod.yml exec todolist-app env

# Verificar portas em uso
netstat -tulpn | grep :8080
sudo lsof -i :8080

# Resetar completamente (CUIDADO: apaga dados!)
docker-compose -f docker-compose.prod.yml down -v
docker system prune -af --volumes
docker-compose -f docker-compose.prod.yml up -d
```

### Verificar Conectividade

```bash
# Testar porta da aplicação
curl http://localhost:8080/actuator/health

# Testar do exterior (substitua IP)
curl http://192.168.1.100:8080/actuator/health

# Verificar se porta está aberta
nc -zv localhost 8080

# Verificar firewall
sudo ufw status
sudo iptables -L -n | grep 8080
```

---

## 📝 Exemplos de Uso da API

### Endpoints da Aplicação

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Criar usuário
curl -X POST http://localhost:8080/users/ \
  -H "Content-Type: application/json" \
  -d '{"name":"João Silva","username":"joao","password":"senha123"}'

# Listar usuários
curl http://localhost:8080/users/

# Criar tarefa
curl -X POST http://localhost:8080/tasks/ \
  -H "Content-Type: application/json" \
  -d '{"title":"Minha Tarefa","description":"Descrição","priority":"ALTA","userId":"<UUID>"}'

# Listar tarefas
curl http://localhost:8080/tasks/

# Buscar tarefa por ID
curl http://localhost:8080/tasks/<UUID>

# Atualizar tarefa
curl -X PUT http://localhost:8080/tasks/<UUID> \
  -H "Content-Type: application/json" \
  -d '{"title":"Tarefa Atualizada","description":"Nova descrição","priority":"MEDIA"}'

# Deletar tarefa
curl -X DELETE http://localhost:8080/tasks/<UUID>
```

---

## 🚀 Quick Reference

### Pipeline Completo

```bash
# 1. Desenvolvimento local
mvn test
git add .
git commit -m "feat: nova funcionalidade"
git push origin main

# 2. GitHub Actions (automático)
# - Executa testes
# - Build da imagem Docker
# - Push para Docker Hub
# - Deploy no servidor

# 3. Verificar no servidor
ssh usuario@servidor
cd ~/todolist
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f todolist-app
curl http://localhost:8080/actuator/health
```

### Rollback Rápido

```bash
# No servidor
cd ~/todolist

# Voltar para versão anterior do código
git log --oneline  # Ver commits
git checkout <commit-hash-anterior>

# Ou usar tag anterior
git checkout v1.0.0

# Deploy da versão anterior
export IMAGE_NAME=seu_usuario/todolist-app
export IMAGE_TAG=<tag-anterior>  # ex: main-abc123def

docker-compose -f docker-compose.prod.yml down
docker pull ${IMAGE_NAME}:${IMAGE_TAG}
docker-compose -f docker-compose.prod.yml up -d
```

---

**💡 Dica**: Salve este arquivo para referência rápida durante o desenvolvimento e deploy!
