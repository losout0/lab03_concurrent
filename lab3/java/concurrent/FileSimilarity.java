import java.io.*;
import java.util.*;

public class FileSimilarity {

    // Create a map to store the fingerprint for each file
    static Map<String, List<Long>> fileFingerprints = new HashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        // Calculate the fingerprint for each file
        for (String path : args) {
            FileSum taskSum = new FileSum(path);
            Thread threadSum = new Thread(taskSum, "myThreadSum");
            threadSum.start();
            //threadSum.join();
        }

        // Compare each pair of files
        for (int i = 0; i < args.length; i++) {
            for (int j = i + 1; j < args.length; j++) {
                String file1 = args[i];
                String file2 = args[j];
                List<Long> fingerprint1 = fileFingerprints.get(file1);
                List<Long> fingerprint2 = fileFingerprints.get(file2);
                Similarity taskSim =  new Similarity(file1, file2, fingerprint1, fingerprint2);
                Thread threadSim = new Thread(taskSim, "myThread");
                threadSim.start();
            }
        }
    }

    private static long sum(byte[] buffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Byte.toUnsignedInt(buffer[i]);
        }
        return sum;
    }

    public static class FileSum implements Runnable{
        private File file;
        private List<Long> chunks;
        private String filePath;

        public FileSum(String filePath){
            this.filePath = filePath;
            this.file = new File(filePath);
            this.chunks = new ArrayList<>();

        }
        
        @Override
        public void run() {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[100];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    long sum = sum(buffer, bytesRead);
                    chunks.add(sum);  // possível região crítica
                }
            }catch(Exception e){
                System.err.println(e.getMessage());
            }
            fileFingerprints.put(filePath, chunks);
        }
    }

    public static class Similarity implements Runnable{

        private List<Long> base;
        private String file1;
        private List<Long> target;
        private String file2;

        public Similarity(String file1, String file2, List<Long> base, List<Long> target){
            this.file1 = file1;
            this.file2 = file2;
            this.base = base;
            this.target = target;
        }

        @Override
        public void run() {
            int counter = 0;
            List<Long> targetCopy = new ArrayList<>(target);
    
            for (Long value : base) {
                if (targetCopy.contains(value)) {
                    counter++;
                    targetCopy.remove(value);
                }
            }
            float similarityScore = (float) counter / base.size();
            System.out.println("Similarity between " + this.file1 + " and " + this.file2 + ": " + (similarityScore * 100) + "%");
        }
    
        
    }
}
