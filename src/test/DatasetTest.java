import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Dataset class
 *
 * TODO: split the methods to individual test cases
 * @author Juho Puumalainen
 */
class DatasetTest {

    private final double EPSILON =  0.00000000001;

    private static final double[][] S2_TRUNCATED_DATA = new double[][]{
            {845753, 636607},
            {812954, 643720},
            {868217, 609046},
            {920248, 607272},
            {841621, 639394}
    };

    private static final double[][] S2_REAL_CENTROIDS = new double[][]{
        {834220, 637720},
        {564548, 247802},
        {257635, 742332},
        {546668, 436109},
        {808321, 241707},
        {445012, 611486},
        {745598, 490515},
        {810815, 798625},
        {639888, 716368},
        {367949, 393074},
        {153217, 241347},
        {383641, 170604},
        {198897, 480489},
        {505997, 849938},
        {681475, 151167}
    };

    @Test
    void constructors() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2-truncated.txt", 5);
        assertTrue(Arrays.deepEquals(set.data, S2_TRUNCATED_DATA));

        // more clusters than data
        assertThrows(IllegalArgumentException.class, () -> new Dataset("src/test/testdata/s2-truncated.txt", 15));

        // too low number of clusters
        assertThrows(IllegalArgumentException.class, () -> new Dataset("src/test/testdata/s2-truncated.txt", 0));
        assertThrows(IllegalArgumentException.class, () -> new Dataset("src/test/testdata/s2-truncated.txt", -1));

        // nonnumerical data
        assertThrows(IllegalArgumentException.class, () -> new Dataset("src/test/testdata/s2-nonnumerical.txt", 15));

        // file does not exist
        assertThrows(IOException.class, () -> new Dataset("src/test/testdata/doesntexist.txt", 15));

        // Constructor which takes the pre-loaded matrix
        set = new Dataset(S2_TRUNCATED_DATA, 5);
        assertTrue(Arrays.deepEquals(set.data, S2_TRUNCATED_DATA));

        // more clusters than data
        assertThrows(IllegalArgumentException.class, () -> new Dataset(new double[][]{}, 2));

        // too low number of clusters
        assertThrows(IllegalArgumentException.class, () -> new Dataset(new double[][]{}, 0));
        assertThrows(IllegalArgumentException.class, () -> new Dataset(new double[][]{}, -1));

        // invalid dimensions
        assertThrows(IllegalArgumentException.class, () -> new Dataset(new double[][]{{1, 2}, {1, 2, 3}}, 2));
    }

    @Test
    void getNumberOfClusters() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2-truncated.txt", 5);
        assertEquals(5, set.getNumberOfClusters());
    }

    @Test
    void getData() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2-truncated.txt", 5);
        double[][] data = set.getData();
        assertTrue(Arrays.deepEquals(S2_TRUNCATED_DATA, data));
        // should be a deep copy
        set.data[0] = set.data[1];
        assertFalse(Arrays.deepEquals(set.getData(), data));
    }

    @Test
    void getPartitions() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2-truncated.txt", 5);
        set.initializeRandomCentroids();
        int[] partitions = set.getPartitions();
        assertArrayEquals(set.partitions, partitions);
        // should be a deep copy
        set.partitions[0]++;
        assertFalse(Arrays.equals(set.getPartitions(), partitions));
    }

    @Test
    void getCentroids() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2-truncated.txt", 5);
        set.initializeRandomCentroids();
        double[][] centroids = set.getCentroids();
        assertArrayEquals(set.centroids, centroids);
        // should be a deep copy
        set.centroids[0] = set.centroids[1];
        assertFalse(Arrays.equals(set.getCentroids(), centroids));
    }

    @Test
    void getRealCentroids() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2-truncated.txt", 5);
        set.initializeRandomCentroids();
        set.realCentroids = Arrays.copyOf(set.centroids, set.centroids.length);
        double[][] realCentroids = set.getRealCentroids();
        assertArrayEquals(set.realCentroids, realCentroids);
        // should be a deep copy
        set.realCentroids[0] = set.realCentroids[1];
        assertFalse(Arrays.equals(set.realCentroids, realCentroids));
    }

    @Test
    void copy() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2-truncated.txt", 5);
        set.initializeRandomCentroids();

        Dataset set2 = set.copy();
        assertTrue(Arrays.deepEquals(set.data, set2.data));
        assertTrue(Arrays.equals(set.partitions, set2.partitions));
        assertTrue(Arrays.deepEquals(set.centroids, set2.centroids));

        // shallow copy of data
        set.data[0] = set.data[1];
        assertTrue(Arrays.deepEquals(set.data, set2.data));

        // deep copy of partitions and centroids
        set.partitions[0]++;
        set.centroids[0] = set.centroids[1];
        assertTrue(!Arrays.equals(set.partitions, set2.partitions));
        assertTrue(!Arrays.deepEquals(set.centroids, set2.centroids));
    }

    @Test
    void loadRealCentroids() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.loadRealCentroids("src/test/testdata/s2-centroids.txt");
        assertTrue(Arrays.deepEquals(set.realCentroids, S2_REAL_CENTROIDS));

        // invalid number of clusters between constructor and file
        final Dataset set2 = new Dataset("src/test/testdata/s2.txt", 5);
        assertThrows(IllegalArgumentException.class, () -> set2.loadRealCentroids("src/test/testdata/s2-centroids.txt"));

        // file has nonnumerical data
        final Dataset set4 = new Dataset("src/test/testdata/s2.txt", 15);
        assertThrows(IllegalArgumentException.class, () -> set4.loadRealCentroids("src/test/testdata/s2-nonnumerical.txt"));

        // file has mismatching dimensions
        final Dataset set5 = new Dataset("src/test/testdata/s2.txt", 15);
        assertThrows(IllegalArgumentException.class, () -> set5.loadRealCentroids("src/test/testdata/s2-invalid_dimensions.txt"));

        // file does not exist
        final Dataset set3 = new Dataset("src/test/testdata/s2.txt", 15);
        assertThrows(IOException.class, () -> set3.loadRealCentroids("src/test/testdata/doesnotexist.txt"));
    }

    @Test
    void partition() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.initializeRandomCentroids();
        set.partition();

        checkPartitioning(set);
    }

    /**
     * Checks dataset's partitioning. Each data vector must be partitioned to the
     * nearest centroid's cluster.
     */
    void checkPartitioning(Dataset set){
        for(int i = 0; i < set.data.length; i++){
            double[] v = set.data[i];
            double minDist = Dataset.distSq(v, set.centroids[0]);
            int minId = 0;
            for(int j = 1; j < set.centroids.length; j++){
                double dist = Dataset.distSq(v, set.centroids[j]);
                if(dist < minDist){
                    minDist = dist;
                    minId = j;
                }
            }
            assertEquals(minId, set.partitions[i]);
        }
    }

    @Test
    void updateCentroids() throws IOException {
        // This will be tested in more depth during k-means testing:
        // if the k-means converges, the centroid should be updated properly

        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.initializeRandomCentroids();
        set.partition();
        double[][] oldCentroids =  Arrays.copyOf(set.centroids, set.centroids.length);
        double initialMSE = set.MSE();

        set.updateCentroids();
        set.partition();
        checkPartitioning(set);
        // check that the centroids moved
        // technically these can be equal but in practice it will never happen
        assertFalse(Arrays.equals(set.centroids, oldCentroids));

        // do a couple of k-means-like iterations to move the centroids and check that the clustering result improves
        for(int i = 0; i < 5; i++){
            set.updateCentroids();
            set.partition();
        }
        assertTrue(set.MSE() < initialMSE);

        // without centroid initialization
        set = new Dataset("src/test/testdata/s2.txt", 15);
        set.updateCentroids();
    }

    @Test
    void reduceSize() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.initializeRandomCentroids();
        set.partition();
        double initialMSE = set.MSE();

        int newSize = set.data.length / 2;
        set.reduceSize(newSize);
        assertEquals(newSize, set.data.length);
        assertTrue(set.MSE() < initialMSE * 1.2); // the MSE should stay similar but randomness can introduce slight variance

        // check too large, too small input
        assertThrows(IllegalArgumentException.class, () -> set.reduceSize(99999999));
        assertThrows(IllegalArgumentException.class, () -> set.reduceSize(-1));
        // this should pass even though it does not make much sense
        set.reduceSize(0);
        assertEquals(0, set.data.length);
    }

    @Test
    void initializeRandomCentroids() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.initializeRandomCentroids();
        double[][] oldCentroids =  Arrays.copyOf(set.centroids, set.centroids.length);
        set.initializeRandomCentroids();
        assertFalse(Arrays.deepEquals(set.centroids, oldCentroids));

        // each centroid should match a data vector
        for(double[] c : set.centroids){
            boolean matches = false;
            for(double[] v : set.data){
                if(Arrays.equals(c, v)){
                    matches = true;
                    break;
                }
            }
            assertTrue(matches);
        }

        // partitioning should be done as part of the function
        int[] oldPartitions = Arrays.copyOf(set.partitions, set.partitions.length);
        set.initializeRandomCentroids();
        assertFalse(Arrays.equals(set.partitions, oldPartitions));
    }

    @Test
    void centroidIndex() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.loadRealCentroids("src/test/testdata/s2-centroids.txt");
        set.initializeRandomCentroids();

        // all of the centroids are matched with a single real centroid -> 14 real centroids without match
        for(int i = 0; i < set.centroids.length; i++){
            set.centroids[i] = set.realCentroids[0];
        }
        assertEquals(14, set.centroidIndex());
        // equal centroids
        for(int i = 0; i < set.centroids.length; i++){
            set.centroids[i] = Arrays.copyOf(set.realCentroids[i], set.realCentroids[i].length);
        }
        assertEquals(0, set.centroidIndex());
        // slightly offset but still matching centroids
        for(int i = 0; i < set.centroids.length; i++){
            for(int d = 0; d < set.centroids[0].length; d++){
                set.centroids[i][d] += Math.random() * 10 - 5;
            }
        }
        assertFalse(Arrays.deepEquals(set.centroids, set.realCentroids));
        assertEquals(0, set.centroidIndex());
    }

    @Test
    void TSE() {
        double[][] vectors = new double[][]{{1, 0}, {0, 1}};
        Dataset set = new Dataset(vectors, 2);
        set.centroids = new double[2][2];
        set.centroids[0] = Arrays.copyOf(vectors[0], 2);
        set.centroids[1] = Arrays.copyOf(vectors[1], 2);
        set.partition();
        assertEquals(0, set.TSE());

        set.centroids[0] = new double[]{1, 1};
        set.partition();
        assertEquals(1, set.TSE());
        set.centroids[1] = new double[]{0, 0};
        set.partition();
        assertEquals(2, set.TSE());


        set.centroids[0] = Arrays.copyOf(vectors[0], 2);
        set.centroids[1] = Arrays.copyOf(vectors[1], 2);
        set.partition();
        set.centroids[0] = new double[]{2, 0};
        // re-partitioning skipped here intentionally
        // the centroid movement would change the partitioning but we only care about the distance to assigned centroids
        assertEquals(1, set.TSE());
        set.centroids[0] = new double[]{3, 0};
        assertEquals(4, set.TSE());
        set.centroids[0] = new double[]{4, 0};
        assertEquals(9, set.TSE());
        set.centroids[0] = new double[]{-1, 0};
        assertEquals(4, set.TSE());
    }

    @Test
    void MSE() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.initializeRandomCentroids();
        assertEquals(set.TSE() / set.data.length, set.MSE());
    }

    @Test
    void nMSE() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        set.initializeRandomCentroids();
        assertEquals(set.TSE() / set.data.length / set.data[0].length, set.nMSE());
    }

    @Test
    void mean() {
        double[][] vectors = new double[][]{{1, 0}, {0, 1}};
        Dataset set = new Dataset(vectors, 2);
        assertTrue(Arrays.equals(set.mean(), new double[]{0.5, 0.5}));

        for(int dim = 1; dim < 10; dim++) {
            vectors = new double[10][dim];
            for (double[] v : vectors) {
                for (int d = 0; d < dim; d++) {
                    v[d] = Math.random() * 10;
                }
            }
            set = new Dataset(vectors, 2);
            double[] mean = new double[dim];
            for (double[] v : vectors) {
                for (int d = 0; d < dim; d++) {
                    mean[d] += v[d] / vectors.length;
                }
            }
            // there might be slight difference in the calculating method, so use an epsilon comparison
            double[] setMean = set.mean();
            for (int d = 0; d < dim; d++) {
                assertTrue(Math.abs(setMean[d] - mean[d]) < EPSILON);
            }
        }
    }

    @Test
    void variances() throws IOException {
        Dataset set = new Dataset("src/test/testdata/s2.txt", 15);
        double[] variances = new double[set.data[0].length];
        double[] mean = set.mean();
        for(double[] v : set.data){
            for(int dim = 0; dim < variances.length; dim++){
                variances[dim] += (v[dim] - mean[dim]) * (v[dim] - mean[dim]);
            }
        }
        double[] setVariances = set.variances();
        for(int dim = 0; dim < variances.length; dim++){
            variances[dim] /= set.data.length;
            assertTrue(Math.abs(setVariances[dim] - variances[dim]) < EPSILON);
        }

    }

    @Test
    void nearestIndex() {
        double[][] vectors = new double[][]{{1, 0}, {0, 1}};
        Dataset set = new Dataset(vectors, 2);
        set.centroids = vectors;

        assertEquals(0, Dataset.nearestIndex(new double[]{234234, 11}, set.centroids));
        assertEquals(1, Dataset.nearestIndex(new double[]{0, 2}, set.centroids));
        assertEquals(1, Dataset.nearestIndex(new double[]{-1, 1}, set.centroids));
        assertEquals(1, Dataset.nearestIndex(new double[]{-10000, 1}, set.centroids));

        assertThrows(IllegalArgumentException.class, () -> Dataset.nearestIndex(new double[]{0}, set.centroids));
    }

    @Test
    void distSq() {
        assertEquals(Dataset.distSq(new double[]{1, 0}, new double[]{0, 1}), 2);
        assertEquals(Dataset.distSq(new double[]{2, 0}, new double[]{0, 2}), 8);
        assertEquals(Dataset.distSq(new double[]{-2, 0}, new double[]{0, -2}), 8);
        assertThrows(IllegalArgumentException.class, () -> Dataset.distSq(new double[]{1, 0}, new double[]{0, 1, 2}));
    }
}