terraform {
  required_version = ">= 1.0"
  
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
  
  # Backend remoto configurado via backend.tf
}

# Provider Google Cloud
provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
  zone    = var.gcp_zone
}

# Script de inicialização (Cloud-Init) para instalar Docker e Docker Compose
locals {
  startup_script = <<-EOT
    #!/bin/bash
    set -e
    
    # Log de execução
    exec > >(tee /var/log/startup-script.log)
    exec 2>&1
    
    echo "=== Iniciando configuração do servidor ==="
    
    # Atualizar sistema
    apt-get update -qq
    apt-get upgrade -y -qq
    
    # Instalar Docker
    echo "=== Instalando Docker ==="
    curl -fsSL https://get.docker.com | sh
    
    # Adicionar usuário ao grupo docker
    usermod -aG docker ${var.ssh_user}
    
    # Instalar Docker Compose
    echo "=== Instalando Docker Compose ==="
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    
    # Instalar Git
    echo "=== Instalando Git ==="
    apt-get install -y git
    
    # Instalar outras dependências úteis
    apt-get install -y curl wget nano htop
    
    # Habilitar Docker para iniciar no boot
    systemctl enable docker
    systemctl start docker
    
    # Verificar instalações
    echo "=== Verificando instalações ==="
    docker --version
    docker-compose --version
    git --version
    
    echo "=== Configuração concluída com sucesso ==="
  EOT
}

# Regra de firewall para aplicação (porta 8080)
resource "google_compute_firewall" "todolist_app" {
  name    = "allow-todolist-app"
  network = "default"
  
  allow {
    protocol = "tcp"
    ports    = ["8080"]
  }
  
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["todolist-server"]
  
  description = "Permite acesso à aplicação ToDoList na porta 8080"
}

# Regra de firewall para MySQL (porta 3309) - Opcional
resource "google_compute_firewall" "todolist_mysql" {
  name    = "allow-mysql"
  network = "default"
  
  allow {
    protocol = "tcp"
    ports    = ["3309"]
  }
  
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["todolist-server"]
  
  description = "Permite acesso ao MySQL na porta 3309"
}

# Instância de VM (Compute Engine)
resource "google_compute_instance" "todolist_server" {
  name         = var.instance_name
  machine_type = var.machine_type
  zone         = var.gcp_zone
  
  tags = ["todolist-server"]
  
  # Disco de boot
  boot_disk {
    initialize_params {
      image = var.boot_disk_image
      size  = var.boot_disk_size
      type  = var.boot_disk_type
    }
  }
  
  # Interface de rede
  network_interface {
    network = "default"
    
    # IP externo ephemeral (gratuito)
    access_config {
      # Deixe vazio para usar IP ephemeral
      # Para IP estático, use: nat_ip = google_compute_address.static_ip.address
    }
  }
  
  # Script de inicialização
  metadata_startup_script = local.startup_script
  
  # Chave SSH
  metadata = {
    ssh-keys = "${var.ssh_user}:${var.ssh_public_key}"
  }
  
  # Labels para organização
  labels = {
    environment = var.environment
    managed_by  = "terraform"
    project     = "todolist"
  }
  
  # Permitir reinicialização automática em caso de manutenção
  scheduling {
    automatic_restart   = true
    on_host_maintenance = "MIGRATE"
  }
  
  # Service account com permissões mínimas
  service_account {
    scopes = ["cloud-platform"]
  }
}

# (Opcional) IP estático - Custa ~$3/mês
# Descomente se precisar de IP fixo
# resource "google_compute_address" "static_ip" {
#   name   = "todolist-static-ip"
#   region = var.gcp_region
# }
