package com.loja.serviceregistry

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@SpringBootApplication
@EnableEurekaServer
class ServiceregistryApplication

fun main(args: Array<String>) {
    runApplication<ServiceregistryApplication>(*args)
}
