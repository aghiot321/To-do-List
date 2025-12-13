# Variáveis do Google Cloud Platform
variable "gcp_project_id" {
  description = "ID do projeto no Google Cloud"
  type        = string
}

variable "gcp_region" {
  description = "Região do GCP (ex: us-west1)"
  type        = string
  default     = "us-west1"
}

variable "gcp_zone" {
  description = "Zona do GCP (ex: us-west1-b)"
  type        = string
  default     = "us-west1-b"
}

# Variáveis da VM
variable "instance_name" {
  description = "Nome da instância de VM"
  type        = string
  default     = "todolist-server"
}

variable "machine_type" {
  description = "Tipo de máquina (e2-micro para free tier)"
  type        = string
  default     = "e2-micro"
}

variable "boot_disk_image" {
  description = "Imagem do disco de boot"
  type        = string
  default     = "ubuntu-os-cloud/ubuntu-2204-lts"
}

variable "boot_disk_size" {
  description = "Tamanho do disco em GB (máx 30 GB gratuito)"
  type        = number
  default     = 30
}

variable "boot_disk_type" {
  description = "Tipo do disco (pd-standard para free tier)"
  type        = string
  default     = "pd-standard"
}

# Variáveis de SSH
variable "ssh_user" {
  description = "Usuário SSH para acessar a VM"
  type        = string
}

variable "ssh_public_key" {
  description = "Chave SSH pública para acesso à VM"
  type        = string
  sensitive   = true
}

# Variável de ambiente
variable "environment" {
  description = "Ambiente de deploy (dev, staging, prod)"
  type        = string
  default     = "prod"
}
