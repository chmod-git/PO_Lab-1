import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class Main {
    public static void compute(int[][] array, int startRow, int endRow, int[] minArray, int[] maxArray, int index) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = startRow; i < endRow - startRow; i++) {
            for (int j = 0; j < array[0].length; j++) {
                int currentValue = array[i][j];
                if (currentValue < min) {
                    min = currentValue;
                }
                if (currentValue > max) {
                    max = currentValue;
                }
            }
        }

        minArray[index] = min;
        maxArray[index] = max;
    }

    public static void main(String[] args) {
        int n = 1000;
        int m = 2000;
        int mult = 2;
        int min = -1;
        int max = -1;
        n *= mult;
        m *= mult;
        double runAmount = 20.0;
        int[][] array = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                array[i][j] = (int) (Math.random() * 10000);
            }
        }

        int[] numThreadsList = {1, 2, 4, 8, 16, 32, 64};

        for (int numThreads : numThreadsList) {
            double elapsedTime = 0.0;
            double totalMemoryUsage = 0.0;
            for (int i = 0; i < runAmount; i++) {
                long startTime = System.nanoTime();
                int[] minArray = new int[numThreads];
                int[] maxArray = new int[numThreads];
                Thread[] threads = new Thread[numThreads];
                int chunkSize = n / numThreads;
                for (int j = 0; j < numThreads; j++) {
                    int startRow = j * chunkSize;
                    int endRow = (j == numThreads - 1) ? n : (j + 1) * chunkSize;
                    final int index = j;
                    threads[j] = new Thread(() -> compute(array, startRow, endRow, minArray, maxArray, index));
                    threads[j].start();
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                min = minArray[0];
                max = maxArray[0];
                for (int j = 0; j < numThreads; j++) {
                    if (minArray[j] < min) min = minArray[j];
                    if (maxArray[j] > max) max = maxArray[j];
                }
                long endTime = System.nanoTime();
                elapsedTime += (endTime - startTime) / 1e9;
            }

            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            totalMemoryUsage += memoryBean.getHeapMemoryUsage().getUsed() / (1024.0 * 1024.0);

            System.out.printf("Number of Threads: %d, Total Execution Time: %.5f seconds, Memory Usage: %.2f MB\n",
                    numThreads, (elapsedTime / runAmount), (totalMemoryUsage / runAmount));
        }

        System.out.println("Overall Minimum Value: " + min);
        System.out.println("Overall Maximum Value: " + max);
    }
}
