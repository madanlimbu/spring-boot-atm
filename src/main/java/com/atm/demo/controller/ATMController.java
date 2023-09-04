package com.atm.demo.controller;

import com.atm.demo.service.ATMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ATMController {

    @Autowired
    ATMService atmService;
}
