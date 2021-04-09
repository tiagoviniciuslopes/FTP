import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;

public class ServerReceiver implements Runnable{

	Server server;
	private Socket cliente;
	private Server servidor;
	
	public ServerReceiver(Socket cliente, Server server){
		this.cliente = cliente;
		this.server = server;
	}
	
	public void run(){
		while(true){ 
			
			if(cliente.isBound() && cliente.isConnected() && !cliente.isClosed()){
				receive(cliente);
			}else{
				 return;
			}
		}
	}
	
	public void receive(Socket socket){
		try{
			int bytesRead;  
			int current = 0;  
			
			InputStream in = socket.getInputStream();
			   
			DataInputStream socketData = new DataInputStream(in);   
			String fileName = socketData.readUTF();        
			long size = socketData.readLong();
			boolean bool = socketData.readBoolean();
			
			if(size!=0L){ //Verifica se o arquivo é pra ser recebido ou enviado
				OutputStream output = new FileOutputStream("./ftp_server\\"+fileName);
				byte[] buffer = new byte[1024];     
				while (size > 0 && (bytesRead = socketData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)     
				{     
					output.write(buffer, 0, bytesRead);     
					size -= bytesRead;     
				}  
				   
				System.out.println("Arquivo " + fileName + " recebido!");  
				output.close();
			}else{ 
				if(bool && fileName.equals("")){ //Verifica se deve enviar a lista de arquivos ou nao
					sendList(socket.getInetAddress().getHostAddress());
				}else if(bool && fileName.equals("users")) {
					sendUsers(socket.getInetAddress().getHostAddress());
				}else if(bool && fileName.equals("CLOSE")) {
					socket.close();
				}else{
					send(fileName,socket.getInetAddress().getHostAddress());
				}
			}
		}catch(IOException e){
			return;
		}
	}
	
	public void send(String fileName,String ip){
		try{
					
			File myFile = new File("./ftp_server\\"+fileName); //Arquivo a ser enviado 
			byte[] mybytearray = new byte[(int) myFile.length()]; //Cria um vetor de bytes do tamanho do arquivo  
			
			FileInputStream fis = new FileInputStream(myFile);  
			BufferedInputStream bis = new BufferedInputStream(fis);   
			
			DataInputStream dis = new DataInputStream(bis);     
			dis.readFully(mybytearray, 0, mybytearray.length);  
			
			
			Socket sender = new Socket(ip,8080);

			OutputStream os = sender.getOutputStream();  

			DataOutputStream dos = new DataOutputStream(os);     
			dos.writeUTF(myFile.getName()); //Enviando o nome do arquivo
			dos.writeLong(mybytearray.length); //Enviando o tamanho do arquivo     
			dos.write(mybytearray, 0, mybytearray.length); //Enviando o arquivo
			dos.flush(); //Forca o envio
				 
			System.out.println("Arquivo " + fileName + " enviado ao cliente " + ip);
			   
			   
			sender.close();
			sender = null;
		}catch(IOException e){
			System.out.println("catch send: " + e.getMessage());
		}
	}
	
	public void sendList(String ip){
		

		try{
			String line="";
			
			File folder = new File("./ftp_server");
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				
				line = line.concat(listOfFiles[i].getName()+System.getProperty("line.separator"));
			}

			Socket sender = new Socket(ip,8080);
			
			OutputStream os = sender.getOutputStream();   
			
			DataOutputStream dos = new DataOutputStream(os);     
			dos.writeUTF(line); //Enviando o nome do arquivo
			dos.writeLong(0L); //Enviando o tamanho do arquivo como 0 pra sinalizar que eh get    
			dos.flush(); //Forca o envio		 
			
			   
			sender.close();
			sender = null;
		}catch(IOException e){
			System.out.println("catch sendlist: " + e.getMessage());
		}
	}
	
	public void sendUsers(String ip){
		server.sendUsers(ip);
	}
	
}
