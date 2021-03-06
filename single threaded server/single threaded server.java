import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server
{
	public static final float Megabytes = 1024*1024; // constant for displaying memory in a pretty way

	public static void main(String args[]) throws ClassNotFoundException, InterruptedException, IOException
	{
		int serverPort = 8080; // make sure to change port if another teammate is using it when you want to use it
		long startTimeMili = System.currentTimeMillis(); // for getting uptime if the user wants it
		boolean quit = false;
		int userCount = 0; // make sure to increment userCount at each function
		ServerSocket server = new ServerSocket(serverPort);

		while (!quit)
		{
			try
			{
				Socket socket = server.accept();
				ObjectInputStream input = new ObjectInputStream((socket.getInputStream()));
				ObjectOutputStream output = new ObjectOutputStream((socket.getOutputStream()));

				Runtime runtime = Runtime.getRuntime();
				float memory = runtime.totalMemory()-runtime.freeMemory();

				// gets option for menu
				int option = Integer.parseInt((input.readObject().toString()));

				switch (option)
				{
					default: // invalid input
						System.out.println(("\nInvalid Input"));
						output.writeObject("\nInvalid Input, try again dummy.");
						break;

					case 1: // Sending date and time
						userCount++;
						System.out.println(("\nSending date and time"));
						output.writeObject("Server Date and time: "+ new Date());
						break;

					case 2: // Sending Host uptime
						userCount++;
						System.out.println(("\nSending Host uptime"));
						output.writeObject("\nHost Uptime In Milliseconds: " + (System.currentTimeMillis() - startTimeMili));
						break;

					case 3: // Sending Host memory use
						userCount++;
						System.out.println(("\nSending Host memory use"));
						output.writeObject(" Memory usage in megabytes: " + (memory/Megabytes));
						break;

					case 4: // Sending Host Netstat
						userCount++;
						System.out.println(("\nSending Host Netstat"));

						String netstat = "";
						Process netStatProcess = Runtime.getRuntime().exec("netstat");
						BufferedReader reader = new BufferedReader((new InputStreamReader((netStatProcess.getInputStream()))));
						while ((reader.readLine()) != null)
						{
							netstat += ("\n" + reader.readLine());
							//System.out.println((reader.readLine()));
						}

						output.writeObject("\nNetstat: " + netstat);
						break;

					case 5: // Sending Host current users
						System.out.println(("\nSending Host current users"));
						output.writeObject("\nUser Count: " + userCount);
						userCount++;
						break;

					case 6: // Host running processes
						userCount++;
						System.out.println(("\nSending Host running processes"));
						String runningProcesses = "";
						Process runningProcessesProcess = Runtime.getRuntime().exec("ps -e");
						BufferedReader processReader = new BufferedReader((new InputStreamReader((runningProcessesProcess.getInputStream()))));
						while ((processReader.readLine()) != null)
						{
							runningProcesses += ("\n" + processReader.readLine());
							//System.out.println((reader.readLine()));
						}

						output.writeObject("\nCurrent Running Processes:  " + runningProcesses);
						break;

					case 7: // quitting
						userCount++;
						System.out.println(("\nQuitting"));
						output.writeObject("\nThank you for using me, goodbye!");
						quit = true;
						break;
				}

				//System.out.println(("Client sent us: " + recieved));

				input.close();
				output.close();
				socket.close();

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
