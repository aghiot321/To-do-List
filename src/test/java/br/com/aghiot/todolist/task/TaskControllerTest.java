package br.com.aghiot.todolist.task;

import br.com.aghiot.todolist.user.IUserRepository;
import br.com.aghiot.todolist.user.UserModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ITaskRepository taskRepository;

    @Autowired
    private IUserRepository userRepository;

    private UUID userId;
    private TaskModel taskModel;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // Criar um usuário para associar às tarefas
        UserModel user = new UserModel();
        user.setName("Test User");
        user.setUsername("testuser");
        user.setPassword("password123");
        user = userRepository.save(user);
        userId = user.getId();

        // Criar uma tarefa de exemplo
        taskModel = new TaskModel();
        taskModel.setTitle("Test Task");
        taskModel.setDescription("Test Description");
        taskModel.setPriority("ALTA");
        taskModel.setUserId(userId);
        taskModel.setStartAt(LocalDateTime.now());
        taskModel.setEndAt(LocalDateTime.now().plusDays(1));
    }

    @Test
    public void testCreateTask() throws Exception {
        mockMvc.perform(post("/tasks/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskModel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.priority").value("ALTA"))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    public void testListAllTasks() throws Exception {
        // Criar algumas tarefas
        TaskModel task1 = taskRepository.save(taskModel);
        
        TaskModel task2 = new TaskModel();
        task2.setTitle("Second Task");
        task2.setDescription("Second Description");
        task2.setPriority("BAIXA");
        task2.setUserId(userId);
        task2.setStartAt(LocalDateTime.now());
        task2.setEndAt(LocalDateTime.now().plusDays(2));
        taskRepository.save(task2);

        mockMvc.perform(get("/tasks/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[1].title").value("Second Task"));
    }

    @Test
    public void testGetTaskById() throws Exception {
        TaskModel savedTask = taskRepository.save(taskModel);

        mockMvc.perform(get("/tasks/" + savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId().toString()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    public void testGetTaskById_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/tasks/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found"));
    }

    @Test
    public void testUpdateTask() throws Exception {
        TaskModel savedTask = taskRepository.save(taskModel);

        TaskModel updatedTask = new TaskModel();
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");
        updatedTask.setPriority("MEDIA");
        updatedTask.setUserId(userId);
        updatedTask.setStartAt(LocalDateTime.now());
        updatedTask.setEndAt(LocalDateTime.now().plusDays(3));

        mockMvc.perform(put("/tasks/" + savedTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId().toString()))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.priority").value("MEDIA"));
    }

    @Test
    public void testUpdateTask_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        TaskModel updatedTask = new TaskModel();
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");

        mockMvc.perform(put("/tasks/" + randomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found"));
    }

    @Test
    public void testDeleteTask() throws Exception {
        TaskModel savedTask = taskRepository.save(taskModel);

        mockMvc.perform(delete("/tasks/" + savedTask.getId()))
                .andExpect(status().isNoContent());

        // Verificar se a tarefa foi realmente deletada
        mockMvc.perform(get("/tasks/" + savedTask.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteTask_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(delete("/tasks/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found"));
    }
}
