package ru.minebot.cloverserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class CloverServerApplication {

	private static CloverBridge cloverBridge;

	public static void main(String[] args) {
		cloverBridge = new CloverBridge();
		new Thread(() -> cloverBridge.initialize()).start();
		SpringApplication.run(CloverServerApplication.class, args);

		// Debug
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		new Thread(() -> {
			cloverBridge.launch();
			cloverBridge.waitForNextInputWait();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			cloverBridge.reload();
		}).start();
	}
}
