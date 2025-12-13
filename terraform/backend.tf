# Backend remoto para armazenar o state do Terraform
# 
# Opções:
# 1. Google Cloud Storage (GCS) - Recomendado para GCP
# 2. Terraform Cloud - Multiplataforma
# 3. AWS S3 - Se usar AWS
#
# INSTRUÇÕES:
# 1. Crie um bucket no GCS antes de usar este backend
# 2. Descomente o bloco correspondente
# 3. Execute: terraform init -migrate-state

# ========================================
# Opção 1: Google Cloud Storage (GCS)
# ========================================
# Para criar o bucket, execute:
# gsutil mb -p SEU_PROJECT_ID -c STANDARD -l us-west1 gs://todolist-terraform-state
#
# Ou via console: https://console.cloud.google.com/storage

terraform {
  backend "gcs" {
    bucket = "todolist-terraform-state"  # Nome do bucket (deve ser único globalmente)
    prefix = "terraform/state"           # Caminho dentro do bucket
    
    # Credenciais são lidas da variável de ambiente GOOGLE_APPLICATION_CREDENTIALS
    # ou do arquivo JSON especificado
    # credentials = "path/to/service-account-key.json"
  }
}

# ========================================
# Opção 2: Terraform Cloud (Alternativa)
# ========================================
# Crie uma conta em: https://app.terraform.io/
# Crie uma organização e workspace
# Gere um token de API
#
# terraform {
#   cloud {
#     organization = "sua-organizacao"
#     
#     workspaces {
#       name = "todolist-prod"
#     }
#   }
# }

# ========================================
# Opção 3: AWS S3 + DynamoDB (Se usar AWS)
# ========================================
# terraform {
#   backend "s3" {
#     bucket         = "todolist-terraform-state"
#     key            = "terraform/state/terraform.tfstate"
#     region         = "us-east-1"
#     encrypt        = true
#     dynamodb_table = "terraform-lock"
#   }
# }
