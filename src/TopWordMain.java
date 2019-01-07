import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TopWordMain {

    private int countProcess = Runtime.getRuntime().availableProcessors();

    BlockingQueue<String>strings = new ArrayBlockingQueue<>(10,true);

    BlockingQueue<Map<String,Integer>>mapBlockingQueue = new ArrayBlockingQueue<>(countProcess,true);

    private Map<String,Integer>resultMap = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {

        TopWordMain main = new TopWordMain();

        List<Thread>threads = new ArrayList<>();

        for(int i = 0; i<main.countProcess; i++){

            threads.add(new Thread(new Worker(main)));

        }

        for (Thread thread: threads){
            thread.start();
        }
        try(FileInputStream in2 = new FileInputStream("wp.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in2))

        ){

            String word;

            while((word =  bufferedReader.readLine())!=null){
                main.strings.put(word);
            }

            for (int k = 0; k<main.countProcess; k++) main.strings.put("STOP");

            for (Thread thread: threads){
                thread.join();
            }


        }catch (IOException e){
            e.printStackTrace();
        }


        for(Map<String,Integer>mapWords : main.mapBlockingQueue){

            for (Map.Entry word : mapWords.entrySet()){
                main.resultMap.merge(word.getKey().toString(), (Integer)word.getValue(),(v1,v2)->v1+v2);
            }
        }

        main.resultMap.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(100).forEach(System.out::println);

    }
}

class Worker extends Thread {

    private BlockingQueue<String>words;
    private BlockingQueue<Map<String,Integer>>mapBlockingQueue;

    private Map<String,Integer>innerMap;

    public Worker(TopWordMain main){
        this.words = main.strings;
        this.mapBlockingQueue = main.mapBlockingQueue;
        this.innerMap = new HashMap<>();
    }

    @Override
    public void run() {
        String line = null;

        try{

            while(!(line = words.take()).equals("STOP")){

                String[] wordsList  = line.toLowerCase().replaceAll("\\pP", " ").trim().split("\\s");

                for(String word : wordsList){
                    if (word.equals("")){
                        continue;
                    }
                    innerMap.merge(word,1,(v1,v2)->v1 + v2);
                }
            }

            mapBlockingQueue.put(innerMap);

        }catch (InterruptedException e ){
            e.printStackTrace();
        }

    }


}

class TestThread extends Thread {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
}
