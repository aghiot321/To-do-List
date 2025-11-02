# todolist

## Docker Compose - Configuração

Configuração Docker Compose com dois serviços:
- MySQL 8.4.0 (banco de dados)
- Spring Boot Application (aplicação)

### Variáveis de Ambiente

As seguintes variáveis estão configuradas em `.env`:

- MYSQL_ROOT_PASSWORD - Senha root do MySQL
- MYSQL_DATABASE - Nome do banco de dados
- MYSQL_USER - Usuário do banco
- MYSQL_PASSWORD - Senha do usuário
- MYSQL_PORT - Porta do MySQL (padrão: 3306)
- SPRING_PROFILE_ACTIVE - Perfil da aplicação
- SERVER_PORT - Porta da aplicação (padrão: 8080)
- JAVA_OPTS - Opções JVM

### Como Executar

Iniciar os serviços:
```bash
docker-compose up -d
```

Parar os serviços:
```bash
docker-compose down
```

Ver status:
```bash
docker-compose ps
```

Ver logs:
```bash
docker-compose logs -f
```

### Acessar os Serviços

- Aplicação: http://localhost:8080
- MySQL: localhost:3306

Credenciais MySQL:
- Usuário: todolist_user
- Senha: todolist_password
- Banco: todolist

### Testar Conexão com MySQL

```bash
docker-compose exec mysql mysql -u todolist_user -p todolist
```

Dentro do MySQL:
```sql
show tables;
describe user;
describe task;
```

### Volumes

Dados persistem em `./mysql_data`. Para remover dados:
```bash
docker-compose down -v
```

### Rede

Containers se comunicam através da rede `todolist-network`.

### Health Checks

- MySQL: verificação a cada 10 segundos
- Aplicação: verificação a cada 30 segundos

### Backup e Restauração

Backup:
```bash
docker-compose exec mysql mysqldump -u todolist_user -p todolist > backup.sql
```

Restaurar:
```bash
docker-compose exec mysql mysql -u todolist_user -p todolist < backup.sql
```

### Solução de Problemas

Logs da aplicação:
```bash
docker-compose logs todolist-app
```

Logs do MySQL:
```bash
docker-compose logs mysql
```