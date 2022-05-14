package example;

import java.util.Random;

/**
 * ProjectLoomEx.
 * Created on 24/4/22.
 * User Khan, C M Abdullah.
 * Ref: https://stackoverflow.com/questions/67164057/how-to-remove-enable-preview-in-java-16
 *
 */
public class Hello {
	static class RandomNumbers implements Runnable {

		Random random = new Random();

		@Override
		public void run() {
			for (int i = 0; i < 120; i++) { // during 120 seconds aprox
				try {
					int a = random.nextInt();
					int b = random.nextInt();
					int c = a * b;
					System.out.println("c = " + c); // print to avoid compiler remove the operation
					Thread.sleep(1000); // each operation every second
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public static void main(String[] args) {
		// Create 1000 native threads
//		for (int i = 0; i < 1000; i++) {
//			Thread t = new Thread(new RandomNumbers());
//			t.start();
//		}

		// Create 1000 virtual threads
		for (int i = 0; i < 1000; i++) {
			Thread.startVirtualThread(new RandomNumbers());
		}

		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
