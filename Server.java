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

public class Server{
	
	
	ServerSocket servidor;
	ArrayList<Socket> clientes= new ArrayList<Socket>();
	
	

	
	public static void main(String[] args) throws Exception{
		Server r = new Server();
		
		r.abreServidor();
		r.executa();
	}
	
	public void abreServidor() throws Exception{
		servidor = new ServerSocket(8889);
		InetAddress ip = InetAddress.getLocalHost();	
		new File("./ftp_server").mkdir(); //Pasta onde os arquivos recebidos serão armazenados
		System.out.println("Porta "+servidor.getLocalPort()+" aberta!");
		System.out.println("IP do servidor: "+ip.getHostAddress());
		
	}
	
	public void executa() throws IOException{
		
		while(true){
			try{
				//Aceita um cliente
				Socket cliente = servidor.accept();
				Socket aux=null;
				
				for (Socket s:clientes) {
					if(s.getInetAddress().getHostAddress().equals(cliente.getInetAddress().getHostAddress())){
						aux = s;
					}
				}
				clientes.remove(aux);
				clientes.add(cliente);
				
				System.out.println("\nCliente conectado: " + cliente.getInetAddress().getHostAddress());
				if(cliente.isBound()){
					ServerReceiver rc = new ServerReceiver(cliente, this);
					new Thread(rc).start();
				}
			}catch(IOException e){
				System.out.println("catch accept: " + e.getMessage());
			}
		}


	}
	
	public void sendUsers(String ip){
		

		try{
			String line="";

			for (Socket s:clientes) {
				if(s.isBound() && s.isConnected() && !s.isClosed() && !s.isInputShutdown() && !s.isOutputShutdown()){
					line =  line.concat(s.getInetAddress().getHostAddress()+System.getProperty("line.separator"));
				}
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
}
