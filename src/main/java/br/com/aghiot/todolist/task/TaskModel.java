package br.com.aghiot.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

 /**
     * 
     * Id
     * Usuário (ID_USUARIO)
     * Descrição da tarefa
     * Título da tarefa
     * Data de Início
     * Data de Término
     * Prioridade
     */

@Data
@Entity(name = "tb_tasks")
public class TaskModel {
   
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    private UUID userId;
    private String description;

    @Column(length = 50)
    private String title;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String priority;

    @CreationTimestamp
    private LocalDateTime createAt;
}
