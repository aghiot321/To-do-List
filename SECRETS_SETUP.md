# 🔐 Guia de Configuração de Secrets no GitHub

Este documento fornece instruções passo a passo para configurar os secrets necessários no GitHub para o pipeline de CI/CD funcionar corretamente.

---

## 📍 Onde Configurar os Secrets

1. Acesse seu repositório no GitHub: `https://github.com/aghiot321/To-do-List`
2. Clique em **Settings** (Configurações)
3. No menu lateral, clique em **Secrets and variables** > **Actions**
4. Clique no botão **New repository secret**

---

## 🔑 Secrets Necessários

### 1. DOCKER_USERNAME

**Descrição**: Seu nome de usuário do Docker Hub

**Como obter**:
- Acesse [hub.docker.com](https://hub.docker.com/)
- Faça login ou crie uma conta
- Seu username aparece no canto superior direito

**Exemplo**: `aghiot321`

**Passos no GitHub**:
1. Name: `DOCKER_USERNAME`
2. Secret: `seu_usuario_dockerhub`
3. Clique em **Add secret**

---

### 2. DOCKER_PASSWORD

**Descrição**: Token de acesso do Docker Hub (recomendado) ou sua senha

**Como obter um Access Token (RECOMENDADO)**:
1. Acesse [hub.docker.com](https://hub.docker.com/)
2. Clique no seu perfil > **Account Settings**
3. Clique em **Security** > **New Access Token**
4. Dê um nome descritivo: `GitHub Actions CI/CD`
5. Permissões: **Read, Write, Delete**
6. Clique em **Generate**
7. **COPIE O TOKEN AGORA** (só será exibido uma vez!)

**Formato do token**: `dckr_pat_xxxxxxxxxxxxxxxxxxxxxx`

**Passos no GitHub**:
1. Name: `DOCKER_PASSWORD`
2. Secret: Cole o token gerado (ou sua senha)
3. Clique em **Add secret**

---

### 3. SERVER_HOST

**Descrição**: Endereço IP público ou domínio do seu servidor VPS

**Como obter**:
- **DigitalOcean**: No painel de controle, veja o IP do Droplet
- **AWS EC2**: No console EC2, veja o Public IPv4
- **Vultr**: No painel do servidor, veja o IP Address
- **Seu domínio**: Se configurou DNS, use `todolist.exemplo.com`

**Exemplos**: 
- `192.168.1.100`
- `203.0.113.42`
- `todolist.exemplo.com`

**Passos no GitHub**:
1. Name: `SERVER_HOST`
2. Secret: `seu_ip_ou_dominio`
3. Clique em **Add secret**

---

### 4. SERVER_USER

**Descrição**: Nome do usuário SSH para conectar ao servidor

**Usuários comuns por provedor**:
- **Ubuntu/Debian**: `ubuntu` ou `root`
- **CentOS**: `centos` ou `root`
- **Fedora**: `fedora` ou `root`
- **Customizado**: O usuário que você criou

**⚠️ Importante**: Este usuário deve:
- Ter acesso SSH ao servidor
- Estar no grupo `docker` (ou ter permissões Docker)
- Ter permissões para executar `git pull`, `docker-compose`, etc.

**Passos no GitHub**:
1. Name: `SERVER_USER`
2. Secret: `ubuntu` (ou seu usuário)
3. Clique em **Add secret**

---

### 5. SERVER_PORT

**Descrição**: Porta SSH do servidor (geralmente 22)

**Valor padrão**: `22`

**Se você alterou a porta SSH**: use o número customizado (ex: `2222`, `22000`)

**Passos no GitHub**:
1. Name: `SERVER_PORT`
2. Secret: `22`
3. Clique em **Add secret**

---

### 6. SSH_PRIVATE_KEY

**Descrição**: Chave privada SSH para autenticação sem senha

**Como gerar e configurar**:

#### Opção A: Gerar Nova Chave SSH (Recomendado)

**No seu computador local**:

```bash
# Gerar par de chaves SSH
ssh-keygen -t ed25519 -C "github-actions@todolist" -f ~/.ssh/github_actions_todolist

# Quando perguntado por passphrase, deixe em branco (apenas pressione Enter)
```

Isso criará dois arquivos:
- `~/.ssh/github_actions_todolist` (chave privada - vai para o GitHub Secret)
- `~/.ssh/github_actions_todolist.pub` (chave pública - vai para o servidor)

**Copiar chave pública para o servidor**:

```bash
# Substituir USER e SERVER_IP pelos seus valores
ssh-copy-id -i ~/.ssh/github_actions_todolist.pub USER@SERVER_IP

# OU manualmente:
cat ~/.ssh/github_actions_todolist.pub | ssh USER@SERVER_IP "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"
```

**Obter a chave privada**:

```bash
# Windows (PowerShell)
Get-Content ~\.ssh\github_actions_todolist | clip

# Linux/Mac
cat ~/.ssh/github_actions_todolist | pbcopy  # Mac
cat ~/.ssh/github_actions_todolist | xclip -selection clipboard  # Linux
```

**Testar a conexão**:

```bash
ssh -i ~/.ssh/github_actions_todolist USER@SERVER_IP
```

Se conectar sem pedir senha, está funcionando!

#### Opção B: Usar Chave Existente

Se você já usa uma chave SSH para acessar o servidor:

```bash
# Windows (PowerShell)
Get-Content ~\.ssh\id_rsa | clip

# Linux/Mac
cat ~/.ssh/id_rsa | pbcopy  # Mac
cat ~/.ssh/id_rsa | xclip -selection clipboard  # Linux
```

**Formato esperado**:

```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACBqjVN5VzVN5VzVN5VzVN5VzVN5VzVN5VzVN5VzVAAAAJBAAAAJBAAAAtzc2
...
(várias linhas)
...
-----END OPENSSH PRIVATE KEY-----
```

**Passos no GitHub**:
1. Name: `SSH_PRIVATE_KEY`
2. Secret: Cole **TODO** o conteúdo da chave privada (incluindo BEGIN e END)
3. Clique em **Add secret**

---

## ✅ Verificação Final

Após adicionar todos os secrets, você deve ter **6 secrets** configurados:

- [x] DOCKER_USERNAME
- [x] DOCKER_PASSWORD
- [x] SERVER_HOST
- [x] SERVER_USER
- [x] SERVER_PORT
- [x] SSH_PRIVATE_KEY

**Para verificar**:
1. Vá em **Settings** > **Secrets and variables** > **Actions**
2. Confirme que todos os 6 secrets estão listados
3. Você verá apenas os nomes, não os valores (por segurança)

---

## 🧪 Testando a Configuração

### Teste 1: Login Docker Hub

```bash
# No seu terminal local
echo "SEU_DOCKER_PASSWORD" | docker login -u SEU_DOCKER_USERNAME --password-stdin
```

Se retornar `Login Succeeded`, as credenciais Docker estão corretas.

### Teste 2: Conexão SSH

```bash
# Substituir pelos seus valores
ssh -i ~/.ssh/github_actions_todolist -p 22 ubuntu@192.168.1.100
```

Se conectar sem pedir senha, a configuração SSH está correta.

### Teste 3: Permissões Docker no Servidor

```bash
# Conecte ao servidor e execute
docker ps
```

Se listar containers (ou retornar lista vazia), as permissões estão corretas.
Se retornar erro de permissão:

```bash
# Adicione seu usuário ao grupo docker
sudo usermod -aG docker $USER
newgrp docker
# Teste novamente
docker ps
```

---

## 🔒 Boas Práticas de Segurança

1. **Nunca commite secrets no código**
   - Use sempre GitHub Secrets
   - Adicione `.env` no `.gitignore`

2. **Use Access Tokens ao invés de senhas**
   - Tokens podem ser revogados sem mudar sua senha
   - Permissões granulares

3. **Rotação de chaves**
   - Troque as chaves SSH periodicamente
   - Revogue tokens antigos no Docker Hub

4. **Chaves SSH dedicadas**
   - Use uma chave SSH específica para CI/CD
   - Não reutilize sua chave pessoal

5. **Backup das chaves**
   - Guarde cópias seguras das chaves privadas
   - Use gerenciador de senhas (1Password, Bitwarden, etc.)

---

## ❓ Problemas Comuns

### Erro: "Permission denied (publickey)"

**Causa**: Chave SSH não configurada corretamente

**Solução**:
1. Verifique se a chave pública está em `~/.ssh/authorized_keys` no servidor
2. Verifique permissões: `chmod 600 ~/.ssh/authorized_keys`
3. Teste manualmente: `ssh -i chave_privada user@host`

### Erro: "denied: requested access to the resource is denied"

**Causa**: Credenciais Docker incorretas

**Solução**:
1. Verifique `DOCKER_USERNAME` (sem espaços ou caracteres especiais)
2. Regenere o Access Token no Docker Hub
3. Atualize o secret `DOCKER_PASSWORD`

### Erro: "No such file or directory: docker-compose.prod.yml"

**Causa**: Repositório não clonado no servidor

**Solução**:
1. SSH no servidor
2. `cd ~ && git clone https://github.com/aghiot321/To-do-List.git todolist`
3. Verifique: `ls ~/todolist/docker-compose.prod.yml`

---

## 📞 Suporte

Se tiver problemas:

1. Verifique os logs do GitHub Actions
2. Verifique os logs do servidor: `docker-compose logs`
3. Teste cada componente individualmente (Docker, SSH, Git)
4. Consulte a documentação oficial:
   - [GitHub Actions](https://docs.github.com/en/actions)
   - [Docker Hub](https://docs.docker.com/docker-hub/)
   - [SSH Key Authentication](https://www.ssh.com/academy/ssh/key)
