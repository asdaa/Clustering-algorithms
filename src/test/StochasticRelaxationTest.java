import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class StochasticRelaxationTest {

    private final double EPSILON =  0.00000000001;

    @Test
    void compareToKMeans() throws IOException {
        // The datasets here are selected in such a way that the SR algorithm wins k-means more consistently.
        // It should be noted that SR sometimes under-performs on easy sets like s2 and only wins out
        // if you repeat both algorithms multiple times.

        Dataset set = new Dataset("src/test/testdata/bridge.txt", 128);
        for(int i = 0; i < 10; i++){
            set.initializeRandomCentroids();
            double[][] initialCentroids = set.getCentroids(); // copy
            // set the temperature alpha to a high value to guarantee a good result
            assertTimeoutPreemptively(Duration.ofMillis(5000), () -> new StochasticRelaxation(0.95).cluster(set));
            double srTSE = set.TSE();

            set.centroids = initialCentroids;
            set.partition();
            assertTimeoutPreemptively(Duration.ofMillis(5000), () -> new KMeans().cluster(set));
            assertTrue(srTSE / set.TSE() < 1 + EPSILON);
        }

        // try with a harder set
        Dataset set2 = new Dataset("src/test/testdata/birch2.txt", 100);
        set2.reduceSize(set2.data.length/10);
        for(int i = 0; i < 5; i++){
            set.initializeRandomCentroids();
            double[][] initialCentroids = set.getCentroids(); // copy
            // set the temperature alpha to a high value to guarantee a good result
            assertTimeoutPreemptively(Duration.ofMillis(3000), () -> new StochasticRelaxation(0.98).cluster(set));
            double srTSE = set.TSE();

            set.centroids = initialCentroids;
            set.partition();
            assertTimeoutPreemptively(Duration.ofMillis(3000), () -> new KMeans().cluster(set));
            assertTrue(srTSE / set.TSE() < 1 + EPSILON);
        }
    }

    @Test
    void benchmarkVsKMeans() throws IOException {
        // Expected outcome here is to have a higher quality result (but taking more time)
        // This test depends on randomness but when averaged, the stochastic relaxation should pass this vast majority of time.
        LinkedList<Double> srTSEs = new LinkedList<>();
        LinkedList<Double> kmTSEs = new LinkedList<>();

        Dataset set = new Dataset("src/test/testdata/bridge.txt", 128);
        set.reduceSize(set.data.length/4);
        for(int i = 0; i < 20; i++){
            set.initializeRandomCentroids();
            double[][] initialCentroids = set.getCentroids(); // copy

            assertTimeoutPreemptively(Duration.ofMillis(10000), () -> new StochasticRelaxation().cluster(set, 1000));
            srTSEs.add(set.TSE());

            set.centroids = initialCentroids;
            set.partition();
            assertTimeoutPreemptively(Duration.ofMillis(10000), () -> new KMeans().cluster(set, 1000));
            kmTSEs.add(set.TSE());
        }
        srTSEs.sort(Comparator.naturalOrder());
        kmTSEs.sort(Comparator.naturalOrder());
        System.out.println("Benchmark:\tStochastic " + srTSEs.get(srTSEs.size()/2) + " TSE median");
        System.out.println("\t\t\tk-means " + kmTSEs.get(kmTSEs.size()/2) + " TSE median" );
        assertTrue(srTSEs.get(srTSEs.size()/2) < kmTSEs.get(kmTSEs.size()/2));
    }
}