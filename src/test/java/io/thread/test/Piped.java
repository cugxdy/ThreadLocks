package io.thread.test;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

import org.junit.Test;

public class Piped {
	
	@Test
	public void main() throws IOException {
		PipedWriter writer = new PipedWriter();
		PipedReader reader = new PipedReader();
		writer.connect(reader);
		
		Thread thread = new Thread(new Print(reader), "PrintThread");
		thread.start();
		
		int receive = 0;
		try {
			while((receive = System.in.read()) != -1) {
				writer.write(receive);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			writer.close();
		}
	}
	
	static class Print implements Runnable {
		private PipedReader reader;

		public Print(PipedReader reader) {
			// TODO Auto-generated constructor stub
			this.reader = reader;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int receive = 0;
			
			try {
				while((receive = reader.read()) != -1) {
					System.out.print((char) receive);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			
		}
		
	}

}
