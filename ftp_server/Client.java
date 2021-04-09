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

public class Client {
	
	private Socket cliente;
	
	
	public static void main(String[] args) throws Exception{
		Client c = new Client();
		c.abreServidor();
		c.executa();		
	}
	public void abreServidor()throws Exception{
		ClientServer sc = new ClientServer(this);
		new File("./ftp_received").mkdir(); //Pasta onde os arquivos recebidos serão armazenados
		new Thread(sc).start();
	}
	
	public void executa() throws Exception{
		Client c = new Client();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int port=8889;
		
		
		System.out.println("Entre com o ip do servidor: ");
		String ip = reader.readLine().trim();
		c.connect(ip,port);
			
		while(true){
			for (int i = 0; i < 100; ++i) System.out.println();
			System.out.println("Selecione uma opcao");
			System.out.println("1) Enviar arquivo");
			System.out.println("2) Baixar arquivo");
			System.out.println("3) Listar usuarios");
			System.out.println("0) Qualquer outra opcao finaliza o programa");
			String opcao = reader.readLine().trim();
			
			if(opcao.equals("1")){
				System.out.println("Entre com o caminho do arquivo: ");
				c.sendFile(reader.readLine().trim());
			}else if(opcao.equals("2")){
				c.getList();
				System.out.println("Entre com o nome do arquivo: ");
				c.getFile(reader.readLine().trim());
			}else if(opcao.equals("3")){
				c.getUsers();
				
			}else{
				c.disconnect();
				System.exit(0);
			}
			reader.readLine();	
		}
	
	}
	
	public void connect(String server, int porta) throws UnknownHostException, IOException{
		cliente = new Socket(server,porta);	
		System.out.println(" ");
	}
	
	public void sendFile(String arq) throws IOException{
		long startTime = System.currentTimeMillis();
        File myFile = new File(arq); //Arquivo a ser enviado 
        byte[] mybytearray = new byte[(int) myFile.length()]; //Cria um vetor de bytes do tamanho do arquivo  
           
        FileInputStream fis = new FileInputStream(myFile);  
        BufferedInputStream bis = new BufferedInputStream(fis);            
        DataInputStream dis = new DataInputStream(bis);     
        dis.readFully(mybytearray, 0, mybytearray.length);  
           
           
           
        OutputStream os = cliente.getOutputStream();    
        DataOutputStream dos = new DataOutputStream(os);    
        dos.writeUTF(myFile.getName()); //Enviando o nome do arquivo
        dos.writeLong(mybytearray.length); //Enviando o tamanho do arquivo    
        dos.writeBoolean(false);  //Diz que nao quer buscar a lista de arquivos
        dos.write(mybytearray, 0, mybytearray.length); //Enviando o arquivo
        dos.flush(); //Forca o envio
        
        double estimatedTime = (System.currentTimeMillis() - startTime)/1000;
        
        
        for (int i = 0; i < 100; ++i) System.out.println();
        System.out.println("Arquivo " + myFile.getName() + " enviado!");
        if(estimatedTime!=0){
			System.out.println("Taxa de transferencia: " + (((double)mybytearray.length)/1000)/estimatedTime + "kbps");  
			System.out.println("Tamanho: " + (((double)mybytearray.length)/1000) + "kb \nTempo: " + estimatedTime + " segundos");    
		}else{
			System.out.println("Taxa de transferencia: " + mybytearray.length + "bps");  
			System.out.println("Tamanho: " + (mybytearray.length) + "bytes \nTempo: 1 segundo");   
		}      
	}
	
	public void getFile(String arq) throws IOException{      //Metodo so envia o nome do arquivo a ser baixado     
        OutputStream os = cliente.getOutputStream();  
        
        DataOutputStream dos = new DataOutputStream(os);     
        dos.writeUTF(arq); //Enviando o nome do arquivo
        dos.writeLong(0L); //Enviando o tamanho do arquivo como 0 pra sinalizar que eh get    
        dos.writeBoolean(false); //Diz que nao quer buscar a lista de arquivos
        dos.flush(); //Forca o envio
	}
	
	public void getList() throws IOException{      //Metodo pede lista de arquivos do servidor
        OutputStream os = cliente.getOutputStream();  
        
        DataOutputStream dos = new DataOutputStream(os);     
        dos.writeUTF(""); //Enviando o nome do arquivo
        dos.writeLong(0L); //Enviando o tamanho do arquivo como 0 pra sinalizar que eh get    
        dos.writeBoolean(true); //Diz que quer buscar a lista de arquivos
        dos.flush(); //Forca o envio
	}
	
	public void getUsers() throws IOException{      //Metodo pede lista de arquivos do servidor
        OutputStream os = cliente.getOutputStream();  
        
        DataOutputStream dos = new DataOutputStream(os);     
        dos.writeUTF("users"); //Enviando o nome do arquivo
        dos.writeLong(0L); //Enviando o tamanho do arquivo como 0 pra sinalizar que eh get    
        dos.writeBoolean(true); //Diz que quer buscar a lista de arquivos
        dos.flush(); //Forca o envio
	}

	public void disconnect() throws UnknownHostException, IOException{
		OutputStream os = cliente.getOutputStream();  
		InputStream is = cliente.getInputStream();   
        
        DataOutputStream dos = new DataOutputStream(os);     
        dos.writeUTF("CLOSE"); //Enviando o nome do arquivo
        dos.writeLong(0L); //Enviando o tamanho do arquivo como 0 pra sinalizar que eh get    
        dos.writeBoolean(true); //Diz que quer buscar a lista de arquivos
        dos.flush(); //Forca o envio
		
		dos.close();
		os.close();
		is.close();
		cliente.close();
		cliente = null;
	}
	
}
