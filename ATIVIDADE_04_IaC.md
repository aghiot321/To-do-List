# 📚 Atividade 04: Infraestrutura como Código (IaC) e Pipeline Completo

## 🎯 Objetivo

Integrar o Terraform ao fluxo de trabalho CI/CD, automatizando não apenas o deploy da aplicação, mas também o provisionamento e gerenciamento da infraestrutura que a sustenta.

## 📋 Sumário

- [Pré-requisitos](#pré-requisitos)
- [Arquitetura da Solução](#arquitetura-da-solução)
- [Configuração Inicial](#configuração-inicial)
- [Secrets do GitHub](#secrets-do-github)
- [Pipeline CI/CD com Terraform](#pipeline-cicd-com-terraform)
- [Teste Local do Terraform](#teste-local-do-terraform)
- [Monitoramento e Troubleshooting](#monitoramento-e-troubleshooting)
- [Custos](#custos)

---

## ✅ Pré-requisitos

### Ferramentas

- ✅ Terraform instalado (v1.0+): https://www.terraform.io/downloads
- ✅ gcloud CLI instalado: https://cloud.google.com/sdk/docs/install
- ✅ Git instalado
- ✅ Conta no Google Cloud Platform
- ✅ Conta no Docker Hub

### Conhecimentos

- Conclusão da Atividade 03 (Pipeline CI/CD básico)
- Conceitos básicos de Terraform
- Comandos básicos de Docker e Docker Compose

---

## 🏗️ Arquitetura da Solução

### Fluxo Completo do Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│                    PUSH PARA MAIN BRANCH                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│  JOB 1: TESTES (CI)                                             │
│  • Checkout do código                                           │
│  • Setup Java 21                                                │
│  • Executar testes unitários (mvn test)                         │
│  • Gerar relatório de cobertura (JaCoCo)                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼ (se testes passarem)
┌─────────────────────────────────────────────────────────────────┐
│  JOB 2: PROVISIONAR INFRAESTRUTURA (IaC)                        │
│  • Setup Terraform                                              │
│  • Configurar credenciais GCP                                   │
│  • terraform init (com backend remoto GCS)                      │
│  • terraform plan                                               │
│  • terraform apply -auto-approve                                │
│  • Capturar IP público da VM (output)                           │
│  • Aguardar VM estar acessível                                  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼ (em paralelo com infra)
┌─────────────────────────────────────────────────────────────────┐
│  JOB 3: BUILD E PUSH (CD)                                       │
│  • Setup Docker Buildx                                          │
│  • Login no Docker Hub                                          │
│  • Build da imagem Docker                                       │
│  • Push para Docker Hub                                         │
│  • Tag: main-<SHA> e latest                                     │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼ (aguarda infra + build)
┌─────────────────────────────────────────────────────────────────┐
│  JOB 4: DEPLOY (CD)                                             │
│  • Conectar via SSH (usando IP dinâmico)                        │
│  • Aguardar Docker estar instalado (cloud-init)                 │
│  • Clonar/atualizar repositório                                 │
│  • Pull da nova imagem Docker                                   │
│  • docker-compose up -d                                         │
│  • Health check da aplicação                                    │
│  • Limpeza de imagens antigas                                   │
└─────────────────────────────────────────────────────────────────┘
```

### Recursos Terraform Criados

| Recurso | Tipo | Descrição |
|---------|------|-----------|
| **VM** | `google_compute_instance` | Compute Engine e2-micro (Free Tier) |
| **Firewall App** | `google_compute_firewall` | Porta 8080 aberta para 0.0.0.0/0 |
| **Firewall MySQL** | `google_compute_firewall` | Porta 3309 aberta (opcional) |
| **Cloud-Init** | `metadata_startup_script` | Instala Docker, Docker Compose, Git |
| **SSH Key** | `metadata.ssh-keys` | Chave pública injetada na VM |

---

## 🛠️ Configuração Inicial

### 1. Criar Service Account no GCP

A Service Account é necessária para o Terraform autenticar no GCP.

```bash
# 1. Criar Service Account
gcloud iam service-accounts create terraform-sa \
  --display-name="Terraform Service Account" \
  --description="Service Account para Terraform provisionar recursos"

# 2. Conceder permissões (Compute Admin + Storage Admin)
gcloud projects add-iam-policy-binding SEU_PROJECT_ID \
  --member="serviceAccount:terraform-sa@SEU_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/compute.admin"

gcloud projects add-iam-policy-binding SEU_PROJECT_ID \
  --member="serviceAccount:terraform-sa@SEU_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

# 3. Criar chave JSON
gcloud iam service-accounts keys create ~/terraform-sa-key.json \
  --iam-account=terraform-sa@SEU_PROJECT_ID.iam.gserviceaccount.com
```

**Ou via Console**:
1. Acesse: https://console.cloud.google.com/iam-admin/serviceaccounts
2. Clique em "Criar conta de serviço"
3. Nome: `terraform-sa`
4. Adicione papéis: `Compute Admin` e `Storage Admin`
5. Clique na conta > "Chaves" > "Adicionar chave" > "JSON"

### 2. Criar Bucket para State Remoto

O state do Terraform precisa ser armazenado na nuvem para o GitHub Actions acessá-lo.

```bash
# Via gcloud CLI
gcloud storage buckets create gs://todolist-terraform-state \
  --project=SEU_PROJECT_ID \
  --location=us-west1 \
  --uniform-bucket-level-access

# Habilitar versionamento (recomendado)
gcloud storage buckets update gs://todolist-terraform-state \
  --versioning
```

**Ou via Console**:
1. Acesse: https://console.cloud.google.com/storage
2. Clique em "Criar bucket"
3. Nome: `todolist-terraform-state` (deve ser único globalmente)
4. Região: `us-west1`
5. Controle de acesso: Uniforme
6. Clique em "Criar"

### 3. Gerar Chaves SSH (se não tiver)

```bash
# Gerar par de chaves SSH
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/todolist_deploy

# Ver chave pública
cat ~/.ssh/todolist_deploy.pub

# Ver chave privada (CUIDADO: é secreta!)
cat ~/.ssh/todolist_deploy
```

---

## 🔐 Secrets do GitHub

Adicione os seguintes secrets em:  
`https://github.com/SEU_USUARIO/To-do-List/settings/secrets/actions`

### Secrets Necessários

| Secret | Descrição | Como Obter | Exemplo |
|--------|-----------|------------|---------|
| `GCP_PROJECT_ID` | ID do projeto no GCP | Console GCP > Dashboard | `todolist-prod-123456` |
| `GCP_SA_KEY` | Service Account JSON completo | JSON baixado no passo 1 | `{"type":"service_account",...}` |
| `GCP_TERRAFORM_BUCKET` | Nome do bucket do state | Criado no passo 2 | `todolist-terraform-state` |
| `SSH_USER` | Usuário SSH da VM | Seu username do GCP | `triguinhogemeos` |
| `SSH_PUBLIC_KEY` | Chave SSH pública | `cat ~/.ssh/todolist_deploy.pub` | `ssh-ed25519 AAAA...` |
| `SSH_PRIVATE_KEY` | Chave SSH privada | `cat ~/.ssh/todolist_deploy` | `-----BEGIN OPENSSH...` |
| `DOCKER_USERNAME` | Usuário do Docker Hub | https://hub.docker.com | `aghiot` |
| `DOCKER_PASSWORD` | Token de acesso do Docker Hub | Docker Hub > Settings > Security | `dckr_pat_xxx...` |

### Como Adicionar Secrets

1. Vá em: https://github.com/aghiot321/To-do-List/settings/secrets/actions
2. Clique em "New repository secret"
3. Nome: `GCP_PROJECT_ID`
4. Valor: `seu-projeto-id`
5. Clique em "Add secret"
6. Repita para cada secret

**⚠️ IMPORTANTE**: 
- O `GCP_SA_KEY` deve conter o JSON inteiro (incluindo `{` e `}`)
- O `SSH_PRIVATE_KEY` deve incluir `-----BEGIN` e `-----END`
- NUNCA faça commit desses valores no código!

---

## 🚀 Pipeline CI/CD com Terraform

### Estrutura do Workflow

O arquivo `.github/workflows/cicd-terraform.yml` contém 4 jobs:

#### Job 1: Test (CI)
```yaml
test:
  - Executa testes unitários
  - Gera relatório de cobertura (JaCoCo)
  - Upload do relatório como artifact
```

#### Job 2: Provision Infra (IaC)
```yaml
provision-infra:
  needs: test  # Só roda se testes passarem
  - Setup Terraform
  - Autentica no GCP
  - terraform init (com backend GCS)
  - terraform plan
  - terraform apply -auto-approve
  - Captura IP público da VM (output)
  - Aguarda VM estar acessível via SSH
```

#### Job 3: Build and Push (CD)
```yaml
build-and-push:
  needs: provision-infra  # Aguarda infra pronta
  - Build da imagem Docker
  - Push para Docker Hub
  - Tags: main-<SHA> e latest
```

#### Job 4: Deploy (CD)
```yaml
deploy:
  needs: [provision-infra, build-and-push]
  - Conecta via SSH usando IP dinâmico
  - Aguarda Docker estar instalado (cloud-init)
  - Clona/atualiza repositório
  - docker-compose up -d
  - Health check
  - Limpeza de imagens antigas
```

### Fluxo de Execução

1. **Developer faz push para `main`**
2. **GitHub Actions inicia workflow**
3. **Testes executam** (3-5 min)
4. **Terraform provisiona VM** (2-3 min)
5. **Docker build e push** (3-5 min) - em paralelo
6. **Deploy na VM criada** (2-3 min)
7. **Total**: ~10-15 minutos

---

## 🧪 Teste Local do Terraform

Antes de usar no pipeline, teste localmente:

### 1. Configurar Ambiente Local

```bash
# Navegar para pasta terraform
cd terraform

# Copiar arquivo de exemplo
cp terraform.tfvars.example terraform.tfvars

# Editar com seus valores
nano terraform.tfvars
```

**Preencha `terraform.tfvars`**:
```hcl
gcp_project_id = "todolist-prod-123456"
gcp_region     = "us-west1"
gcp_zone       = "us-west1-b"
instance_name  = "todolist-server"
machine_type   = "e2-micro"
boot_disk_size = 30
boot_disk_type = "pd-standard"
ssh_user       = "triguinhogemeos"
ssh_public_key = "ssh-ed25519 AAAA... seu_email@gmail.com"
environment    = "prod"
```

### 2. Configurar Credenciais

```bash
# Apontar para o JSON da Service Account
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/terraform-sa-key.json"
```

### 3. Executar Terraform

```bash
# Inicializar
terraform init

# Validar sintaxe
terraform validate

# Ver plano de execução
terraform plan

# Aplicar mudanças
terraform apply
# Digite: yes

# Ver outputs
terraform output

# Ver IP público
terraform output public_ip
```

### 4. Testar SSH

```bash
# Obter IP
SERVER_IP=$(terraform output -raw public_ip)

# Conectar
ssh triguinhogemeos@$SERVER_IP

# Verificar Docker instalado
docker --version
docker-compose --version
```

### 5. Destruir (quando não precisar mais)

```bash
# Remove todos os recursos
terraform destroy
# Digite: yes
```

---

## 📊 Monitoramento e Troubleshooting

### Ver Execução do Pipeline

1. Acesse: https://github.com/aghiot321/To-do-List/actions
2. Clique na última execução
3. Veja cada job (Test, Provision, Build, Deploy)

### Logs Úteis

```bash
# Logs da VM (startup script)
gcloud compute instances get-serial-port-output todolist-server --zone=us-west1-b

# SSH na VM
ssh triguinhogemeos@<IP_DA_VM>

# Logs da aplicação
cd ~/todolist
docker-compose -f docker-compose.prod.yml logs -f todolist-app

# Status dos containers
docker-compose -f docker-compose.prod.yml ps

# Ver imagens baixadas
docker images | grep todolist
```

### Problemas Comuns

#### 1. "Error authenticating: Could not find default credentials"
- **Causa**: Service Account não configurada
- **Solução**: Adicione `GCP_SA_KEY` nos Secrets do GitHub

#### 2. "Error creating instance: Quota exceeded"
- **Causa**: Região errada ou tipo de máquina não gratuito
- **Solução**: Use `us-west1` + `e2-micro`

#### 3. "Error loading state: Bucket does not exist"
- **Causa**: Bucket do state não foi criado
- **Solução**: Crie o bucket: `gcloud storage buckets create gs://todolist-terraform-state`

#### 4. "Permission denied (publickey)"
- **Causa**: Chave SSH incorreta ou não injetada
- **Solução**: Verifique `SSH_PUBLIC_KEY` e `SSH_PRIVATE_KEY` nos Secrets

#### 5. "Docker not found" durante deploy
- **Causa**: Cloud-init ainda não terminou
- **Solução**: O script aguarda até 10min. Se falhar, aumente o timeout ou verifique logs

#### 6. Deploy falha em "Health check"
- **Causa**: Aplicação demorou muito para iniciar
- **Solução**: Verifique logs da aplicação na VM

### Comandos de Debug

```bash
# Ver state do Terraform
terraform show

# Atualizar state com realidade
terraform refresh

# Forçar recriar VM
terraform taint google_compute_instance.todolist_server
terraform apply

# Ver outputs novamente
terraform output
```

---

## 💰 Custos

### ✅ Recursos Gratuitos (Free Tier)

| Recurso | Limite Gratuito | Custo se Ultrapassar |
|---------|-----------------|----------------------|
| VM e2-micro | 1 instância | ~$7/mês por instância adicional |
| Disco SSD | 30 GB | ~$0.17/GB/mês |
| Tráfego de saída | 1 GB/mês (América do Norte) | ~$0.12/GB |
| IP Ephemeral | Ilimitado enquanto VM está rodando | Grátis |
| IP Estático | - | ~$3/mês (se reservar) |
| GCS (State) | 5 GB | ~$0.02/GB/mês |

### ⚠️ Garantir Free Tier

- ✅ Região: `us-west1`, `us-central1` ou `us-east1`
- ✅ Tipo de máquina: `e2-micro`
- ✅ Disco: Máximo 30 GB
- ✅ IP: Ephemeral (não reservar estático)

### Monitorar Custos

1. Acesse: https://console.cloud.google.com/billing/reports
2. Configure alerta de faturamento:
   - Menu: **Faturamento** > **Orçamentos e alertas**
   - Criar orçamento: $5/mês
   - Alerta em: $1 (20%)

---

## 📚 Recursos Adicionais

- **Terraform GCP Provider**: https://registry.terraform.io/providers/hashicorp/google/latest/docs
- **GCP Free Tier**: https://cloud.google.com/free/docs/gcp-free-tier
- **Terraform Backend GCS**: https://www.terraform.io/language/settings/backends/gcs
- **GitHub Actions**: https://docs.github.com/en/actions
- **Docker Compose**: https://docs.docker.com/compose/

---

## 🎓 Entrega da Atividade

### Checklist

- [ ] Pasta `terraform/` criada com todos os arquivos
- [ ] Backend remoto (GCS) configurado
- [ ] Secrets do GitHub configurados
- [ ] Workflow `.github/workflows/cicd-terraform.yml` criado
- [ ] Pipeline executado com sucesso (todos os jobs verdes)
- [ ] Aplicação acessível via `http://<IP>:8080/actuator/health`
- [ ] README.md atualizado com documentação

### Entregáveis

1. **Link do repositório**: https://github.com/aghiot321/To-do-List
2. **Link do workflow**: https://github.com/aghiot321/To-do-List/actions
3. **Screenshot do pipeline**: Todos os jobs verdes (✅)
4. **URL da aplicação**: `http://<IP_DA_VM>:8080/actuator/health`
5. **Documentação**: README.md atualizado

### Exemplo de Resposta

```
Repositório: https://github.com/aghiot321/To-do-List
Pipeline: https://github.com/aghiot321/To-do-List/actions/runs/12345678
Aplicação: http://35.212.146.198:8080/actuator/health

Plataforma: Google Cloud Platform (GCP)
VM: e2-micro (1 vCPU, 1GB RAM)
Região: us-west1-b (Oregon)
Custo: R$ 0,00 (Free Tier permanente)

IaC: Terraform v1.6.0
Backend: Google Cloud Storage (GCS)
Pipeline: GitHub Actions
```

---

## ✅ Conclusão

Você agora tem:
- ✅ Infraestrutura versionada em código (IaC)
- ✅ Pipeline totalmente automatizado (CI/CD + IaC)
- ✅ Provisionamento automático de servidores
- ✅ State remoto para trabalho em equipe
- ✅ Deploy com IP dinâmico (infraestrutura efêmera)
- ✅ Custo zero (Free Tier do GCP)

**Próximos passos**:
- Adicionar múltiplos ambientes (dev, staging, prod)
- Implementar módulos Terraform reutilizáveis
- Adicionar testes de infraestrutura (Terratest)
- Configurar monitoramento (Prometheus + Grafana)
