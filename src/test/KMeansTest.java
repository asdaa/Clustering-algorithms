import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class KMeansTest {
    private final double EPSILON =  0.00000000001;

    @Test
    void stableStart() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.loadRealCentroids("src/test/testdata/s2-centroids.txt");

        set.centroids = set.getRealCentroids(); // copy

        // when iinitialized to ideal position, the centroids should not move
        assertTrue(Arrays.deepEquals(set.centroids, set.realCentroids));
        assertTimeoutPreemptively(Duration.ofMillis(3000), () -> new KMeans().cluster(set));
        for(int i = 0; i < set.centroids.length; i++){
            for(int d = 0; d < set.centroids[0].length; d++){
                assertTrue(Math.abs(set.realCentroids[i][d] - set.realCentroids[i][d]) < EPSILON);
            }
        }
    }

    @Test
    void almostStableStart() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.loadRealCentroids("src/test/testdata/s2-centroids.txt");

        for(int i = 0; i < set.centroids.length; i++){
            for(int d = 0; d < set.centroids[0].length; d++){
                set.centroids[i][d] = set.realCentroids[d][d] + (Math.random() * 10 - 5);
            }
        }
        // when initialized close to ideal position, the centroids should move to ideal positions
        assertFalse(Arrays.deepEquals(set.centroids, set.realCentroids));
        assertTimeoutPreemptively(Duration.ofMillis(3000), () -> new KMeans().cluster(set));
        for(int i = 0; i < set.centroids.length; i++){
            for(int d = 0; d < set.centroids[0].length; d++){
                assertTrue(Math.abs(set.realCentroids[i][d] - set.realCentroids[i][d]) < EPSILON);
            }
        }
    }

    @Test
    void twoSyntheticClustersFull() throws IOException {
        double[][] data = new double[4096][2];
        // first cluster around (0, -0.5), radius 1
        for(int i = 0; i < data.length / 2; i++){
            data[i] = new double[]{0, -0.5};
            // this should provide gaussian-like distribution
            data[i][0] += (Math.random() + Math.random() + Math.random() + Math.random()) / 2 - 1;
            data[i][1] += (Math.random() + Math.random() + Math.random() + Math.random()) / 2 - 1;
        }

        // second cluster around (0, +0.5), radius 1
        for(int i = data.length / 2; i < data.length; i++){
            data[i] = new double[]{0, +0.5};
            // this should provide gaussian-like distribution
            data[i][0] += (Math.random() + Math.random() + Math.random() + Math.random()) / 2 - 1;
            data[i][1] += (Math.random() + Math.random() + Math.random() + Math.random()) / 2 - 1;
        }

        Dataset set = new Dataset(data, 2);
        set.centroids = new double[][]{{0, -0.05}, {0, +0.05}};
        set.partition();

        assertTimeoutPreemptively(Duration.ofMillis(3000), () -> new KMeans().cluster(set));
        assertTrue(Math.abs(set.centroids[0][0]) < 0.05);
        assertTrue(Math.abs(set.centroids[0][1] + 0.5) < 0.05);
        assertTrue(Math.abs(set.centroids[1][0]) < 0.05);
        assertTrue(Math.abs(set.centroids[1][1] - 0.5) < 0.05);
    }

    @Test
    void twoSyntheticClustersStepByStep() throws IOException {
        double[][] data = new double[4096][2];
        // first cluster around (0, -0.5), radius 1
        for(int i = 0; i < data.length / 2; i++){
            data[i] = new double[]{0, -0.5};
            // this should provide gaussian-like distribution
            data[i][0] += (Math.random() + Math.random() + Math.random() + Math.random()) / 2 - 1;
            data[i][1] += (Math.random() + Math.random() + Math.random() + Math.random()) / 2 - 1;
        }

        // second cluster around (0, +0.5), radius 1
        for(int i = data.length / 2; i < data.length; i++){
            data[i] = new double[]{0, +0.5};
            // this should provide gaussian-like distribution
            data[i][0] += (Math.random() + Math.random() + Math.random() + Math.random()) / 2 - 1;
            data[i][1] += (Math.random() + Math.random() + Math.random() + Math.random()) / 2 - 1;
        }

        Dataset set = new Dataset(data, 2);
        set.centroids = new double[][]{{0, -0.05}, {0, +0.05}};
        set.partition();

        // the result should improve as we iterate
        double TSE = set.TSE();
        for(int i = 0; i < 5; i++){
            assertTimeoutPreemptively(Duration.ofMillis(500), () -> new KMeans().cluster(set, 1));
            assertTrue(set.TSE() <= TSE);
            TSE = set.TSE();
        }
        // and the result should be same if we do the iterations in one go
        set.centroids = new double[][]{{0, -0.05}, {0, +0.05}};
        set.partition();
        assertTimeoutPreemptively(Duration.ofMillis(500), () -> new KMeans().cluster(set, 5));
        assertTrue(set.TSE() == TSE);
    }

}