package com.loja.ecomercio

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<EcomercioApplication>().with(TestcontainersConfiguration::class).run(*args)
}
