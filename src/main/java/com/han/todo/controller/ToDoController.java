package com.han.todo.controller;

import com.han.todo.domain.ToDo;
import com.han.todo.domain.ToDoBuilder;
import com.han.todo.repository.ToDoRepository;
import com.han.todo.validation.ToDoValidationError;
import com.han.todo.validation.ToDoValidationErrorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;
@RestController
@RequestMapping("/api")
public class ToDoController {
    private ToDoRepository toDoRepository;
    @Autowired
    public ToDoController(ToDoRepository toDoRepository) {
        this.toDoRepository = toDoRepository;
    }
    @GetMapping("/todo")
    public ResponseEntity<Iterable<ToDo>> getToDos(){
        return ResponseEntity.ok(toDoRepository.findAll());
    }
    @GetMapping("/todo/{id}")
    public ResponseEntity<ToDo> getToDoById(@PathVariable String id){
        Optional<ToDo> toDo = toDoRepository.findById(id);
        if(toDo.isPresent())
            return ResponseEntity.ok(toDo.get());
        return ResponseEntity.notFound().build();
    }
    @PatchMapping("/todo/{id}")
    public ResponseEntity<ToDo> setCompleted(@PathVariable String id){
        Optional<ToDo> toDo = toDoRepository.findById(id);
        if(!toDo.isPresent())
            return ResponseEntity.notFound().build();
        ToDo result = toDo.get();
        result.setCompleted(true);
        toDoRepository.save(result);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.ok().header("Location",location.toString()).
                build();
    }
    @RequestMapping(value="/todo", method = {RequestMethod.
            POST,RequestMethod.PUT})
    public ResponseEntity<?> createToDo(@Valid @RequestBody ToDo toDo,
                                        Errors errors){
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().
                    body(errors.getAllErrors());
        }
        ToDo result = toDoRepository.save(toDo);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().
                path("/{id}").buildAndExpand(result.getId()).toUri();

        //return ResponseEntity.created(location).build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("location",location.toString());
        return new ResponseEntity(result,headers,HttpStatus.OK);
    }
    @DeleteMapping("/todo/{id}")
    public ResponseEntity<ToDo> deleteToDo(@PathVariable String id){
        Optional<ToDo> toDo = toDoRepository.findById(id);
        if(!toDo.isPresent())
            return ResponseEntity.notFound().build();
        ToDo result = toDo.get();
        toDoRepository.delete(result);
        return ResponseEntity.noContent().build();

    }
    @DeleteMapping("/todo")
    public ResponseEntity<ToDo> deleteToDo(@RequestBody ToDo toDo){
        toDoRepository.delete(toDo);
        return ResponseEntity.noContent().build();
    }
    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ToDoValidationError handleException(Exception exception) {
        return new ToDoValidationError(exception.getMessage());
    }
}