import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientServer implements Runnable{

	Client client;
	ServerSocket receiver;
	
	public ClientServer(Client client) throws Exception{
		this.client = client;
		receiver = new ServerSocket(8080);
	}

	//Thread que aceita conexao do servidor para receber arquivos
	public void run () {
		while(true){
			try{
				//Aceita a conexao com o servidor
				Socket cliente = receiver.accept();
				if(cliente.isBound()){
					receiveFile(cliente); // Recebe arquivos do servidor
				}
			}catch(IOException e){
				System.out.println("catch accept: " + e.getMessage());
			}
		}
	}
	
	public void receiveFile(Socket socket){
		try{
			int bytesRead;  
			int current = 0;  
				   
			InputStream in = socket.getInputStream();
			DataInputStream socketData = new DataInputStream(in);   
			String fileName = socketData.readUTF();     
			long size = socketData.readLong();
			long sizeAux = size;
			long startTime = System.currentTimeMillis();
			
			
			if(size!=0L){
				OutputStream output = new FileOutputStream("./ftp_received\\"+fileName); 
				byte[] buffer = new byte[1024];     
				
				while (size > 0 && (bytesRead = socketData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)     
				{     
					output.write(buffer, 0, bytesRead);     
					size -= bytesRead;
					
					System.out.println("Arquivo " + fileName + " " + (((sizeAux-size)*100)/sizeAux) + "% recebido ");  
				}  
				
				double estimatedTime = (System.currentTimeMillis() - startTime)/1000;

				for (int i = 0; i < 100; ++i) System.out.println();
				System.out.println("Arquivo " + fileName + " recebido!");
				System.out.println("Arquivos recebidos ficam na pasta ./ftp_received");
				
				if(estimatedTime!=0){
					System.out.println("Taxa de transferencia: " + (((double)sizeAux)/1000)/estimatedTime + "kbps");   
					System.out.println("Tamanho: " + (((double)sizeAux)/1000) + "kb \nTempo: " + estimatedTime + " segundos");
				}else{
					System.out.println("Taxa de transferencia: " + sizeAux + "bps");
					System.out.println("Tamanho: " + (sizeAux) + "bytes \nTempo: 1 segundo");  
				}
				
				
				output.close();
			}else{
				System.out.println(fileName);
			}		
		}catch(IOException e){
			System.out.println("catch receive: " + e.getMessage());
		}
	}
}
