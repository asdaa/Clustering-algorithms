import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the algorithm runner class.
 * Does not test full functionality, just makes some
 * basic sanity checks.
 *
 * TODO: split the methods into individual test cases
 * @author Juho Puumalainen
 */
class ClusteringTest {

    @Test
    void runOnceVsRunMultiple(){
        // These must be run for multiple times to reduce the effect of random initial conditions
        // There is a possibility of this test failing randomly, but it should be very unlikely and fixable by
        // running the test again. We can check the collected statistics in the same go.

        double cumulativeTSE = 0;
        double cumulativeNMSE = 0;
        double cumulativeCI = 0;
        double bestTSE = Double.MAX_VALUE;
        for(int i = 0; i < 200; i++){
            Clustering km = new Clustering("src/test/testdata/s2.txt", 15,
                    "src/test/testdata/s2-centroids.txt", new KMeans());
            km.run();
            bestTSE = Math.min(km.dataset.TSE(), bestTSE);
            cumulativeTSE += km.dataset.TSE();
            cumulativeNMSE += km.dataset.TSE() / km.dataset.data.length / km.dataset.data[0].length;
            cumulativeCI += km.dataset.centroidIndex();
        }

        Clustering km = new Clustering("src/test/testdata/s2.txt", 15,
                "src/test/testdata/s2-centroids.txt", new KMeans());
        long startTime = System.currentTimeMillis();
        km.runMultiple(200);
        long endTime = System.currentTimeMillis();

        // since the results are dependant on randomness, slight difference is permitted
        assertTrue(Math.abs(1 - cumulativeTSE / km.cumulativeTSE) < 0.2);
        assertTrue(Math.abs(1 - cumulativeNMSE / km.cumulativeNMSE) < 0.2);
        assertTrue(Math.abs(1 - cumulativeCI / km.cumulativeCI) < 0.2);
        // 100 ms variance in time is allowed due to setup etc.
        assertTrue(Math.abs(endTime - startTime - km.runtimeMs) < 100);
        assertTrue(Math.abs(1 - bestTSE / km.dataset.TSE()) < 0.1);
    }

}