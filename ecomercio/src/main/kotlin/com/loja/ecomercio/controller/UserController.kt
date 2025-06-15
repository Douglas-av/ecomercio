package com.loja.ecomercio.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {

    @Value("\${test:default}")
    var testeValue : String = ""

    @GetMapping
    fun showProperties() : String {
        return testeValue
    }
}