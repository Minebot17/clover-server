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

		// Debug
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		PrintStream outStream = System.out;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		new Thread(() -> {
			cloverBridge.launch();
			cloverBridge.waitForNextInputWait();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			cloverBridge.loadGame("kk");
			System.setOut(outStream);
			System.out.println(cloverBridge.getLastMessage());
			System.out.println("Hello0");
			cloverBridge.sendResponse("Hello0");
			System.out.println(cloverBridge.getLastMessage());
			System.out.println("Hello1");
			cloverBridge.sendResponse("Hello1");
			System.out.println(cloverBridge.getLastMessage());
		}).start();
	}
}
