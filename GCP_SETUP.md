# 🚀 Setup Google Cloud Platform (GCP) - Guia Completo GRATUITO

Este guia mostrará como configurar uma VM gratuita no Google Cloud para hospedar sua aplicação ToDoList.

---

## 💰 Plano Gratuito do GCP

**O que você ganha PERMANENTEMENTE grátis**:
- ✅ 1 VM e2-micro (0.25-1 vCPU, 1GB RAM)
- ✅ 30GB de armazenamento SSD padrão
- ✅ 1GB de tráfego de rede por mês (América do Norte)
- ✅ IP externo gratuito (ephemeral)
- ✅ **NUNCA EXPIRA** (ao contrário do trial de $300)

**Regiões gratuitas**: 
- us-west1 (Oregon)
- us-central1 (Iowa)
- us-east1 (Carolina do Sul)

---

## 📋 Pré-requisitos

- [ ] Conta Google (Gmail)
- [ ] Cartão de crédito/débito (para verificação - não será cobrado no free tier)
- [ ] 30 minutos do seu tempo

---

## 🎯 Passo a Passo Completo

### 1️⃣ Criar Conta no Google Cloud (5 min)

1. Acesse: https://console.cloud.google.com/
2. Clique em "Começar gratuitamente" ou "Start Free"
3. Faça login com sua conta Google
4. Preencha os dados:
   - País: Brasil
   - Tipo de conta: Individual
   - Concordar com os termos
5. Adicione cartão de crédito (apenas para verificação)
6. Clique em "Iniciar meu teste gratuito"

**Você ganhará**:
- $300 de crédito por 90 dias (trial)
- Acesso ao Free Tier permanente

**⚠️ Importante**: Mesmo após o trial, a VM e2-micro continua gratuita!

---

### 2️⃣ Criar Projeto (2 min)

1. No console do GCP, clique em "Selecionar projeto" (topo da página)
2. Clique em "Novo projeto"
3. Nome do projeto: `todolist-prod`
4. Clique em "Criar"
5. Aguarde alguns segundos
6. Selecione o projeto criado

---

### 3️⃣ Criar Instância de VM (10 min)

1. No menu lateral, vá em: **Compute Engine** > **Instâncias de VM**
2. Se aparecer, clique em "Ativar API" e aguarde 1-2 minutos
3. Clique em "Criar instância"

**Configurações da VM**:

**Nome**: `todolist-server`

**Região**: `us-west1` (Oregon) ⚠️ **OBRIGATÓRIO PARA SER GRÁTIS**

**Zona**: `us-west1-b`

**Série da máquina**: `E2`

**Tipo de máquina**: `e2-micro` (0.25-1 vCPU, 1GB RAM) ⚠️ **OBRIGATÓRIO PARA SER GRÁTIS**

**Disco de inicialização**: Clique em "Alterar"
- Sistema operacional: `Ubuntu`
- Versão: `Ubuntu 22.04 LTS`
- Tipo de disco de inicialização: `Disco permanente padrão`
- Tamanho: `30 GB` (máximo gratuito)
- Clique em "Selecionar"

**Firewall**:
- ✅ Permitir tráfego HTTP
- ✅ Permitir tráfego HTTPS

**Redes, discos, segurança, gerenciamento, locatário individual**:
- Clique para expandir essa seção (se estiver recolhida)
- Clique na aba **"Redes"**
- Você verá:
  - Tags de rede (deixe em branco por enquanto)
  - Nome do host (deixe padrão)
  - Encaminhamento de IP (deixe padrão)
  - Configuração de desempenho de rede (deixe padrão)
- Role até **"Interfaces de rede"**
- Clique na **setinha para baixo** ▼ para expandir os detalhes
- Você verá vários campos: Rede, Sub-rede, Placa de rede, Tipo de pilha de IP, etc.
- Procure o campo **"IP externo"** (pode estar mais abaixo)
- Clique no dropdown de **"IP externo"**
- Selecione **"Ephemeral"** 
- Se aparecer opções como "Criar endereço IP" ou "Reservar", NÃO selecione (isso custa $3/mês)
- Deixe as outras opções como padrão

**Clique em "Criar"** (botão azul no final da página) e aguarde 30-60 segundos

---

### 4️⃣ Configurar Regras de Firewall (5 min)

Precisamos abrir as portas 8080 (aplicação) e 3309 (MySQL).

1. Menu lateral: **VPC Network** > **Firewall**
2. Clique em "Criar regra de firewall"

**Regra 1 - Aplicação (porta 8080)**:
- Nome: `allow-todolist-app`
- Logs: Desativado
- Rede: `default`
- Prioridade: `1000`
- Direção do tráfego: `Entrada`
- Ação em caso de correspondência: `Permitir`
- Destinos: `Tags de destino especificadas`
- Tags de destino: `todolist-server`
- Filtro de origem: `Intervalos de IPv4`
- Intervalos de IPv4 de origem: `0.0.0.0/0`
- Protocolos e portas: `Protocolos e portas especificadas`
  - ✅ TCP: `8080`
- Clique em "Criar"

**Regra 2 - MySQL (porta 3309)** (Opcional - apenas se precisar acesso externo):
- Repita o processo acima, mas com:
  - Nome: `allow-mysql`
  - TCP: `3309`

**Adicionar tag à VM**:
1. Volte em **Compute Engine** > **Instâncias de VM**
2. Clique no nome da VM `todolist-server`
3. Clique em "Editar"
4. Role até "Tags de rede"
5. Adicione: `todolist-server`
6. Clique em "Salvar"

---

### 5️⃣ Conectar à VM via SSH (2 min)

**Opção A - Pelo Console (Mais Fácil)**:
1. Em **Compute Engine** > **Instâncias de VM**
2. Na linha da sua VM, clique em "SSH"
3. Uma janela de terminal abrirá no navegador

**Opção B - Por Chave SSH Local**:

No seu computador (PowerShell):

```powershell
# Gerar chave SSH
ssh-keygen -t ed25519 -C "seu_email@gmail.com" -f $HOME\.ssh\gcp_todolist

# Ver chave pública
Get-Content $HOME\.ssh\gcp_todolist.pub
```

No GCP:
1. **Compute Engine** > **Instâncias de VM**
2. Clique no nome da VM
3. Clique em "Editar"
4. Role até "Chaves SSH"
5. Clique em "Adicionar item"
6. Cole a chave pública
7. Clique em "Salvar"

Conectar:
```powershell
# Obtenha o IP externo da VM (mostrado na lista de VMs)
ssh -i $HOME\.ssh\gcp_todolist seu_usuario@IP_EXTERNO
```

---

### 6️⃣ Configurar o Servidor (10 min)

**Execute estes comandos na VM** (conectado via SSH):

```bash
# Atualizar sistema
sudo apt-get update -qq
sudo apt-get upgrade -y -qq

# Instalar Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Instalar Git
sudo apt-get install -y git

# Aplicar permissões Docker (IMPORTANTE)
newgrp docker

# Verificar instalações
docker --version
docker-compose --version
git --version
```

---

### 7️⃣ Clonar Repositório e Configurar (5 min)

```bash
# Clonar repositório
cd ~
git clone https://github.com/aghiot321/To-do-List.git todolist
cd todolist

# Criar arquivo .env
nano .env
```

**Cole este conteúdo no .env** (pressione Ctrl+O para salvar, Ctrl+X para sair):

```env
# MySQL
MYSQL_ROOT_PASSWORD=maly/jubileu3
MYSQL_DATABASE=todolist
MYSQL_USER=todolist_user
MYSQL_PASSWORD=maly/jubileu3
MYSQL_PORT=3309

# Docker
IMAGE_NAME=aghiot/todolist
IMAGE_TAG=latest

# Spring
SPRING_PROFILES_ACTIVE=prod
```

**⚠️ IMPORTANTE**: Substitua `seu_usuario_dockerhub` pelo seu username real!

---

### 8️⃣ Obter Informações para GitHub Secrets (5 min)

**Anote essas informações**:

```bash
# IP externo da VM (veja no console do GCP ou execute)
curl -s http://checkip.amazonaws.com
35.212.146.198
# Usuário SSH (geralmente seu nome de usuário do Google)
whoami
triguinhogemeos
# Porta SSH (padrão)
# 22

# Chave privada SSH (se criou no passo 5B)
# Copie o conteúdo de: $HOME\.ssh\gcp_todolist (no seu PC)
```

**Se usou SSH pelo Console do GCP** (Opção A), você precisa gerar chave SSH:

```bash
# Na VM, execute:
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions
# Pressione Enter 3 vezes (sem passphrase)

# Adicionar à authorized_keys
cat ~/.ssh/github_actions.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# Ver chave privada (COPIE TODO O CONTEÚDO)
cat ~/.ssh/github_actions
```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACCvc8EBVq9TDdSEfTNB/RV87tpnhGHBEaRPB4eIBHzs8wAAAJh4+jk7ePo5
OwAAAAtzc2gtZWQyNTUxOQAAACCvc8EBVq9TDdSEfTNB/RV87tpnhGHBEaRPB4eIBHzs8w
AAAECNB5W3Yz2wBdR5eQHtxBNOeCR0MvV95rBRS3Kl5N2JoK9zwQFWr1MN1IR9M0H9FXzu
2meEYcERpE8Hh4gEfOzzAAAADmdpdGh1Yi1hY3Rpb25zAQIDBAUGBw==
-----END OPENSSH PRIVATE KEY-----

**Copie a chave privada completa** (incluindo `-----BEGIN` e `-----END`)
dckr_pat_8hhfXTZKfFsMcoi_0I5OW9MIjzk

### 9️⃣ Configurar Secrets no GitHub (5 min)

Vá em: https://github.com/aghiot321/To-do-List/settings/secrets/actions

Crie estes 6 secrets:

| Nome | Valor | Onde Obter |
|------|-------|------------|
| `DOCKER_USERNAME` | seu_usuario_dockerhub | https://hub.docker.com/ |
| `DOCKER_PASSWORD` | dckr_pat_xxxxx | Docker Hub > Account Settings > Security > New Access Token |
| `SERVER_HOST` | 34.XXX.XXX.XXX | IP externo da VM (console GCP) |
| `SERVER_USER` | seu_usuario | Execute `whoami` na VM |
| `SERVER_PORT` | 22 | Porta SSH padrão |
| `SSH_PRIVATE_KEY` | -----BEGIN OPENSSH... | Chave privada do passo 8 |

---

### 🔟 Testar Deploy Manual (Opcional - 3 min)

Antes de testar o pipeline, faça um deploy manual:

```bash
# Na VM, no diretório ~/todolist
cd ~/todolist

# Fazer login no Docker Hub
docker login -u seu_usuario_dockerhub

# Subir os containers
docker-compose -f docker-compose.prod.yml up -d

# Ver logs
docker-compose -f docker-compose.prod.yml logs -f

# Testar (em outro terminal)
curl http://localhost:8080/actuator/health
```

Se retornar `{"status":"UP"}`, está funcionando! 🎉

---

### 1️⃣1️⃣ Testar Pipeline de CI/CD (3 min)

No seu computador:

```bash
# Fazer uma alteração
echo "# Deploy configurado no GCP!" >> README.md

# Commit e push
git add .
git commit -m "test: testar pipeline com GCP"
git push origin main

# Acompanhar em:
# https://github.com/aghiot321/To-do-List/actions
```

Aguarde 3-5 minutos. O pipeline deve:
1. ✅ Executar testes
2. ✅ Fazer build da imagem Docker
3. ✅ Fazer deploy na VM do GCP

---

## ✅ Verificação Final

### No Navegador
Acesse (substitua pelo seu IP):
```
http://34.XXX.XXX.XXX:8080/actuator/health
```

Deve retornar: `{"status":"UP"}`

### Na VM
```bash
# Ver containers rodando
docker ps

# Ver logs
docker-compose -f docker-compose.prod.yml logs -f todolist-app

# Ver uso de recursos
docker stats
```

---

## 💰 Garantir que Permanece Gratuito

**Checklist**:
- ✅ Região: us-west1, us-central1 ou us-east1
- ✅ Tipo de máquina: e2-micro
- ✅ Disco: 30GB ou menos
- ✅ IP: Ephemeral (ou aceite pagar $3/mês por IP estático)

**Monitorar custos**:
1. Menu: **Faturamento** > **Relatórios**
2. Verifique se está $0.00
3. Configure alertas de faturamento

**Configurar alerta**:
1. **Faturamento** > **Orçamentos e alertas**
2. Criar orçamento: $5/mês
3. Receba email se ultrapassar $1

---

## 🔧 Comandos Úteis do GCP

```bash
# Iniciar VM
gcloud compute instances start todolist-server --zone=us-west1-b

# Parar VM (economiza na conta trial, mas free tier já é grátis)
gcloud compute instances stop todolist-server --zone=us-west1-b

# SSH via gcloud CLI
gcloud compute ssh todolist-server --zone=us-west1-b

# Ver informações da VM
gcloud compute instances describe todolist-server --zone=us-west1-b
```

---

## 🐛 Troubleshooting

### Erro: "Quota exceeded"
- Você provavelmente criou a VM fora da região gratuita
- Delete a VM e crie novamente em us-west1, us-central1 ou us-east1

### Erro: "Cannot connect via SSH"
- Verifique se adicionou a tag `todolist-server` na VM
- Verifique se a chave SSH está correta
- Tente SSH pelo console do GCP primeiro

### Aplicação não responde
```bash
# Ver logs
docker-compose -f docker-compose.prod.yml logs

# Reiniciar containers
docker-compose -f docker-compose.prod.yml restart

# Verificar portas
sudo netstat -tulpn | grep :8080
```

### Estou sendo cobrado
- Verifique se a VM é e2-micro
- Verifique se está em região gratuita
- Verifique se o disco não ultrapassou 30GB
- IP estático custa $3/mês (use ephemeral se quiser grátis)

---

## 🎓 Para Entregar na Atividade

**URL do Repositório**: https://github.com/aghiot321/To-do-List

**URL da Aplicação**: http://SEU_IP:8080/actuator/health

**Informações adicionais**:
- Plataforma: Google Cloud Platform (GCP)
- VM: e2-micro (1GB RAM, 1 vCPU)
- Região: us-west1 (Oregon)
- Sistema: Ubuntu 22.04 LTS
- Custo: **R$ 0,00** (Free Tier permanente)

---

## 📚 Recursos Adicionais

- **Documentação GCP**: https://cloud.google.com/compute/docs
- **Free Tier**: https://cloud.google.com/free/docs/gcp-free-tier
- **Calculadora de preços**: https://cloud.google.com/products/calculator
- **Console GCP**: https://console.cloud.google.com/

---

## 🎁 Bônus: Configurar IP Estático (Opcional - Custa $3/mês)

Se quiser que o IP não mude quando reiniciar a VM:

1. **VPC Network** > **Endereços IP**
2. Clique em "Reservar endereço estático"
3. Nome: `todolist-ip`
4. Região: `us-west1`
5. Anexado a: `todolist-server`
6. Clique em "Reservar"

**⚠️ Atenção**: IP estático custa ~$3/mês. No free tier, use ephemeral (gratuito).

---

**✅ Pronto! Sua aplicação está rodando no Google Cloud gratuitamente!**

Qualquer dúvida, consulte:
- `README.md` - Documentação geral
- `SECRETS_SETUP.md` - Configuração de secrets
- `COMANDOS_UTEIS.md` - Comandos úteis
