#!/bin/bash

# ============================================================================
# SCRIPT DE CONFIGURACAO INICIAL DO SERVIDOR
# ============================================================================
# Este script automatiza a configuracao inicial do servidor para deploy
# Execute este script MANUALMENTE no servidor antes do primeiro deploy
#
# USO: bash setup-server.sh
# ============================================================================

set -e

echo "============================================================================"
echo "Configuracao Inicial do Servidor - ToDoList App"
echo "============================================================================"
echo ""

if [ "$EUID" -eq 0 ]; then 
    echo "ERRO: Nao execute este script como root!"
    echo "Execute como: bash setup-server.sh"
    exit 1
fi

echo "Executando como usuario: $(whoami)"
echo ""

echo "Atualizando pacotes do sistema..."
sudo apt-get update -qq
sudo apt-get upgrade -y -qq
echo "Sistema atualizado"
echo ""

if ! command -v docker &> /dev/null; then
    echo "Instalando Docker..."
    curl -fsSL https://get.docker.com | sh
    echo "Docker instalado"
else
    echo "Docker ja esta instalado: $(docker --version)"
fi
echo ""

if ! command -v docker-compose &> /dev/null; then
    echo "Instalando Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "Docker Compose instalado"
else
    echo "Docker Compose ja esta instalado: $(docker-compose --version)"
fi
echo ""

echo "Adicionando usuario $(whoami) ao grupo docker..."
sudo usermod -aG docker $(whoami)
echo "Usuario adicionado ao grupo docker"
echo "ATENCAO: Voce precisara fazer logout e login novamente para as permissoes terem efeito"
echo ""

if ! command -v git &> /dev/null; then
    echo "Instalando Git..."
    sudo apt-get install -y git
    echo "Git instalado"
else
    echo "Git ja esta instalado: $(git --version)"
fi
echo ""

REPO_URL="https://github.com/aghiot321/To-do-List.git"
REPO_DIR="$HOME/todolist"

if [ -d "$REPO_DIR" ]; then
    echo "Repositorio ja existe em $REPO_DIR"
    echo "Atualizando..."
    cd "$REPO_DIR"
    git pull origin main
    echo "Repositorio atualizado"
else
    echo "Clonando repositorio..."
    git clone "$REPO_URL" "$REPO_DIR"
    echo "Repositorio clonado em $REPO_DIR"
fi
echo ""

cd "$REPO_DIR"

if [ -f ".env" ]; then
    echo "ATENCAO: Arquivo .env ja existe. Pulando criacao."
    echo "Para recriar, delete o arquivo .env e execute o script novamente."
else
    echo "Criando arquivo .env..."
    
    MYSQL_ROOT_PASS=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
    MYSQL_USER_PASS=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
    
    cat > .env << EOF
# ============================================================================
# ARQUIVO DE VARIAVEIS DE AMBIENTE - PRODUCAO
# ============================================================================
# Gerado automaticamente em: $(date)
# ATENCAO: Este arquivo contem credenciais sensiveis!
# ATENCAO: Nunca commite este arquivo no repositorio Git!
# ============================================================================

# CONFIGURACOES DO MYSQL
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASS}
MYSQL_DATABASE=todolist
MYSQL_USER=todolist_user
MYSQL_PASSWORD=${MYSQL_USER_PASS}
MYSQL_PORT=3309

# CONFIGURACOES DO DOCKER
# IMPORTANTE: Substitua 'seu_usuario_dockerhub' pelo seu username real do Docker Hub
IMAGE_NAME=seu_usuario_dockerhub/todolist-app
IMAGE_TAG=latest

# CONFIGURACOES DA APLICACAO
SPRING_PROFILES_ACTIVE=prod
EOF

    echo "Arquivo .env criado com senhas aleatorias"
    echo ""
    echo "IMPORTANTE: Edite o arquivo .env e substitua 'seu_usuario_dockerhub' pelo seu username real!"
    echo ""
    echo "Senhas geradas:"
    echo "  - MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASS}"
    echo "  - MYSQL_PASSWORD: ${MYSQL_USER_PASS}"
    echo ""
    echo "Salve essas senhas em um local seguro!"
    echo ""
fi

if command -v ufw &> /dev/null; then
    echo "Configurando firewall UFW..."
    
    if sudo ufw status | grep -q "Status: active"; then
        echo "UFW esta ativo. Configurando regras..."
        
        sudo ufw allow 22/tcp comment 'SSH'
        sudo ufw allow 8080/tcp comment 'ToDoList App'
        
        echo "Regras do firewall configuradas"
    else
        echo "ATENCAO: UFW nao esta ativo. Pulando configuracao de firewall."
        echo "Para ativar: sudo ufw enable"
    fi
else
    echo "ATENCAO: UFW nao esta instalado. Pulando configuracao de firewall."
fi
echo ""

echo "============================================================================"
echo "Verificando instalacao..."
echo "============================================================================"
echo ""

echo "Docker: $(docker --version)"
echo "Docker Compose: $(docker-compose --version)"
echo "Git: $(git --version)"
echo "Repositorio: $REPO_DIR"
echo ""

if groups $(whoami) | grep -q docker; then
    echo "Usuario $(whoami) esta no grupo docker"
else
    echo "ATENCAO: Usuario $(whoami) NAO esta no grupo docker"
    echo "Execute: newgrp docker"
fi
echo ""

echo "============================================================================"
echo "Configuracao inicial concluida!"
echo "============================================================================"
echo ""
echo "Proximos passos:"
echo ""
echo "1. Editar arquivo .env com suas credenciais:"
echo "   cd $REPO_DIR"
echo "   nano .env"
echo "   (Substitua 'seu_usuario_dockerhub' pelo seu username real)"
echo ""
echo "2. Aplicar permissoes do grupo docker (OBRIGATORIO):"
echo "   newgrp docker"
echo "   (Ou faca logout/login)"
echo ""
echo "3. Testar se consegue executar Docker sem sudo:"
echo "   docker ps"
echo ""
echo "4. Configurar os Secrets no GitHub conforme documentado no README"
echo ""
echo "5. Fazer o primeiro deploy manual (opcional):"
echo "   cd $REPO_DIR"
echo "   docker-compose -f docker-compose.prod.yml up -d"
echo ""
echo "6. Apos configurar os Secrets, faca um push para a branch main:"
echo "   O pipeline de CI/CD sera executado automaticamente!"
echo ""
echo "============================================================================"
