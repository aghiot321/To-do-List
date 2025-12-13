# ========================================
# README - Configuração do Terraform
# ========================================

Este diretório contém a infraestrutura como código (IaC) para provisionar automaticamente
a VM no Google Cloud Platform (GCP) para hospedar a aplicação ToDoList.

## 📁 Estrutura de Arquivos

```
terraform/
├── main.tf                    # Definição dos recursos (VM, firewall, etc)
├── variables.tf               # Declaração de variáveis
├── outputs.tf                 # Outputs (IP público, URLs, etc)
├── backend.tf                 # Configuração do backend remoto (state)
├── terraform.tfvars.example   # Exemplo de variáveis (copiar para terraform.tfvars)
└── README.md                  # Este arquivo
```

## 🚀 Uso Local (Desenvolvimento)

### 1. Pré-requisitos

- Terraform instalado: https://www.terraform.io/downloads
- Conta no GCP com projeto criado
- Service Account com permissões (Compute Admin)
- Chave SSH gerada

### 2. Configurar Credenciais do GCP

**Criar Service Account**:
1. Acesse: https://console.cloud.google.com/iam-admin/serviceaccounts
2. Clique em "Criar conta de serviço"
3. Nome: `terraform-sa`
4. Clique em "Criar e continuar"
5. Adicione o papel: `Compute Admin` e `Storage Admin`
6. Clique em "Concluir"
7. Clique na conta criada > "Chaves" > "Adicionar chave" > "JSON"
8. Salve o arquivo JSON em local seguro

**Configurar variável de ambiente**:
```powershell
# Windows PowerShell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\service-account-key.json"

# Linux/Mac
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account-key.json"
```

### 3. Configurar Variáveis

```bash
# Copiar arquivo de exemplo
cp terraform.tfvars.example terraform.tfvars

# Editar com seus valores
nano terraform.tfvars
```

**Preencha**:
- `gcp_project_id`: ID do seu projeto no GCP
- `ssh_user`: Seu usuário (ex: triguinhogemeos)
- `ssh_public_key`: Conteúdo da sua chave pública SSH (~/.ssh/id_ed25519.pub)

### 4. Criar Bucket para State Remoto (Opcional, mas recomendado)

```bash
# Via gcloud CLI
gcloud storage buckets create gs://todolist-terraform-state \
  --project=SEU_PROJECT_ID \
  --location=us-west1 \
  --uniform-bucket-level-access

# Ou via console: https://console.cloud.google.com/storage
```

**Se NÃO quiser usar backend remoto** (apenas para testes locais):
- Comente o bloco `backend "gcs"` no arquivo `backend.tf`

### 5. Executar Terraform

```bash
# Entrar no diretório
cd terraform

# Inicializar (baixa providers e configura backend)
terraform init

# Validar sintaxe
terraform validate

# Ver plano de execução (o que será criado)
terraform plan

# Aplicar mudanças (criar infraestrutura)
terraform apply

# Confirmar com: yes
```

### 6. Obter Informações da VM

```bash
# Ver outputs
terraform output

# Ver IP público
terraform output public_ip

# Ver comando SSH
terraform output ssh_connection
```

### 7. Destruir Infraestrutura (Cuidado!)

```bash
# Remove todos os recursos criados
terraform destroy

# Confirmar com: yes
```

## 🤖 Uso no GitHub Actions (CI/CD)

O Terraform é executado automaticamente no pipeline de CI/CD antes do deploy.

### Secrets Necessários no GitHub

Adicione em: `https://github.com/aghiot321/To-do-List/settings/secrets/actions`

| Secret | Descrição | Como Obter |
|--------|-----------|------------|
| `GCP_PROJECT_ID` | ID do projeto no GCP | Console GCP > Projeto |
| `GCP_SA_KEY` | Service Account JSON completo | JSON baixado no passo 2 |
| `GCP_TERRAFORM_BUCKET` | Nome do bucket para state | Ex: todolist-terraform-state |
| `SSH_USER` | Usuário SSH da VM | Seu username no GCP |
| `SSH_PUBLIC_KEY` | Chave SSH pública | Conteúdo de ~/.ssh/id_ed25519.pub |
| `SSH_PRIVATE_KEY` | Chave SSH privada | Conteúdo de ~/.ssh/id_ed25519 |

### Fluxo do Pipeline

1. **Teste (CI)**: Roda testes da aplicação
2. **Provision Infra**: Terraform cria/atualiza a VM
3. **Build**: Cria imagem Docker e publica
4. **Deploy (CD)**: Deploy da aplicação na VM provisionada

## 📝 Recursos Criados

- ✅ Compute Engine VM (e2-micro, free tier)
- ✅ Regras de firewall (portas 8080, 3309)
- ✅ Script de inicialização (instala Docker, Docker Compose, Git)
- ✅ Configuração SSH automática

## 💡 Dicas

**Manter no Free Tier**:
- Região: us-west1, us-central1 ou us-east1
- Tipo de máquina: e2-micro
- Disco: 30GB ou menos
- IP: Ephemeral (gratuito)

**Segurança**:
- NUNCA commite `terraform.tfvars` ou `*.tfstate*`
- Use backend remoto para evitar perda do state
- Proteja suas chaves SSH e service account

**Troubleshooting**:
```bash
# Ver logs da VM
gcloud compute instances get-serial-port-output todolist-server --zone=us-west1-b

# Forçar recriar VM
terraform taint google_compute_instance.todolist_server
terraform apply

# Ver state atual
terraform show

# Atualizar state com realidade da cloud
terraform refresh
```

## 🔗 Recursos Úteis

- [Terraform GCP Provider](https://registry.terraform.io/providers/hashicorp/google/latest/docs)
- [GCP Free Tier](https://cloud.google.com/free)
- [Terraform Backend GCS](https://www.terraform.io/language/settings/backends/gcs)
