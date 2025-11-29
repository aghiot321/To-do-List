package br.com.aghiot.todolist.user;

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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IUserRepository userRepository;

    private UserModel userModel;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();

        userModel = new UserModel();
        userModel.setName("John Doe");
        userModel.setUsername("johndoe");
        userModel.setPassword("password123");
    }

    @Test
    public void testCreateUser() throws Exception {
        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userModel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.password").isNotEmpty());
    }

    @Test
    public void testCreateUser_DuplicateUsername() throws Exception {
        // Criar primeiro usuário
        userRepository.save(userModel);

        // Tentar criar outro usuário com o mesmo username
        UserModel duplicateUser = new UserModel();
        duplicateUser.setName("Jane Doe");
        duplicateUser.setUsername("johndoe"); // mesmo username
        duplicateUser.setPassword("password456");

        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuário já existe"));
    }

    @Test
    public void testListAllUsers() throws Exception {
        // Criar alguns usuários
        UserModel user1 = new UserModel();
        user1.setName("User One");
        user1.setUsername("user1");
        user1.setPassword("pass1");
        userRepository.save(user1);

        UserModel user2 = new UserModel();
        user2.setName("User Two");
        user2.setUsername("user2");
        user2.setPassword("pass2");
        userRepository.save(user2);

        mockMvc.perform(get("/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    public void testGetUserById() throws Exception {
        UserModel savedUser = userRepository.save(userModel);

        mockMvc.perform(get("/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/users/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuário não encontrado"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        UserModel savedUser = userRepository.save(userModel);

        UserModel updatedUser = new UserModel();
        updatedUser.setName("Updated Name");
        updatedUser.setUsername("updateduser");
        updatedUser.setPassword("newpassword");

        mockMvc.perform(put("/users/" + savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.username").value("updateduser"));
    }

    @Test
    public void testUpdateUser_WithoutPassword() throws Exception {
        UserModel savedUser = userRepository.save(userModel);
        String originalPassword = savedUser.getPassword();

        UserModel updatedUser = new UserModel();
        updatedUser.setName("Updated Name");
        updatedUser.setUsername("updateduser");
        // Não definir password

        mockMvc.perform(put("/users/" + savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        // Verificar que a senha não mudou
        UserModel userFromDb = userRepository.findById(savedUser.getId()).get();
        assert(userFromDb.getPassword().equals(originalPassword));
    }

    @Test
    public void testUpdateUser_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        UserModel updatedUser = new UserModel();
        updatedUser.setName("Updated Name");
        updatedUser.setUsername("updateduser");

        mockMvc.perform(put("/users/" + randomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuário não encontrado"));
    }

    @Test
    public void testDeleteUser() throws Exception {
        UserModel savedUser = userRepository.save(userModel);

        mockMvc.perform(delete("/users/" + savedUser.getId()))
                .andExpect(status().isNoContent());

        // Verificar se o usuário foi realmente deletado
        mockMvc.perform(get("/users/" + savedUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteUser_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(delete("/users/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuário não encontrado"));
    }

    @Test
    public void testPasswordIsHashed() throws Exception {
        String plainPassword = "mySecretPassword";
        
        UserModel user = new UserModel();
        user.setName("Test User");
        user.setUsername("testpassword");
        user.setPassword(plainPassword);

        mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").exists())
                // Verificar que a senha retornada não é a senha em texto plano
                .andExpect(jsonPath("$.password").value(not(plainPassword)));
    }
}
