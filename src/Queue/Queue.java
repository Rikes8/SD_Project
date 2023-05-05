    package src.Queue;

    import java.net.*;
    import java.io.*;
    import java.util.ArrayList;
    import java.util.StringTokenizer;
    import java.util.concurrent.*;

    import static java.lang.Integer.parseInt;


    public class Queue {
        private static int serverPort = 6000;

        //LinkedBlockingQueue does not need size
        public static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        public static LinkedBlockingQueue<String> processados = new LinkedBlockingQueue<>();
        public static ArrayList<String> cache = new ArrayList<>();


        public static void main(String args[]){
            int numero=0;
            String file_info = "";

            //file backup location
            String file_queue = "src/Queue/queue_cache.txt";

            //socket TCP
            try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
                System.out.println("LISTEN SOCKET=" + listenSocket);

                //if file is empty create new one, if exists read information insided it
                File f = new File(file_queue);
                if (f.exists()) {
                    //read the file
                    try{
                        FileReader freader = new FileReader(f);
                        BufferedReader reader = new BufferedReader(freader);

                        while ((file_info = reader.readLine()) != null){
                            queue.add(file_info);
                        }

                        reader.close();
                    }catch (IOException e){
                    }
                }else{
                    f.createNewFile();
                }
                //System.out.println(queue);

                //Ctrl + C handler
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        String file_queue = "src/Queue/queue_cache.txt";
                        //store information in the file
                        //if file has information, read first, add the new, and store to it
                        try{
                            FileWriter file = new FileWriter(file_queue);
                            while (!queue.isEmpty()){
                                cache.add(queue.poll());
                            }
                            for(String c : cache){
                                System.out.println(c);
                                file.write(c + "\n");
                            }
                            file.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("Queue ending...");
                    }
                });


                while(true) {
                    Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                    //System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                    numero++;
                    new Connection(clientSocket, numero, queue,processados);
                }
            } catch(IOException e) {
                System.out.println("Listen:" + e.getMessage());
            }
        }
    }

    // Thread -> client channel
    class Connection extends Thread {
        DataInputStream in;
        DataOutputStream out;
        Socket clientSocket;
        int thread_number;
        LinkedBlockingQueue<String> queue;
        LinkedBlockingQueue<String> process;


        public Connection (Socket aClientSocket, int numero, LinkedBlockingQueue<String> fifo, LinkedBlockingQueue<String> processados) {
            thread_number = numero;
            try{
                clientSocket = aClientSocket;
                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());
                queue = fifo;
                process = processados;
                this.start();
            }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
        }
        //=============================
        public void run(){
            String data, pedido;
            try {
                while (true) {
                    //recieve request from downloader to crawl
                    pedido = in.readUTF();
                    String[] aux = pedido.split(" ");


                    if (aux[0].equals("pedido")) {
                        //send url to downloader
                        String url = queue.take();
                        System.out.println(url);
                        out.writeUTF(url);

                        //recieve information from downloader
                        data = in.readUTF();

                        String dataAux[] = data.split("-->");
                        if(dataAux[0].equals(url)){
                            //System.out.println("AQUI");
                            process.add(dataAux[0]);
                        }
                        else{
                            queue.add(dataAux[0]);
                        }
                        int i = 0;
                        int length = data.length();
                        String[] words = new String[length];
                        StringTokenizer tokens = new StringTokenizer(dataAux[1]);
                        while (tokens.hasMoreElements()) {
                            words[i] = tokens.nextToken();
                            if (words[i].equals("url_ap")) {
                                String garbage = tokens.nextToken();
                                String Url = tokens.nextToken();
                                if (!queue.contains(Url) && !process.contains(Url)) {
                                    queue.add(Url);
                                }
                            }
                            i = i + 1;
                        }

                    }else if (aux.length == 2){ //receive request from client
                        out.writeUTF("message received!");
                        String Url = aux[1];
                        if (!queue.contains(Url) && !process.contains(Url)) {
                            queue.add(Url);
                        }
                    }
                    pedido = "";
                    //System.out.println("queue contains " + queue);
                }


            } catch(IOException e) {
                //e.printStackTrace();
                //System.out.println("aqui2");
            }catch(InterruptedException e){
               // e.printStackTrace();
                //System.out.println("aqui3");
            }
        }

    }
