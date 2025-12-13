# Configuracao do Terraform

Este diretorio contem a infraestrutura como codigo (IaC) para provisionar automaticamente
a VM no Google Cloud Platform (GCP) para hospedar a aplicacao ToDoList.

## Estrutura de Arquivos

```
terraform/
├── main.tf                    # Definicao dos recursos (VM, firewall, etc)
├── variables.tf               # Declaracao de variaveis
├── outputs.tf                 # Outputs (IP publico, URLs, etc)
├── backend.tf                 # Configuracao do backend remoto (state)
├── terraform.tfvars.example   # Exemplo de variaveis (copiar para terraform.tfvars)
└── README.md                  # Este arquivo
```

## Uso Local (Desenvolvimento)

### 1. Pre-requisitos

- Terraform instalado: https://www.terraform.io/downloads
- Conta no GCP com projeto criado
- Service Account com permissoes (Compute Admin)
- Chave SSH gerada

### 2. Configurar Credenciais do GCP

Criar Service Account:
1. Acesse: https://console.cloud.google.com/iam-admin/serviceaccounts
2. Clique em "Criar conta de servico"
3. Nome: `terraform-sa`
4. Clique em "Criar e continuar"
5. Adicione o papel: `Compute Admin` e `Storage Admin`
6. Clique em "Concluir"
7. Clique na conta criada > "Chaves" > "Adicionar chave" > "JSON"
8. Salve o arquivo JSON em local seguro

Configurar variavel de ambiente:
```powershell
# Windows PowerShell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\service-account-key.json"

# Linux/Mac
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account-key.json"
```

### 3. Configurar Variaveis

```bash
# Copiar arquivo de exemplo
cp terraform.tfvars.example terraform.tfvars

# Editar com seus valores
nano terraform.tfvars
```

Preencha:
- `gcp_project_id`: ID do seu projeto no GCP
- `ssh_user`: Seu usuario (ex: triguinhogemeos)
- `ssh_public_key`: Conteudo da sua chave publica SSH (~/.ssh/id_ed25519.pub)

### 4. Criar Bucket para State Remoto (Opcional, mas recomendado)

```bash
# Via gcloud CLI
gcloud storage buckets create gs://todolist-terraform-state \
  --project=SEU_PROJECT_ID \
  --location=us-west1 \
  --uniform-bucket-level-access

# Ou via console: https://console.cloud.google.com/storage
```

Se NAO quiser usar backend remoto (apenas para testes locais):
- Comente o bloco `backend "gcs"` no arquivo `backend.tf`

### 5. Executar Terraform

```bash
# Entrar no diretorio
cd terraform

# Inicializar (baixa providers e configura backend)
terraform init

# Validar sintaxe
terraform validate

# Ver plano de execucao (o que sera criado)
terraform plan

# Aplicar mudancas (criar infraestrutura)
terraform apply

# Confirmar com: yes
```

### 6. Obter Informacoes da VM

```bash
# Ver outputs
terraform output

# Ver IP publico
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

## Uso no GitHub Actions (CI/CD)

O Terraform e executado automaticamente no pipeline de CI/CD antes do deploy.

### Secrets Necessarios no GitHub

Adicione em: `https://github.com/aghiot321/To-do-List/settings/secrets/actions`

| Secret | Descricao | Como Obter |
|--------|-----------|------------|
| `GCP_PROJECT_ID` | ID do projeto no GCP | Console GCP > Projeto |
| `GCP_SA_KEY` | Service Account JSON completo | JSON baixado no passo 2 |
| `GCP_TERRAFORM_BUCKET` | Nome do bucket para state | Ex: todolist-terraform-state |
| `SSH_USER` | Usuario SSH da VM | Seu username no GCP |
| `SSH_PUBLIC_KEY` | Chave SSH publica | Conteudo de ~/.ssh/id_ed25519.pub |
| `SSH_PRIVATE_KEY` | Chave SSH privada | Conteudo de ~/.ssh/id_ed25519 |

### Fluxo do Pipeline

1. Teste (CI): Roda testes da aplicacao
2. Provision Infra: Terraform cria/atualiza a VM
3. Build: Cria imagem Docker e publica
4. Deploy (CD): Deploy da aplicacao na VM provisionada

## Recursos Criados

- Compute Engine VM (e2-micro, free tier)
- Regras de firewall (portas 8080, 3309)
- Script de inicializacao (instala Docker, Docker Compose, Git)
- Configuracao SSH automatica

## Dicas

Manter no Free Tier:
- Regiao: us-west1, us-central1 ou us-east1
- Tipo de maquina: e2-micro
- Disco: 30GB ou menos
- IP: Ephemeral (gratuito)

Seguranca:
- NUNCA commite `terraform.tfvars` ou `*.tfstate*`
- Use backend remoto para evitar perda do state
- Proteja suas chaves SSH e service account

Troubleshooting:
```bash
# Ver logs da VM
gcloud compute instances get-serial-port-output todolist-server --zone=us-west1-b

# Forcar recriar VM
terraform taint google_compute_instance.todolist_server
terraform apply

# Ver state atual
terraform show

# Atualizar state com realidade da cloud
terraform refresh
```

## Recursos Uteis

- [Terraform GCP Provider](https://registry.terraform.io/providers/hashicorp/google/latest/docs)
- [GCP Free Tier](https://cloud.google.com/free)
- [Terraform Backend GCS](https://www.terraform.io/language/settings/backends/gcs)
