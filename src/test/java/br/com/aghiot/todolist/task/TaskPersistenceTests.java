package br.com.aghiot.todolist.task;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TaskPersistenceTests {

    @Autowired
    private ITaskRepository taskRepository;

    private TaskModel testTask;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testTask = new TaskModel();
        testTask.setUserId(testUserId);
        testTask.setTitle("Test Task");
        testTask.setDescription("This is a test task");
        testTask.setPriority("HIGH");
        testTask.setStartAt(LocalDateTime.now());
        testTask.setEndAt(LocalDateTime.now().plusDays(1));
    }

    @Test
    void testCreateTask() {
        // Arrange & Act
        TaskModel savedTask = taskRepository.save(testTask);

        // Assert
        assertNotNull(savedTask);
        assertNotNull(savedTask.getId());
        assertEquals("Test Task", savedTask.getTitle());
        assertEquals("This is a test task", savedTask.getDescription());
        assertEquals("HIGH", savedTask.getPriority());
        assertEquals(testUserId, savedTask.getUserId());
    }

    @Test
    void testFindTaskById() {
        // Arrange
        TaskModel savedTask = taskRepository.save(testTask);
        UUID taskId = savedTask.getId();

        // Act
        Optional<TaskModel> foundTask = taskRepository.findById(taskId);

        // Assert
        assertTrue(foundTask.isPresent());
        assertEquals(taskId, foundTask.get().getId());
        assertEquals("Test Task", foundTask.get().getTitle());
    }

    @Test
    void testUpdateTask() {
        // Arrange
        TaskModel savedTask = taskRepository.save(testTask);
        UUID taskId = savedTask.getId();

        // Act
        savedTask.setTitle("Updated Task Title");
        savedTask.setDescription("Updated description");
        savedTask.setPriority("LOW");
        TaskModel updatedTask = taskRepository.save(savedTask);

        // Assert
        assertEquals(taskId, updatedTask.getId());
        assertEquals("Updated Task Title", updatedTask.getTitle());
        assertEquals("Updated description", updatedTask.getDescription());
        assertEquals("LOW", updatedTask.getPriority());
    }

    @Test
    void testDeleteTask() {
        // Arrange
        TaskModel savedTask = taskRepository.save(testTask);
        UUID taskId = savedTask.getId();

        // Act
        taskRepository.deleteById(taskId);

        // Assert
        Optional<TaskModel> deletedTask = taskRepository.findById(taskId);
        assertFalse(deletedTask.isPresent());
    }

    @Test
    void testFindAllTasks() {
        // Arrange
        TaskModel task1 = new TaskModel();
        task1.setUserId(testUserId);
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setPriority("HIGH");

        TaskModel task2 = new TaskModel();
        task2.setUserId(testUserId);
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setPriority("MEDIUM");

        taskRepository.save(task1);
        taskRepository.save(task2);

        // Act
        var allTasks = taskRepository.findAll();

        // Assert
        assertNotNull(allTasks);
        assertTrue(allTasks.size() >= 2);
    }

    @Test
    void testTaskWithDifferentPriorities() {
        // Arrange
        TaskModel highPriorityTask = new TaskModel();
        highPriorityTask.setUserId(testUserId);
        highPriorityTask.setTitle("High Priority");
        highPriorityTask.setPriority("HIGH");

        TaskModel lowPriorityTask = new TaskModel();
        lowPriorityTask.setUserId(testUserId);
        lowPriorityTask.setTitle("Low Priority");
        lowPriorityTask.setPriority("LOW");

        // Act
        TaskModel savedHigh = taskRepository.save(highPriorityTask);
        TaskModel savedLow = taskRepository.save(lowPriorityTask);

        // Assert
        assertEquals("HIGH", savedHigh.getPriority());
        assertEquals("LOW", savedLow.getPriority());
    }

    @Test
    void testTaskWithStartAndEndDates() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(5);

        testTask.setStartAt(startDate);
        testTask.setEndAt(endDate);

        // Act
        TaskModel savedTask = taskRepository.save(testTask);

        // Assert
        assertEquals(startDate, savedTask.getStartAt());
        assertEquals(endDate, savedTask.getEndAt());
    }

    @Test
    void testTaskWithNullEndDate() {
        // Arrange
        testTask.setEndAt(null);

        // Act
        TaskModel savedTask = taskRepository.save(testTask);

        // Assert
        assertNotNull(savedTask.getId());
        assertNull(savedTask.getEndAt());
        assertNotNull(savedTask.getStartAt());
    }

    @Test
    void testMultipleTasksForSameUser() {
        // Arrange
        TaskModel task1 = new TaskModel();
        task1.setUserId(testUserId);
        task1.setTitle("Task for User 1");
        task1.setDescription("Desc 1");

        TaskModel task2 = new TaskModel();
        task2.setUserId(testUserId);
        task2.setTitle("Task for User 2");
        task2.setDescription("Desc 2");

        // Act
        TaskModel saved1 = taskRepository.save(task1);
        TaskModel saved2 = taskRepository.save(task2);

        // Assert
        assertEquals(testUserId, saved1.getUserId());
        assertEquals(testUserId, saved2.getUserId());
        assertNotEquals(saved1.getId(), saved2.getId());
    }
}
