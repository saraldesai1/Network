import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	private final int serverPort = 8080;
	public static long TOTALTIME = 0;

	/**
	 * Sends single request to server
	 * @param serverAddr
	 * @param request
	 * @return
	 */
	public ReturnObject singleRequest(final String serverAddr, final int request){
		long latencyBefore = System.currentTimeMillis();
		Socket client;
		ReturnObject valueObject = new ReturnObject();

		//
		try {
			client = new Socket(serverAddr, serverPort);
			ObjectOutputStream output = new ObjectOutputStream((client.getOutputStream()));

			// sending command to server
			output.writeObject(request);


			// recieving
			ObjectInputStream input = new ObjectInputStream((client.getInputStream()));

			long delayMilli = (System.currentTimeMillis() - latencyBefore);
			String received = (String) input.readObject();

			valueObject.setData(received);
			valueObject.setTime(delayMilli);

			// closing sockets
			input.close();
			output.close();
			client.close();
		} catch(UnknownHostException | NoRouteToHostException | java.net.ConnectException uhe){
			valueObject.setData("No host found");
			valueObject.setTime(0);
			return valueObject;
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valueObject;
	}

	/**
	 * Input validation for client requests
	 * @param selectionType
	 * @param selection
	 * @return
	 */
	public static boolean validateInputAlt(int selectionType, String selection){
		if(selection.matches("exit")){
			System.out.println("Closing application");
			System.exit(0);
		}

		//
		switch(selectionType){
			case 1:
				if(!selection.matches("[1-8]{1}")){
					System.out.println("Your input is invalid");
					System.out.println("Please type the number corresponding to your desired action or "
							+ "enter \"exit\" to close this application");
					return false;
				}
				break;
			case 2:
				if(!selection.matches("\\d*")){
					System.out.println("The value of clients must be a number, please try again or "
							+ "enter \"exit\" to close this application");
					return false;
				}
				break;
			case 3:
				if(!selection.matches("y|Y|n|N")){
					System.out.println("You must type \"n\" or \"y\", please try again or "
							+ "enter \"exit\" to close this application");
					return false;
				}
				break;
		}
		return true;
	}

	/**
	 * Gather values for request and client count
	 * @return
	 */
	@SuppressWarnings("resource")
	public static String[] getValues(){
		Scanner scanner = new Scanner(System.in);
		boolean running = true; //State of request gathering
		int step = 1; //Initialize to step 1
		String[] values = new String[2];
		while(running) {
			switch(step){
				case 1:
					System.out.println("Please select which action to perform ");
					System.out.println("1: Date and Time");
					System.out.println("2: Server Uptime");
					System.out.println("3: Server Memory Use");
					System.out.println("4: Server Netstat");
					System.out.println("5: Server Current Users");
					System.out.println("6: Host Running Processes");
					System.out.println("7: Quit");
					break;
				case 2:
					System.out.println("Please enter number of clients making the request");
					break;
//				case 3:
//					System.out.println("Will this be a concurrent request? y/N");
//					break;
			}

			//Get validated input to place in the values array
			String input = scanner.nextLine();
			if(validateInputAlt(step, input)){
				values[step-1] = input;
				if(step == 2){
					System.out.println("Getting data from server...");
					return values;
				} else {
					step++;
				}
			}
		}

		scanner.close();
		return values;
	}

	/**
	 * Point of entry
	 * @param args
	 */
	public static void main(String args[])
	{
		String command = "";
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter the hostname");
		String hostname = sc.nextLine();
		if(hostname.matches("")){
			System.out.println("No host specified. Closing application.");
			sc.close();
			System.exit(0);
		}


		/*
		 * Get hostname input and validate
		 */
		boolean valid = false;
		while(!valid){
			if(!hostname.matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")){
				System.out.println("The server address entered is invalid, please try again or "
						+ "enter \"Exit\" to close this application");
				sc.reset();
				hostname = sc.nextLine();
				valid = false;
			} else {
				TOTALTIME = 0;
				valid = true;
			}

		}

		//Perform requests
		final String addr = hostname;
		while(!command.equals("exit")){
			TOTALTIME = 0;
			final String[] values = getValues();
			Thread[] threads = new Thread[Integer.valueOf(values[1])];

			//Gather new threads into array and run
			for(int i = 0; i<threads.length; i++){
				threads[i] = new Thread(new Runnable(){

					@Override
					public void run() {
						Client cl = new Client();
						int request = Integer.valueOf(values[0]);
//    					if(values[2].matches("y|Y")){
//    						request = (10*request) + 1;
//    					}
// 					System.out.println(request);
						ReturnObject ro = cl.singleRequest(addr, request);
						System.out.println(ro.getData());
						TOTALTIME += ro.getTime();
					}
				});

			}
			for(int i = 0; i<threads.length; i++){
				threads[i].start();
			}

			//Perform a join to allow concurrency
			for(int i = 0; i<threads.length; i++){
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			//Check that a server was reached
			if(TOTALTIME < 0){
				System.out.println("The server returned no response. Please, verify that you have the right address and "
						+ "that the server is running and try again");
			}

			//Calculate mean server time
			long meanTime = TOTALTIME/threads.length;
			System.out.println("Total Time: " + TOTALTIME);
			System.out.println("Mean response time for " + threads.length + " client requests in milliseconds: "+ meanTime);
			System.out.println("Hit Enter to continue or type \"exit\" to exit");
			command = sc.nextLine();
		}

		//Check if user input is "exit" and exit application if so
		if(command.equals("exit")){
			sc.close();
			System.out.println("Application has been exited");
			System.exit(0);
		}
	}

	/**
	 * Object for holding return values
	 * @author kia
	 *
	 */
	public class ReturnObject {
		long time;
		String data;

		public void setData(String data){
			this.data = data;
		}

		public void setTime(long time){
			this.time = time;
		}

		public String getData(){
			return this.data;
		}

		public long getTime(){
			return this.time;
		}
	}
}
