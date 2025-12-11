#!/bin/bash

# ============================================================================
# SCRIPT DE CONFIGURAÇÃO INICIAL DO SERVIDOR
# ============================================================================
# Este script automatiza a configuração inicial do servidor para deploy
# Execute este script MANUALMENTE no servidor antes do primeiro deploy
#
# USO: bash setup-server.sh
# ============================================================================

set -e  # Parar em caso de erro

echo "============================================================================"
echo "🚀 Configuração Inicial do Servidor - ToDoList App"
echo "============================================================================"
echo ""

# ----------------------------------------------------------------------------
# 1. Verificar se está executando como usuário correto (não root)
# ----------------------------------------------------------------------------
if [ "$EUID" -eq 0 ]; then 
    echo "❌ Não execute este script como root!"
    echo "Execute como: bash setup-server.sh"
    exit 1
fi

echo "✅ Executando como usuário: $(whoami)"
echo ""

# ----------------------------------------------------------------------------
# 2. Atualizar sistema
# ----------------------------------------------------------------------------
echo "📦 Atualizando pacotes do sistema..."
sudo apt-get update -qq
sudo apt-get upgrade -y -qq
echo "✅ Sistema atualizado"
echo ""

# ----------------------------------------------------------------------------
# 3. Instalar Docker (se não instalado)
# ----------------------------------------------------------------------------
if ! command -v docker &> /dev/null; then
    echo "🐳 Instalando Docker..."
    curl -fsSL https://get.docker.com | sh
    echo "✅ Docker instalado"
else
    echo "✅ Docker já está instalado: $(docker --version)"
fi
echo ""

# ----------------------------------------------------------------------------
# 4. Instalar Docker Compose (se não instalado)
# ----------------------------------------------------------------------------
if ! command -v docker-compose &> /dev/null; then
    echo "🐙 Instalando Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "✅ Docker Compose instalado"
else
    echo "✅ Docker Compose já está instalado: $(docker-compose --version)"
fi
echo ""

# ----------------------------------------------------------------------------
# 5. Adicionar usuário ao grupo docker
# ----------------------------------------------------------------------------
echo "👤 Adicionando usuário $(whoami) ao grupo docker..."
sudo usermod -aG docker $(whoami)
echo "✅ Usuário adicionado ao grupo docker"
echo "⚠️  Você precisará fazer logout e login novamente para as permissões terem efeito"
echo ""

# ----------------------------------------------------------------------------
# 6. Instalar Git (se não instalado)
# ----------------------------------------------------------------------------
if ! command -v git &> /dev/null; then
    echo "📚 Instalando Git..."
    sudo apt-get install -y git
    echo "✅ Git instalado"
else
    echo "✅ Git já está instalado: $(git --version)"
fi
echo ""

# ----------------------------------------------------------------------------
# 7. Clonar repositório
# ----------------------------------------------------------------------------
REPO_URL="https://github.com/aghiot321/To-do-List.git"
REPO_DIR="$HOME/todolist"

if [ -d "$REPO_DIR" ]; then
    echo "📁 Repositório já existe em $REPO_DIR"
    echo "Atualizando..."
    cd "$REPO_DIR"
    git pull origin main
    echo "✅ Repositório atualizado"
else
    echo "📥 Clonando repositório..."
    git clone "$REPO_URL" "$REPO_DIR"
    echo "✅ Repositório clonado em $REPO_DIR"
fi
echo ""

# ----------------------------------------------------------------------------
# 8. Criar arquivo .env
# ----------------------------------------------------------------------------
cd "$REPO_DIR"

if [ -f ".env" ]; then
    echo "⚠️  Arquivo .env já existe. Pulando criação."
    echo "Para recriar, delete o arquivo .env e execute o script novamente."
else
    echo "📝 Criando arquivo .env..."
    
    # Gerar senhas aleatórias seguras
    MYSQL_ROOT_PASS=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
    MYSQL_USER_PASS=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
    
    cat > .env << EOF
# ============================================================================
# ARQUIVO DE VARIÁVEIS DE AMBIENTE - PRODUÇÃO
# ============================================================================
# Gerado automaticamente em: $(date)
# ⚠️  ATENÇÃO: Este arquivo contém credenciais sensíveis!
# ⚠️  Nunca commite este arquivo no repositório Git!
# ============================================================================

# ----------------------------------------------------------------------------
# CONFIGURAÇÕES DO MYSQL
# ----------------------------------------------------------------------------
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASS}
MYSQL_DATABASE=todolist
MYSQL_USER=todolist_user
MYSQL_PASSWORD=${MYSQL_USER_PASS}
MYSQL_PORT=3309

# ----------------------------------------------------------------------------
# CONFIGURAÇÕES DO DOCKER
# ----------------------------------------------------------------------------
# IMPORTANTE: Substitua 'seu_usuario_dockerhub' pelo seu username real do Docker Hub
IMAGE_NAME=seu_usuario_dockerhub/todolist-app
IMAGE_TAG=latest

# ----------------------------------------------------------------------------
# CONFIGURAÇÕES DA APLICAÇÃO
# ----------------------------------------------------------------------------
SPRING_PROFILES_ACTIVE=prod
EOF

    echo "✅ Arquivo .env criado com senhas aleatórias"
    echo ""
    echo "⚠️  IMPORTANTE: Edite o arquivo .env e substitua 'seu_usuario_dockerhub' pelo seu username real!"
    echo ""
    echo "Senhas geradas:"
    echo "  - MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASS}"
    echo "  - MYSQL_PASSWORD: ${MYSQL_USER_PASS}"
    echo ""
    echo "Salve essas senhas em um local seguro!"
    echo ""
fi

# ----------------------------------------------------------------------------
# 9. Configurar firewall (UFW)
# ----------------------------------------------------------------------------
if command -v ufw &> /dev/null; then
    echo "🔥 Configurando firewall UFW..."
    
    # Verificar se UFW está ativo
    if sudo ufw status | grep -q "Status: active"; then
        echo "UFW está ativo. Configurando regras..."
        
        # Permitir SSH (importante!)
        sudo ufw allow 22/tcp comment 'SSH'
        
        # Permitir porta da aplicação
        sudo ufw allow 8080/tcp comment 'ToDoList App'
        
        # Permitir porta MySQL (apenas se necessário acesso externo)
        # sudo ufw allow 3309/tcp comment 'MySQL'
        
        echo "✅ Regras do firewall configuradas"
    else
        echo "⚠️  UFW não está ativo. Pulando configuração de firewall."
        echo "Para ativar: sudo ufw enable"
    fi
else
    echo "⚠️  UFW não está instalado. Pulando configuração de firewall."
fi
echo ""

# ----------------------------------------------------------------------------
# 10. Verificar instalação
# ----------------------------------------------------------------------------
echo "============================================================================"
echo "🔍 Verificando instalação..."
echo "============================================================================"
echo ""

echo "Docker: $(docker --version)"
echo "Docker Compose: $(docker-compose --version)"
echo "Git: $(git --version)"
echo "Repositório: $REPO_DIR"
echo ""

# Verificar se o usuário está no grupo docker
if groups $(whoami) | grep -q docker; then
    echo "✅ Usuário $(whoami) está no grupo docker"
else
    echo "⚠️  Usuário $(whoami) NÃO está no grupo docker"
    echo "Execute: newgrp docker"
fi
echo ""

# ----------------------------------------------------------------------------
# 11. Próximos passos
# ----------------------------------------------------------------------------
echo "============================================================================"
echo "✅ Configuração inicial concluída!"
echo "============================================================================"
echo ""
echo "📋 Próximos passos:"
echo ""
echo "1️⃣  Editar arquivo .env com suas credenciais:"
echo "    cd $REPO_DIR"
echo "    nano .env"
echo "    (Substitua 'seu_usuario_dockerhub' pelo seu username real)"
echo ""
echo "2️⃣  Aplicar permissões do grupo docker (OBRIGATÓRIO):"
echo "    newgrp docker"
echo "    (Ou faça logout/login)"
echo ""
echo "3️⃣  Testar se consegue executar Docker sem sudo:"
echo "    docker ps"
echo ""
echo "4️⃣  Configurar os Secrets no GitHub:"
echo "    Veja instruções em: SECRETS_SETUP.md"
echo ""
echo "5️⃣  Fazer o primeiro deploy manual (opcional):"
echo "    cd $REPO_DIR"
echo "    docker-compose -f docker-compose.prod.yml up -d"
echo ""
echo "6️⃣  Após configurar os Secrets, faça um push para a branch main:"
echo "    O pipeline de CI/CD será executado automaticamente!"
echo ""
echo "============================================================================"
echo "📚 Documentação adicional:"
echo "  - README.md: Visão geral do projeto"
echo "  - SECRETS_SETUP.md: Configuração detalhada dos Secrets"
echo "  - docker-compose.prod.yml: Configuração de produção"
echo "============================================================================"
echo ""
