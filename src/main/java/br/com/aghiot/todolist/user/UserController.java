package br.com.aghiot.todolist.user;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")

public class UserController {
    @PostMapping("/")
    public void create(@RequestBody UserModel userModel) {
        System.out.println(userModel.getUsername());
    }
}
/**
 * Modificador
 * public
 * private
 * protected
 */

/**
 * String (texto)
 * int (inteiro)
 * boolean (verdadeiro ou falso)
 * long (inteiro longo)
 * double (número com ponto flutuante)
 * float (número com ponto flutuante de precisão simples)
 * char (caractere único)
 * byte (número inteiro pequeno)
 * short (número inteiro curto)
 */