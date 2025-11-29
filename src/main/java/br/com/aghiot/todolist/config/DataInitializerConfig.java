package br.com.aghiot.todolist.config;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.aghiot.todolist.task.ITaskRepository;
import br.com.aghiot.todolist.task.TaskModel;
import br.com.aghiot.todolist.user.IUserRepository;
import br.com.aghiot.todolist.user.UserModel;

@Configuration
public class DataInitializerConfig {

    @Bean
    @Profile("!test")
    @SuppressWarnings("unused")
    CommandLineRunner initData(IUserRepository userRepository, ITaskRepository taskRepository) {
        return _unused -> {
            if (userRepository.count() > 0) {
                return;
            }

            UserModel user1 = new UserModel();
            user1.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
            user1.setName("João Silva");
            user1.setUsername("joao");
            user1.setPassword(BCrypt.withDefaults().hashToString(12, "senha123".toCharArray()));
            userRepository.save(user1);

            UserModel user2 = new UserModel();
            user2.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
            user2.setName("Maria Santos");
            user2.setUsername("maria");
            user2.setPassword(BCrypt.withDefaults().hashToString(12, "senha123".toCharArray()));
            userRepository.save(user2);

            UserModel user3 = new UserModel();
            user3.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"));
            user3.setName("Pedro Oliveira");
            user3.setUsername("pedro");
            user3.setPassword(BCrypt.withDefaults().hashToString(12, "senha123".toCharArray()));
            userRepository.save(user3);

            LocalDateTime now = LocalDateTime.now();

            TaskModel task1 = new TaskModel();
            task1.setUserId(user1.getId());
            task1.setTitle("Implementar Login");
            task1.setDescription("Criar sistema de autenticação com JWT");
            task1.setPriority("HIGH");
            task1.setStartAt(now);
            task1.setEndAt(now.plusDays(3));
            taskRepository.save(task1);

            TaskModel task2 = new TaskModel();
            task2.setUserId(user1.getId());
            task2.setTitle("Criar Banco de Dados");
            task2.setDescription("Estruturar tabelas e relacionamentos");
            task2.setPriority("HIGH");
            task2.setStartAt(now);
            task2.setEndAt(now.plusDays(2));
            taskRepository.save(task2);

            TaskModel task3 = new TaskModel();
            task3.setUserId(user2.getId());
            task3.setTitle("Documentar API");
            task3.setDescription("Criar documentação Swagger/OpenAPI");
            task3.setPriority("MEDIUM");
            task3.setStartAt(now);
            task3.setEndAt(now.plusDays(5));
            taskRepository.save(task3);

            TaskModel task4 = new TaskModel();
            task4.setUserId(user2.getId());
            task4.setTitle("Testes Unitários");
            task4.setDescription("Implementar testes JUnit5");
            task4.setPriority("HIGH");
            task4.setStartAt(now);
            task4.setEndAt(now.plusDays(4));
            taskRepository.save(task4);

            TaskModel task5 = new TaskModel();
            task5.setUserId(user3.getId());
            task5.setTitle("Design UI");
            task5.setDescription("Criar interface com componentes reusáveis");
            task5.setPriority("MEDIUM");
            task5.setStartAt(now);
            task5.setEndAt(now.plusDays(7));
            taskRepository.save(task5);

            TaskModel task6 = new TaskModel();
            task6.setUserId(user3.getId());
            task6.setTitle("Deploy em Produção");
            task6.setDescription("Configurar ambiente e CI/CD");
            task6.setPriority("LOW");
            task6.setStartAt(now.plusDays(10));
            task6.setEndAt(now.plusDays(15));
            taskRepository.save(task6);
        };
    }
}
