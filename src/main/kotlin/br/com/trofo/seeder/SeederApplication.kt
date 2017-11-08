package br.com.trofo.seeder

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class SeederApplication

fun main(args: Array<String>) {
    SpringApplication.run(SeederApplication::class.java, *args)
}
