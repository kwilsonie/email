package com.example.demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class EmailController {

    @Autowired
    private EmailRepository emailRepository;

    @RequestMapping(value = "/emails", method = RequestMethod.GET)
    public List<Email> getAllEmails() {
        return emailRepository.findAll();
    }

    @RequestMapping(value = "/email", method = RequestMethod.POST)
    public Email sendEmail(@RequestBody Email email) {
        return emailRepository.save(email);
    }

    @RequestMapping(value = "/email/{id}", method = RequestMethod.GET)
    public Email getEmail(@PathVariable Long id) {
        return emailRepository.findById(id).orElse(null);
    }

    @RequestMapping(value = "/email/{id}", method = RequestMethod.DELETE)
    public String deleteEmail(@PathVariable Long id) {
        emailRepository.deleteById(id);
        return "Deleted";
    }
}