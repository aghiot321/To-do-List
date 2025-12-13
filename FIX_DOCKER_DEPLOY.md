# 🔧 Correção dos Problemas de Deploy do Docker

## 📋 Problemas Identificados

O deploy estava falha com os seguintes erros:

1. **`docker-compose: command not found`** - O comando não estava disponível
2. **`permission denied`** - Usuário sem permissão para acessar o Docker socket

## ✅ Soluções Aplicadas

### 1. **Uso do Docker Compose Plugin**

**Antes:** Instalávamos o `docker-compose` standalone (binário separado)
```bash
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-..." -o /usr/local/bin/docker-compose
```

**Depois:** Usamos o plugin `docker compose` que já vem com o Docker moderno
```bash
# O Docker moderno já inclui o plugin compose
docker compose version
```

**Benefícios:**
- Mais moderno e mantido ativamente
- Já vem incluído no Docker
- Melhor integração com o Docker CLI

### 2. **Wrapper de Compatibilidade**

Criamos um script wrapper `/usr/local/bin/docker-compose` que redireciona para `docker compose`:

```bash
#!/bin/bash
exec docker compose "$@"
```

Isso garante compatibilidade com scripts que ainda usam `docker-compose` (hífen).

### 3. **Correção de Permissões**

**Problema:** Quando adicionamos um usuário ao grupo docker (`usermod -aG docker user`), ele precisa fazer logout/login para as permissões tomarem efeito. Mas o GitHub Actions SSH executa imediatamente.

**Soluções aplicadas:**

#### No Startup Script (Terraform):
```bash
# Dar permissões temporárias ao socket
chmod 666 /var/run/docker.sock
```

#### No Script de Deploy (GitHub Actions):
```bash
# Verificar se Docker funciona, senão corrigir permissões
if ! docker ps &> /dev/null; then
  sudo chmod 666 /var/run/docker.sock
fi
```

### 4. **Verificação Robusta**

**Antes:** Apenas verificava se o comando `docker` existia
```bash
if command -v docker &> /dev/null; then
  echo "✅ Docker encontrado!"
fi
```

**Depois:** Verifica se o Docker está instalado E funcionando
```bash
if command -v docker &> /dev/null && docker ps &> /dev/null; then
  echo "✅ Docker encontrado e funcionando!"
fi
```

## 📝 Mudanças nos Arquivos

### [terraform/main.tf](terraform/main.tf)

1. Removida instalação standalone do docker-compose
2. Criado script wrapper para compatibilidade
3. Adicionadas permissões ao socket do Docker
4. Atualizada verificação para usar `docker compose version`

### [.github/workflows/cicd-terraform.yml](.github/workflows/cicd-terraform.yml)

1. Verificação robusta de instalação e permissões do Docker
2. Correção automática de permissões quando necessário
3. Todos os comandos `docker-compose` substituídos por `docker compose`
4. Melhor tratamento de erros e timeouts

## 🚀 Como Testar

### 1. Aplicar mudanças na infraestrutura

Se já tiver uma VM rodando, você tem duas opções:

**Opção A: Recriar a VM (recomendado)**
```bash
cd terraform
terraform destroy -auto-approve
terraform apply -auto-approve
```

**Opção B: Aplicar correções manualmente na VM existente**
```bash
# Conectar à VM
ssh seu-usuario@IP_DA_VM

# Criar wrapper do docker-compose
sudo tee /usr/local/bin/docker-compose > /dev/null << 'EOF'
#!/bin/bash
exec docker compose "$@"
EOF
sudo chmod +x /usr/local/bin/docker-compose

# Corrigir permissões
sudo chmod 666 /var/run/docker.sock

# Testar
docker ps
docker compose version
docker-compose version
```

### 2. Executar o workflow

1. Faça commit e push das mudanças:
```bash
git add .
git commit -m "fix: corrigir problemas de permissão e docker-compose no deploy"
git push origin main
```

2. Acompanhe no GitHub Actions:
   - Vá para **Actions** no repositório
   - Veja o workflow **CI/CD Pipeline com Terraform**
   - Verifique se o deploy completa com sucesso

### 3. Verificar aplicação

Após o deploy bem-sucedido:

```bash
# Health check
curl http://SEU_IP:8080/actuator/health

# Verificar containers
ssh usuario@IP docker compose -f ~/todolist/docker-compose.prod.yml ps
```

## 🔍 Diagnóstico de Problemas

Se ainda houver problemas:

### Verificar logs do startup script
```bash
ssh usuario@IP
sudo cat /var/log/startup-script.log
```

### Verificar Docker
```bash
docker --version
docker compose version
docker ps
```

### Verificar permissões
```bash
groups  # Deve incluir 'docker'
ls -la /var/run/docker.sock  # Deve ter permissões adequadas
```

### Logs da aplicação
```bash
cd ~/todolist
docker compose -f docker-compose.prod.yml logs --tail=100 todolist-app
```

## 📚 Referências

- [Docker Compose V2](https://docs.docker.com/compose/cli-command/)
- [Docker Post-installation Steps](https://docs.docker.com/engine/install/linux-postinstall/)
- [GitHub Actions SSH Action](https://github.com/appleboy/ssh-action)

## ⚠️ Notas Importantes

1. **Segurança:** `chmod 666` no socket é uma solução temporária. Em produção, use grupos adequadamente
2. **Docker Compose V1 vs V2:** O Docker Compose V2 (`docker compose`) é o padrão desde 2021
3. **Cloud-Init:** O startup script pode levar alguns minutos para completar
4. **GitHub Actions:** O timeout de 10 minutos deve ser suficiente para o deploy completo
