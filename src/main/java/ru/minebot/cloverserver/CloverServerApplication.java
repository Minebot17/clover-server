package ru.minebot.cloverserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

@SpringBootApplication
public class CloverServerApplication {

	private static CloverBridge cloverBridge;

	public static void main(String[] args) {
		cloverBridge = new CloverBridge();
		new Thread(() -> cloverBridge.initialize()).start();
		SpringApplication.run(CloverServerApplication.class, args);
	}
}
