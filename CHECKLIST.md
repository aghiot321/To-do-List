# ✅ Checklist de Implementação - CI/CD Pipeline

## 📦 Arquivos Criados/Modificados

### ✅ Arquivos Novos

- [x] `.github/workflows/cicd.yml` - Pipeline completo de CI/CD
- [x] `.env.example` - Template de variáveis de ambiente
- [x] `SECRETS_SETUP.md` - Guia detalhado de configuração dos Secrets
- [x] `setup-server.sh` - Script de configuração automática do servidor
- [x] `COMANDOS_UTEIS.md` - Referência rápida de comandos
- [x] `CHECKLIST.md` - Este arquivo

### ✅ Arquivos Modificados

- [x] `README.md` - Adicionada seção completa de CI/CD com badge
- [x] `.gitignore` - Adicionados padrões para arquivos .env de produção

### ✅ Arquivos Existentes (Já estavam prontos)

- [x] `docker-compose.prod.yml` - Configuração para produção
- [x] `Dockerfile` - Build multi-stage da aplicação
- [x] `src/test/**/*Test.java` - Testes unitários
- [x] `pom.xml` - Configuração Maven com JaCoCo

---

## 🎯 Objetivos da Atividade

### ✅ 1. Criação de Testes Unitários (CI)

**Status**: ✅ COMPLETO

- [x] Testes já existentes para `TaskController`
- [x] Testes já existentes para `UserController`
- [x] Configuração do Maven Surefire
- [x] Configuração do JaCoCo para cobertura
- [x] Script de teste: `mvn test`

**Arquivos**: 
- `src/test/java/br/com/aghiot/todolist/task/TaskControllerTest.java`
- `src/test/java/br/com/aghiot/todolist/user/UserControllerTest.java`

---

### ✅ 2. Configuração de Secrets no GitHub

**Status**: ✅ DOCUMENTADO

**Secrets necessários**:
- [x] `DOCKER_USERNAME` - Username do Docker Hub
- [x] `DOCKER_PASSWORD` - Token ou senha do Docker Hub
- [x] `SERVER_HOST` - IP ou domínio do servidor
- [x] `SERVER_USER` - Usuário SSH do servidor
- [x] `SERVER_PORT` - Porta SSH (geralmente 22)
- [x] `SSH_PRIVATE_KEY` - Chave privada SSH

**Documentação**: `SECRETS_SETUP.md`

**Ação necessária**: 
⚠️ Configurar manualmente em: `GitHub > Settings > Secrets and variables > Actions`

---

### ✅ 3. Adaptação do docker-compose.yml para Produção

**Status**: ✅ JÁ EXISTE

- [x] `docker-compose.prod.yml` criado
- [x] Usa variáveis `IMAGE_NAME` e `IMAGE_TAG`
- [x] Configurado para pull de imagens do Docker Hub
- [x] Health checks configurados

**Arquivo**: `docker-compose.prod.yml`

---

### ✅ 4. Configuração Inicial do Servidor

**Status**: ✅ DOCUMENTADO E AUTOMATIZADO

**Passos manuais documentados**:
- [x] Instruções no README.md
- [x] Script automatizado: `setup-server.sh`
- [x] Template de .env: `.env.example`

**O que fazer no servidor**:
1. Baixar e executar `setup-server.sh`
2. Editar `.env` com credenciais reais
3. Aplicar permissões Docker: `newgrp docker`

**Documentação**: `README.md` seção "Configuração Inicial do Servidor"

---

### ✅ 5. Criação do Workflow do GitHub Actions

**Status**: ✅ COMPLETO

**Jobs implementados**:
- [x] **Test** - Executa testes unitários
  - Checkout do código
  - Setup Java 21
  - Execução dos testes
  - Geração de relatório de cobertura
  - Upload de artefatos

- [x] **Build-and-Push** - Build e push da imagem Docker
  - Setup Docker Buildx
  - Login no Docker Hub
  - Extração de metadados
  - Build e push com cache
  - Tags: `main-<SHA>` e `latest`

- [x] **Deploy** - Deploy automático no servidor
  - Conexão SSH
  - Git pull
  - Docker pull da nova imagem
  - Restart dos containers
  - Health check
  - Limpeza de imagens antigas

**Arquivo**: `.github/workflows/cicd.yml`

**Triggers**:
- Push na branch `main`
- Pull requests para `main`

---

### ✅ 6. Documentação (README.md)

**Status**: ✅ COMPLETO

**Conteúdo adicionado**:
- [x] Badge de status do GitHub Actions
- [x] Explicação completa do pipeline
- [x] Lista de Secrets necessários
- [x] Passos manuais no servidor
- [x] Instruções de monitoramento
- [x] Troubleshooting
- [x] Checklist de deploy

**Seção**: "CI/CD Pipeline" no README.md

---

## 🚀 Próximos Passos

### Para o Aluno Fazer:

#### 1️⃣ Configurar Docker Hub (SE NÃO TIVER)
```bash
# Criar conta em: https://hub.docker.com/
# Anotar username e criar Access Token
```

#### 2️⃣ Configurar Servidor VPS (SE NÃO TIVER)
```bash
# Opções:
# - DigitalOcean (https://www.digitalocean.com/)
# - AWS EC2 (https://aws.amazon.com/ec2/)
# - Vultr (https://www.vultr.com/)
# - Linode (https://www.linode.com/)

# Anotar:
# - IP do servidor
# - Usuário SSH
# - Porta SSH
```

#### 3️⃣ Configurar SSH no Servidor
```bash
# 1. Gerar chave SSH local
ssh-keygen -t ed25519 -C "github-actions@todolist" -f ~/.ssh/github_actions_todolist

# 2. Copiar chave pública para servidor
ssh-copy-id -i ~/.ssh/github_actions_todolist.pub usuario@ip_servidor

# 3. Testar conexão
ssh -i ~/.ssh/github_actions_todolist usuario@ip_servidor
```

#### 4️⃣ Executar Script de Setup no Servidor
```bash
# SSH no servidor
ssh usuario@ip_servidor

# Baixar script (ou copiar manualmente)
wget https://raw.githubusercontent.com/aghiot321/To-do-List/main/setup-server.sh

# Executar
bash setup-server.sh

# Editar .env
cd ~/todolist
nano .env
# Substituir 'seu_usuario_dockerhub' pelo username real

# Aplicar permissões Docker
newgrp docker
```

#### 5️⃣ Configurar Secrets no GitHub
```bash
# 1. Acessar: https://github.com/aghiot321/To-do-List/settings/secrets/actions
# 2. Clicar em "New repository secret"
# 3. Adicionar os 6 secrets (ver SECRETS_SETUP.md)
```

#### 6️⃣ Testar Pipeline
```bash
# 1. Fazer uma alteração qualquer
echo "# Test CI/CD" >> README.md

# 2. Commit e push
git add .
git commit -m "test: testar pipeline de CI/CD"
git push origin main

# 3. Acompanhar em:
# https://github.com/aghiot321/To-do-List/actions
```

#### 7️⃣ Verificar Deploy
```bash
# SSH no servidor
ssh usuario@ip_servidor

# Verificar containers
cd ~/todolist
docker-compose -f docker-compose.prod.yml ps

# Verificar logs
docker-compose -f docker-compose.prod.yml logs -f todolist-app

# Testar aplicação
curl http://localhost:8080/actuator/health

# Testar do navegador
# http://IP_SERVIDOR:8080/actuator/health
```

#### 8️⃣ Adicionar Badge no README
```bash
# 1. GitHub > Actions > cicd.yml > ... > Create status badge
# 2. Copiar código Markdown
# 3. O badge já está no README.md, apenas confirme que funciona
```

---

## 📊 Resultados Esperados

Ao final, você terá:

- ✅ Pipeline de CI/CD funcional no GitHub Actions
- ✅ Testes executados automaticamente a cada push
- ✅ Imagem Docker construída e enviada ao Docker Hub
- ✅ Deploy automático no servidor
- ✅ Badge de status no README
- ✅ Documentação completa

---

## 🎓 Entrega

### Para Entregar:

1. **URL do Repositório GitHub**: 
   ```
   https://github.com/aghiot321/To-do-List
   ```

2. **Verificações**:
   - [ ] Pipeline está verde (passing) na aba Actions
   - [ ] Badge no README mostra status "passing"
   - [ ] Aplicação está rodando no servidor
   - [ ] Health check responde: `http://IP_SERVIDOR:8080/actuator/health`

3. **Arquivos no Repositório**:
   - [ ] `.github/workflows/cicd.yml`
   - [ ] `.env.example`
   - [ ] `README.md` atualizado
   - [ ] `SECRETS_SETUP.md`
   - [ ] `setup-server.sh`
   - [ ] `COMANDOS_UTEIS.md`

---

## 🐛 Troubleshooting Rápido

### Pipeline Falha no Job "Test"
```bash
# Testar localmente
mvn clean test

# Ver logs no GitHub Actions
# Actions > Execução falhada > Job "test" > Step com erro
```

### Pipeline Falha no Job "Build-and-Push"
```bash
# Verificar secrets Docker
# Settings > Secrets > DOCKER_USERNAME e DOCKER_PASSWORD

# Testar login local
echo "SEU_TOKEN" | docker login -u SEU_USERNAME --password-stdin
```

### Pipeline Falha no Job "Deploy"
```bash
# Verificar SSH
ssh -i ~/.ssh/github_actions_todolist usuario@ip_servidor

# Verificar se repositório existe no servidor
ssh usuario@ip_servidor "ls -la ~/todolist"

# Verificar .env no servidor
ssh usuario@ip_servidor "cat ~/todolist/.env"
```

### Aplicação Não Responde no Servidor
```bash
# SSH no servidor
ssh usuario@ip_servidor

# Ver logs
cd ~/todolist
docker-compose -f docker-compose.prod.yml logs -f

# Verificar containers
docker-compose -f docker-compose.prod.yml ps

# Reiniciar
docker-compose -f docker-compose.prod.yml restart
```

---

## 📚 Arquivos de Referência

| Arquivo | Descrição |
|---------|-----------|
| `README.md` | Visão geral e documentação principal |
| `SECRETS_SETUP.md` | Guia detalhado de configuração dos Secrets |
| `COMANDOS_UTEIS.md` | Referência rápida de comandos |
| `CHECKLIST.md` | Este arquivo - checklist de implementação |
| `setup-server.sh` | Script de configuração do servidor |
| `.env.example` | Template de variáveis de ambiente |
| `.github/workflows/cicd.yml` | Pipeline de CI/CD |
| `docker-compose.prod.yml` | Configuração Docker para produção |

---

## ✨ Recursos Extras Implementados

Além dos requisitos da atividade, foram implementados:

- ✅ Script de setup automático do servidor
- ✅ Documentação detalhada de secrets
- ✅ Referência completa de comandos úteis
- ✅ Health checks em todos os estágios
- ✅ Limpeza automática de imagens antigas
- ✅ Cache de build Docker para otimização
- ✅ Notificações de sucesso/falha no deploy
- ✅ Backup e restauração documentados
- ✅ Troubleshooting completo
- ✅ Tags de imagem com SHA do commit
- ✅ Rollback documentado

---

**🎉 Boa sorte com o deploy!**

Se tiver dúvidas, consulte a documentação em `SECRETS_SETUP.md` e `COMANDOS_UTEIS.md`.
