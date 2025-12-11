# 🚀 Quick Start - CI/CD Pipeline

**Guia rápido para colocar o pipeline CI/CD em funcionamento em 10 minutos!**

---

## ⚡ Setup Rápido (10 minutos)

### 1️⃣ Pré-requisitos (2 min)

Você precisa de:
- [ ] Conta no Docker Hub → [Criar aqui](https://hub.docker.com/)
- [ ] Servidor VPS com Ubuntu → [DigitalOcean](https://www.digitalocean.com/), [AWS EC2](https://aws.amazon.com/ec2/), [Vultr](https://www.vultr.com/)
- [ ] Repositório no GitHub → [Este repositório](https://github.com/aghiot321/To-do-List)

---

### 2️⃣ Configurar Docker Hub (1 min)

```bash
# 1. Acesse: https://hub.docker.com/
# 2. Faça login
# 3. Vá em Account Settings > Security > New Access Token
# 4. Nome: "GitHub Actions"
# 5. Permissões: Read, Write, Delete
# 6. COPIE O TOKEN (formato: dckr_pat_xxxxx)
```

**Anote**:
- ✏️ Username: `__________________`
- ✏️ Token: `__________________`

---

### 3️⃣ Configurar Servidor (3 min)

```bash
# SSH no servidor
ssh root@SEU_IP_SERVIDOR

# Baixar e executar script de setup
curl -fsSL https://raw.githubusercontent.com/aghiot321/To-do-List/main/setup-server.sh -o setup.sh
bash setup.sh

# Editar arquivo .env
cd ~/todolist
nano .env
# Substituir 'seu_usuario_dockerhub' pelo seu username

# Aplicar permissões
newgrp docker

# Testar
docker ps
```

**Anote**:
- ✏️ IP do Servidor: `__________________`
- ✏️ Usuário SSH: `__________________` (ex: root, ubuntu)
- ✏️ Porta SSH: `__________________` (geralmente: 22)

---

### 4️⃣ Configurar SSH (2 min)

**No seu computador local:**

```bash
# Windows (PowerShell)
ssh-keygen -t ed25519 -C "github-actions" -f $HOME\.ssh\github_actions

# Linux/Mac
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions

# IMPORTANTE: Quando pedir passphrase, deixe em branco (pressione Enter)

# Copiar chave pública para servidor
# Windows (PowerShell)
cat $HOME\.ssh\github_actions.pub | ssh SEU_USUARIO@SEU_IP "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"

# Linux/Mac
ssh-copy-id -i ~/.ssh/github_actions.pub SEU_USUARIO@SEU_IP

# Testar conexão
# Windows
ssh -i $HOME\.ssh\github_actions SEU_USUARIO@SEU_IP

# Linux/Mac
ssh -i ~/.ssh/github_actions SEU_USUARIO@SEU_IP
```

**Se funcionou sem pedir senha, ótimo! Continue →**

---

### 5️⃣ Configurar Secrets no GitHub (2 min)

```bash
# 1. Acesse: https://github.com/aghiot321/To-do-List/settings/secrets/actions
# 2. Clique em "New repository secret" para cada um:
```

| Nome | Valor | Onde Pegar |
|------|-------|------------|
| `DOCKER_USERNAME` | seu_usuario_dockerhub | Passo 2 |
| `DOCKER_PASSWORD` | dckr_pat_xxxxx | Passo 2 |
| `SERVER_HOST` | 192.168.1.100 | IP do servidor |
| `SERVER_USER` | root ou ubuntu | Usuário SSH |
| `SERVER_PORT` | 22 | Porta SSH |
| `SSH_PRIVATE_KEY` | -----BEGIN... | Ver abaixo 👇 |

**Para copiar a chave privada**:

```bash
# Windows (PowerShell)
Get-Content $HOME\.ssh\github_actions | clip

# Linux
cat ~/.ssh/github_actions | xclip -selection clipboard

# Mac
cat ~/.ssh/github_actions | pbcopy
```

Cole **TODO** o conteúdo (incluindo `-----BEGIN...` e `-----END...`) no secret.

---

### 6️⃣ Testar Pipeline! (Automático)

```bash
# Fazer qualquer alteração
git pull  # Certifique-se de ter a versão mais recente
echo "# CI/CD configurado!" >> README.md

# Commit e push
git add .
git commit -m "test: testar CI/CD pipeline"
git push origin main

# Acompanhar em:
# https://github.com/aghiot321/To-do-List/actions
```

**Aguarde 3-5 minutos** → Pipeline deve ficar verde ✅

---

## ✅ Verificação Final

### No GitHub
- [ ] Actions → Última execução está verde (passing)
- [ ] README → Badge mostra "passing"

### No Servidor
```bash
ssh SEU_USUARIO@SEU_IP

# Ver containers rodando
docker ps

# Ver logs da aplicação
docker logs todolist-app

# Testar API
curl http://localhost:8080/actuator/health
# Deve retornar: {"status":"UP"}
```

### Do Navegador
```
http://SEU_IP:8080/actuator/health
```

Se retornar `{"status":"UP"}`, **PARABÉNS! 🎉**

---

## 🎯 O Que Acontece Agora?

Toda vez que você fizer `git push origin main`:

1. **Testes** executam automaticamente
2. **Imagem Docker** é criada e enviada ao Docker Hub
3. **Deploy** é feito automaticamente no servidor
4. **Aplicação** atualiza sozinha

**É isso! Simples assim! 🚀**

---

## 🐛 Algo Deu Errado?

### Pipeline Vermelho no GitHub?
```bash
# Acesse: https://github.com/aghiot321/To-do-List/actions
# Clique na execução falhada
# Expanda o job com erro
# Leia a mensagem de erro
```

**Erros comuns**:

| Erro | Solução |
|------|---------|
| `Authentication failed` (Docker) | Verifique DOCKER_USERNAME e DOCKER_PASSWORD |
| `Permission denied (publickey)` | Verifique SSH_PRIVATE_KEY |
| `Connection refused` | Verifique SERVER_HOST e SERVER_PORT |
| `Tests failed` | Execute `mvn test` localmente e corrija |

### Aplicação Não Responde?
```bash
# SSH no servidor
ssh SEU_USUARIO@SEU_IP

# Ver status
docker ps

# Ver logs
docker logs todolist-app

# Reiniciar se necessário
docker restart todolist-app
```

---

## 📚 Próximos Passos

Agora que o CI/CD está funcionando:

1. **Desenvolva com confiança**
   - Faça alterações no código
   - Os testes garantem que nada quebrou
   - O deploy é automático

2. **Monitore a aplicação**
   - Verifique logs: `docker logs -f todolist-app`
   - Monitore health: `curl http://localhost:8080/actuator/health`

3. **Aprenda mais**
   - Leia: `COMANDOS_UTEIS.md`
   - Explore: `SECRETS_SETUP.md`
   - Confira: `CHECKLIST.md`

---

## 🆘 Precisa de Ajuda?

1. **Documentação completa**: `README.md`
2. **Setup detalhado**: `SECRETS_SETUP.md`
3. **Comandos úteis**: `COMANDOS_UTEIS.md`
4. **Checklist completo**: `CHECKLIST.md`

---

## 🎁 Bônus: Comandos Úteis

```bash
# Ver status do pipeline
gh run list --workflow=cicd.yml

# Ver logs do último run
gh run view --log

# Fazer rollback
ssh SEU_USUARIO@SEU_IP
cd ~/todolist
git checkout COMMIT_ANTERIOR
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d

# Ver métricas
docker stats
```

---

**🎉 Parabéns! Seu pipeline de CI/CD está pronto!**

Qualquer dúvida, consulte a documentação completa nos arquivos:
- `README.md` - Visão geral
- `SECRETS_SETUP.md` - Setup detalhado
- `COMANDOS_UTEIS.md` - Referência rápida
- `CHECKLIST.md` - Checklist completo
