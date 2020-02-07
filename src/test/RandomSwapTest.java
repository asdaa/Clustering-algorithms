import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class RandomSwapTest {

    private final double EPSILON =  0.00000000001;

    @Test
    void findBestCentroids() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.loadRealCentroids("src/test/testdata/s2-centroids.txt");
        set.centroids = set.getRealCentroids();
        set.partition();

        new KMeans().cluster(set);
        double targetMSE = set.MSE();

        set.initializeRandomCentroids();
        RandomSwap rs = new RandomSwap();
        //this is done in batches of 100 swaps to allow early exit
        boolean matches = true;
        for(int i = 0; i < 100; i++){
            assertTimeoutPreemptively(Duration.ofMillis(3000), () -> rs.cluster(set, 100));
            System.out.println(set.MSE());
            if(set.MSE() / targetMSE < 1 + EPSILON)
                break;
        }
        assertTrue(set.MSE() / targetMSE < 1 + EPSILON);
        assertEquals(0, set.centroidIndex());
    }

    @Test
    void benchmarkVsKMeans() throws IOException {
        // Expected outcome here is to have a higher quality result (but taking more time)
        // This test depends on randomness but when averaged, the random swap should pass this vast majority of time.
        LinkedList<Double> rsTSEs = new LinkedList<>();
        LinkedList<Double> kmTSEs = new LinkedList<>();

        Dataset set = new Dataset("src/test/testdata/birch2.txt", 100);
        set.reduceSize(set.data.length / 10);
        for(int i = 0; i < 20; i++){
            set.initializeRandomCentroids();
            double[][] initialCentroids = set.getCentroids(); // copy
            // the number of iterations is lowered since RS takes longer time to run
            // it should be noted that k-means likes to exit early, which makes it much quicker to run
            assertTimeoutPreemptively(Duration.ofMillis(10000), () -> new RandomSwap().cluster(set, 100));
            rsTSEs.add(set.TSE());

            set.centroids = initialCentroids;
            set.partition();
            assertTimeoutPreemptively(Duration.ofMillis(10000), () -> new KMeans().cluster(set, 1000));
            kmTSEs.add(set.TSE());
        }
        rsTSEs.sort(Comparator.naturalOrder());
        kmTSEs.sort(Comparator.naturalOrder());
        System.out.println("Benchmark:\tRandom swap " + rsTSEs.get(rsTSEs.size()/2) + " TSE median");
        System.out.println("\t\t\tk-means " + kmTSEs.get(kmTSEs.size()/2) + " TSE median" );
        assertTrue(rsTSEs.get(rsTSEs.size()/2) < kmTSEs.get(kmTSEs.size()/2));
    }

}