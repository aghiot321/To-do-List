# 🚀 Quick Start - Atividade 04 (Terraform + CI/CD)

Guia rápido para configurar o pipeline completo com Terraform.

## ⏱️ Tempo estimado: 30 minutos

---

## 📦 Pré-requisitos (Instalações)

Antes de começar, instale as ferramentas necessárias:

### 🔧 Terraform

**O que é**: Ferramenta de Infraestrutura como Código (IaC)

**Instalação**:

**Windows (PowerShell)**:

**⚠️ IMPORTANTE**: Para Opção 1 e 3, você precisa executar o PowerShell como **Administrador**:
- Pesquise "PowerShell" no menu Iniciar
- Clique com botão direito em "Windows PowerShell"
- Selecione "Executar como administrador"

```powershell
# Opção 1: Via Chocolatey (se tiver instalado)
# ⚠️ REQUER ADMIN: Feche o PowerShell e abra como Administrador
# Instalar Chocolatey primeiro (se não tiver):
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Depois instale o Terraform:
choco install terraform -y

# Opção 2: Download manual (RECOMENDADO - NÃO precisa de Admin)
# 1. Baixe de: https://developer.hashicorp.com/terraform/install#windows
# 2. Clique em "AMD64" para baixar o ZIP
# 3. Extraia o arquivo ZIP
# 4. Crie a pasta: C:\terraform\
# 5. Mova terraform.exe para C:\terraform\
# 6. Adicione C:\terraform à variável PATH:
#    - Pressione Win + R, digite: sysdm.cpl e pressione Enter
#    - Aba "Avançado" > "Variáveis de Ambiente"
#    - Em "Variáveis do usuário", selecione "Path" > "Editar"
#    - Clique em "Novo" e adicione: C:\terraform
#    - Clique em "OK" em todas as janelas
# 7. Feche e reabra o PowerShell (normal, sem admin)

# Opção 3: Via winget (Windows 10+)
# ⚠️ REQUER ADMIN: Feche o PowerShell e abra como Administrador
winget install Hashicorp.Terraform

# Depois de instalar, feche e reabra o PowerShell (pode ser normal)
```

**Linux/Mac**:
```bash
# Linux (Ubuntu/Debian)
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform

# Mac (Homebrew)
brew tap hashicorp/tap
brew install hashicorp/tap/terraform
```

**Verificar instalação**:
```bash
terraform --version
# Deve retornar: Terraform v1.6.0 ou superior
```

📖 **Guia oficial**: https://developer.hashicorp.com/terraform/install

---

### ☁️ Google Cloud CLI (gcloud)

**O que é**: Ferramenta de linha de comando para interagir com o Google Cloud

**Instalação**:

**Windows**:
```powershell
# Download do instalador
# Acesse: https://cloud.google.com/sdk/docs/install#windows
# Execute o instalador GoogleCloudSDKInstaller.exe
# Siga o assistente de instalação
```

**Linux**:
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install apt-transport-https ca-certificates gnupg curl
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
sudo apt-get update && sudo apt-get install google-cloud-cli
```

**Mac**:
```bash
# Via Homebrew
brew install --cask google-cloud-sdk
```

**Configurar após instalação**:
```bash
# Fazer login
gcloud auth login

# Configurar projeto padrão
gcloud config set project SEU_PROJECT_ID

# Verificar instalação
gcloud --version
```

📖 **Guia oficial**: https://cloud.google.com/sdk/docs/install

---

### 🔑 Git

**O que é**: Sistema de controle de versão

**Instalação**:

**Windows**:
```powershell
# Opção 1: Via Chocolatey
choco install git

# Opção 2: Download manual
# https://git-scm.com/download/win
```

**Linux**:
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install git

# Fedora/RHEL
sudo dnf install git
```

**Mac**:
```bash
# Via Homebrew
brew install git

# Ou via Xcode Command Line Tools
xcode-select --install
```

**Verificar instalação**:
```bash
git --version
```

📖 **Guia oficial**: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git

---

### 🐳 Docker (Opcional - para testes locais)

**O que é**: Plataforma de containers

**Instalação**:

**Windows**:
- Docker Desktop: https://docs.docker.com/desktop/install/windows-install/

**Linux**:
```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
```

**Mac**:
- Docker Desktop: https://docs.docker.com/desktop/install/mac-install/

**Verificar instalação**:
```bash
docker --version
docker-compose --version
```

📖 **Guia oficial**: https://docs.docker.com/get-docker/

---

## ✅ Verificar Todas as Instalações

Execute este comando para verificar se tudo está instalado:

```bash
echo "=== Verificando instalações ==="
terraform --version
gcloud --version
git --version
docker --version
echo "=== Tudo pronto! ==="
```

---

## 📝 Passo a Passo

### 1. Criar Service Account no GCP (5 min)

**💻 Onde executar**: Terminal local (PowerShell/Bash) com `gcloud CLI` instalado

**PowerShell (Windows)**:
```powershell
# 1. Criar Service Account
gcloud iam service-accounts create terraform-sa --display-name="Terraform Service Account"

# 2. Conceder permissões
$PROJECT_ID = "seu-projeto-id"  # Substitua pelo seu ID do projeto GCP

gcloud projects add-iam-policy-binding $PROJECT_ID --member="serviceAccount:terraform-sa@$PROJECT_ID.iam.gserviceaccount.com" --role="roles/compute.admin"

gcloud projects add-iam-policy-binding $PROJECT_ID --member="serviceAccount:terraform-sa@$PROJECT_ID.iam.gserviceaccount.com" --role="roles/storage.admin"

# 3. Criar chave JSON (será salva na pasta do usuário)
gcloud iam service-accounts keys create $HOME\terraform-sa-key.json --iam-account=terraform-sa@$PROJECT_ID.iam.gserviceaccount.com

# 4. Ver conteúdo (copiar para GitHub Secrets)
Get-Content $HOME\terraform-sa-key.json
```

**Bash/Linux/Mac**:
```bash
# 1. Criar Service Account
gcloud iam service-accounts create terraform-sa \
  --display-name="Terraform Service Account"

# 2. Conceder permissões
PROJECT_ID="seu-projeto-id"  # Substitua pelo seu ID

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:terraform-sa@$PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/compute.admin"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:terraform-sa@$PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

# 3. Criar chave JSON
gcloud iam service-accounts keys create ~/terraform-sa-key.json \
  --iam-account=terraform-sa@$PROJECT_ID.iam.gserviceaccount.com

# 4. Ver conteúdo (copiar para GitHub Secrets)
cat ~/terraform-sa-key.json
```

**Alternativa via Console**:
1. https://console.cloud.google.com/iam-admin/serviceaccounts
2. "Criar conta de serviço" > Nome: `terraform-sa`
3. Adicionar papéis: `Compute Admin` + `Storage Admin`
4. "Chaves" > "Adicionar chave" > "JSON"

---

### 2. Criar Bucket para State (3 min)

**💻 Onde executar**: Terminal local (PowerShell/Bash) com `gcloud CLI` instalado

**PowerShell (Windows)**:
```powershell
$PROJECT_ID = "seu-projeto-id"  # Substitua pelo mesmo ID usado antes

gcloud storage buckets create gs://todolist-terraform-state --project=$PROJECT_ID --location=us-west1 --uniform-bucket-level-access

# Habilitar versionamento
gcloud storage buckets update gs://todolist-terraform-state --versioning
```

**Bash/Linux/Mac**:
```bash
PROJECT_ID="seu-projeto-id"  # Substitua

gcloud storage buckets create gs://todolist-terraform-state \
  --project=$PROJECT_ID \
  --location=us-west1 \
  --uniform-bucket-level-access

# Habilitar versionamento
gcloud storage buckets update gs://todolist-terraform-state \
  --versioning
```

**Alternativa via Console**:
1. https://console.cloud.google.com/storage
2. "Criar bucket"
3. Nome: `todolist-terraform-state` (único globalmente)
4. Região: `us-west1`

---

### 3. Gerar Chaves SSH (2 min)

**💻 Onde executar**: Terminal local (PowerShell, Bash, ou Git Bash no Windows)

```bash
# Gerar par de chaves
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/todolist_deploy
# Pressione Enter 3 vezes (sem passphrase)

# Ver chave PÚBLICA (para GitHub Secret: SSH_PUBLIC_KEY)
cat ~/.ssh/todolist_deploy.pub

# Ver chave PRIVADA (para GitHub Secret: SSH_PRIVATE_KEY)
cat ~/.ssh/todolist_deploy
```

**⚠️ IMPORTANTE**: Copie TODO o conteúdo, incluindo:
- Chave pública: `ssh-ed25519 AAAA...`
- Chave privada: `-----BEGIN OPENSSH PRIVATE KEY-----` até `-----END OPENSSH PRIVATE KEY-----`

---

### 4. Configurar Secrets no GitHub (10 min)

**💻 Onde fazer**: Navegador (GitHub.com)

Acesse: `https://github.com/SEU_USUARIO/To-do-List/settings/secrets/actions`

Clique em "New repository secret" e adicione:

#### 4.1. Secrets do GCP

| Nome | Valor | Onde Obter |
|------|-------|------------|
| `GCP_PROJECT_ID` | `sonic-name-481014-r1` | Console GCP > Dashboard (ID do projeto) |
| `GCP_SA_KEY` | `{"type":"service_account",...}` | Conteúdo completo do `terraform-sa-key.json` |
| `GCP_TERRAFORM_BUCKET` | `todolist-terraform-state` | Nome do bucket criado |

#### 4.2. Secrets do SSH

| Nome | Valor | Onde Obter |
|------|-------|------------|
| `SSH_USER` | `triguinhogemeos` | Seu username (execute `whoami`) |
| `SSH_PUBLIC_KEY` | `ssh-ed25519 AAAA...` | Conteúdo de `~/.ssh/todolist_deploy.pub` |
| `SSH_PRIVATE_KEY` | `-----BEGIN...` | Conteúdo COMPLETO de `~/.ssh/todolist_deploy` |

#### 4.3. Secrets do Docker Hub

| Nome | Valor | Onde Obter |
|------|-------|------------|
| `DOCKER_USERNAME` | `aghiot` | Seu username no Docker Hub |
| `DOCKER_PASSWORD` | `dckr_pat_xxxxx` | Token gerado em Settings > Security |

**Total: 8 secrets**

---

### 5. Testar Terraform Localmente (Opcional - 5 min)

**💻 Onde executar**: Terminal local na pasta do projeto

```powershell
# Navegar para pasta terraform
cd terraform

# Copiar arquivo de exemplo (PowerShell)
Copy-Item terraform.tfvars.example terraform.tfvars

# Editar com seus valores (Windows)
notepad terraform.tfvars
# Ou no VS Code:
# code terraform.tfvars
```

**Preencher**:
```hcl
gcp_project_id = "sonic-name-481014-r1"  # Seu projeto
ssh_user       = "seu_usuario"           # Seu usuário (whoami)
ssh_public_key = "ssh-ed25519 AAAA..."   # Sua chave pública
```

**Executar**:

**PowerShell (Windows)**:
```powershell
# Configurar credenciais
$env:GOOGLE_APPLICATION_CREDENTIALS="$HOME\terraform-sa-key.json"

# Inicializar
terraform init

# Ver plano
terraform plan

# Aplicar (cria a VM)
terraform apply
# Digite: yes

# Ver IP
terraform output public_ip

# Destruir (quando não precisar mais)
terraform destroy
# Digite: yes
```

**Bash/Linux/Mac**:
```bash
# Configurar credenciais
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/terraform-sa-key.json"

# Inicializar
terraform init

# Ver plano
terraform plan

# Aplicar (cria a VM)
terraform apply
# Digite: yes

# Ver IP
terraform output public_ip

# Destruir (quando não precisar mais)
terraform destroy
# Digite: yes
```

---

### 6. Executar Pipeline (3 min)

**💻 Onde executar**: Terminal local na pasta do projeto

```bash
# Fazer uma alteração
echo "# Terraform configurado!" >> README.md

# Commit e push
git add .
git commit -m "feat: adicionar Terraform IaC"
git push origin main
```

**Acompanhar**:
1. https://github.com/SEU_USUARIO/To-do-List/actions
2. Aguardar 10-15 minutos
3. Verificar 4 jobs verdes:
   - ✅ Test
   - ✅ Provision Infra
   - ✅ Build and Push
   - ✅ Deploy

---

### 7. Verificar Deploy (2 min)

**💻 Onde verificar**: Navegador (GitHub Actions + navegador para testar app)

**No GitHub Actions**:
1. Expandir job "Provision Infra"
2. Copiar o IP público da VM (no output)

**No navegador**:
```
http://<IP_DA_VM>:8080/actuator/health
```

Deve retornar:
```json
{"status":"UP"}
```

**Via SSH** (opcional):
```bash
ssh -i ~/.ssh/todolist_deploy triguinhogemeos@<IP_DA_VM>

# Ver containers
docker ps

# Ver logs
cd ~/todolist
docker-compose -f docker-compose.prod.yml logs -f
```

---

## ✅ Checklist Final

- [ ] Service Account criada e chave JSON baixada
- [ ] Bucket para state criado
- [ ] Chaves SSH geradas (pública + privada)
- [ ] 8 secrets configurados no GitHub
- [ ] Terraform testado localmente (opcional)
- [ ] Push para `main` realizado
- [ ] Pipeline executado com sucesso (4 jobs verdes)
- [ ] VM criada no GCP (visível em Compute Engine)
- [ ] Aplicação respondendo em `http://<IP>:8080/actuator/health`

---

## 🐛 Troubleshooting Rápido

### Erro: "Error authenticating"
- Verifique se `GCP_SA_KEY` tem o JSON completo (incluindo `{` e `}`)

### Erro: "Bucket does not exist"
- Verifique se o bucket `todolist-terraform-state` foi criado
- Verifique se `GCP_TERRAFORM_BUCKET` está correto

### Erro: "Permission denied (publickey)"
- Verifique se `SSH_PUBLIC_KEY` e `SSH_PRIVATE_KEY` estão corretos
- Chave privada deve incluir `-----BEGIN` e `-----END`

### Erro: "Docker not found"
- O cloud-init pode levar 5-10min para instalar Docker
- O pipeline aguarda até 10min automaticamente
- Verifique logs: `gcloud compute instances get-serial-port-output todolist-server`

### Pipeline falha em "Health check"
- Aplicação pode levar 1-2min para inicializar
- SSH na VM e veja logs: `docker-compose -f docker-compose.prod.yml logs -f`

---

## 📚 Próximos Passos

1. ✅ Pipeline funcionando? Configure monitoramento
2. ✅ Adicione mais ambientes (dev, staging)
3. ✅ Configure backups automáticos
4. ✅ Adicione alertas de custo no GCP

---

## 🎓 Entrega da Atividade

**Link do repositório**: https://github.com/SEU_USUARIO/To-do-List  
**Link do workflow**: https://github.com/SEU_USUARIO/To-do-List/actions  
**URL da aplicação**: http://<IP_DA_VM>:8080/actuator/health

**Informações**:
- Plataforma: Google Cloud Platform (GCP)
- IaC: Terraform v1.6.0
- Backend: Google Cloud Storage (GCS)
- VM: e2-micro (Free Tier)
- Custo: R$ 0,00/mês

---

**🎉 Pronto! Sua infraestrutura está sendo gerenciada como código!**

📖 Documentação completa: [ATIVIDADE_04_IaC.md](ATIVIDADE_04_IaC.md)
