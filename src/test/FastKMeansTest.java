import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests for the fast k-means clustering algorithm class.
 * Confirms that the functionality matches standard k-means
 * and evaluates the performance.
 *
 * TODO: split the methods into individual test cases
 * @author Juho Puumalainen
 */
class FastKMeansTest {

    @Test
    void compareToKMeans() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        for(int i = 0; i < 25; i++){
            set.initializeRandomCentroids();
            double[][] initialCentroids = set.getCentroids(); // copy
            assertTimeoutPreemptively(Duration.ofMillis(5000), () -> new FastKMeans().cluster(set));
            double[][] fkmCentroids = set.getCentroids();

            set.centroids = initialCentroids;
            set.partition();
            assertTimeoutPreemptively(Duration.ofMillis(5000), () -> new KMeans().cluster(set));
            assertTrue(Arrays.deepEquals(fkmCentroids, set.getCentroids()));
        }

        // try with a harder set
        Dataset set2 = new Dataset("src/test/testdata/birch2.txt", 100);
        for(int i = 0; i < 5; i++){
            set.initializeRandomCentroids();
            double[][] initialCentroids = set.getCentroids(); // copy
            assertTimeoutPreemptively(Duration.ofMillis(10000), () -> new FastKMeans().cluster(set2));
            double[][] fkmCentroids = set.getCentroids();

            set.centroids = initialCentroids;
            set.partition();
            assertTimeoutPreemptively(Duration.ofMillis(10000), () -> new KMeans().cluster(set2));
            assertTrue(Arrays.deepEquals(fkmCentroids, set.getCentroids()));
        }
    }

    @Test
    void compareToKMeansStepByStep() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        for(int i = 0; i < 25; i++){
            set.initializeRandomCentroids();
            for(int j = 0; j < 10; j++){
                double[][] initialCentroids = set.getCentroids(); // copy
                assertTimeoutPreemptively(Duration.ofMillis(2000), () -> new FastKMeans().cluster(set, 1));
                double[][] fkmCentroids = set.getCentroids();

                set.centroids = initialCentroids;
                set.partition();
                assertTimeoutPreemptively(Duration.ofMillis(2000), () -> new KMeans().cluster(set, 1));
                assertTrue(Arrays.deepEquals(fkmCentroids, set.getCentroids()));
            }
        }
    }

    @Test
    void benchmarkVsKMeans() throws IOException {
        // This test is not particularly exact, as it depends on processing time.
        // The JVM or background processes can make one of the algorithms hang.
        // However, when outliers are ignored, the fast k-means algorithm should pass this in 99.9%(?) of cases.

        // we use the median time to weed out any outliers in timings
        LinkedList<Long> fkmTimes = new LinkedList<>();
        LinkedList<Long> kmTimes = new LinkedList<>();

        Dataset set = new Dataset("src/test/testdata/birch2.txt", 100);
        set.reduceSize(set.data.length / 20);
        for(int i = 0; i < 20; i++){
            set.initializeRandomCentroids();
            double[][] initialCentroids = set.getCentroids(); // copy
            long start = System.nanoTime();
            assertTimeoutPreemptively(Duration.ofMillis(10000), () -> new FastKMeans().cluster(set, 500));
            fkmTimes.add(System.nanoTime() - start);
            double[][] fkmCentroids = set.getCentroids();

            set.centroids = initialCentroids;
            set.partition();
            start = System.nanoTime();
            assertTimeoutPreemptively(Duration.ofMillis(10000), () -> new KMeans().cluster(set, 500));
            kmTimes.add(System.nanoTime() - start);
            assertTrue(Arrays.deepEquals(fkmCentroids, set.getCentroids()));
        }
        fkmTimes.sort(Comparator.naturalOrder());
        kmTimes.sort(Comparator.naturalOrder());
        System.out.println("Benchmark:\tFast k-means " + (fkmTimes.get(fkmTimes.size()/2)/1000000) + " ms median");
        System.out.println("\t\t\tk-means " + (kmTimes.get(fkmTimes.size()/2)/1000000) + " ms median" );
        assertTrue(fkmTimes.get(fkmTimes.size()/2) < kmTimes.get(kmTimes.size()/2));
    }
}