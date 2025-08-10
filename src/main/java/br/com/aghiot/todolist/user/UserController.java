package br.com.aghiot.todolist.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")

public class UserController {
    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public UserModel create(@RequestBody UserModel userModel) {
        var user =  this.userRepository.findByUsername(userModel.getUsername());

        if(user != null){
            System.out.println("Usuário já existe");
            return null;
        }

        var userCreated = this.userRepository.save(userModel);
        return userCreated;
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