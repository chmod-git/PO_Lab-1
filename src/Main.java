import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class Main {

    public static void main(String[] args) {
        int n = 1000;
        int m = 2000;
        int mult = 2;
        n *= mult;
        m *= mult;

        int[][] array = new int[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                array[i][j] = (int) (Math.random() * 10000);
            }
        }

        long startTime = System.nanoTime();

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int currentValue = array[i][j];
                if (currentValue < min) {
                    min = currentValue;
                }
                if (currentValue > max) {
                    max = currentValue;
                }
            }
        }

        long endTime = System.nanoTime();
        double elapsedTime = (endTime - startTime) / 1e9;

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        double memoryUsage = memoryBean.getHeapMemoryUsage().getUsed() / (1024.0 * 1024.0);

        System.out.printf("Execution Time: %.5f seconds\n", elapsedTime);
        System.out.printf("Memory Usage: %.2f MB\n", memoryUsage);
        System.out.println("Overall Minimum Value: " + min);
        System.out.println("Overall Maximum Value: " + max);
    }
}
