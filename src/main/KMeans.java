import java.util.Arrays;

/**
 * Standard kMeans
 *
 * @author Juho Puumalainen
 */
public class KMeans implements ClusteringAlgorithm {

    /**
     * Iterates k-means until convergence
     */
    public void cluster(Dataset dataset) {
    	cluster(dataset, Integer.MAX_VALUE);
    }

    /**
     * Iterates k-means for set amount of maximum iterations
     */
    public void cluster(Dataset dataset, int iterations) {
        for (int iteration = 1; iteration <= iterations; iteration++) {
            double[][] prevCentroids = Arrays.copyOf(dataset.centroids, dataset.centroids.length);
            dataset.partition();
            dataset.updateCentroids();

            if (!centroidsChanged(prevCentroids, dataset)) {
                break;
            }
        }
    }

    /**
     * Checks if the centroids changed from previous centroids.
     */
    private boolean centroidsChanged(double[][] prevCentroids, Dataset dataset) {
        for (int i = 0; i < dataset.centroids.length; i++) {
            if (!Arrays.equals(dataset.centroids[i], prevCentroids[i])) {
                return true;
            }
        }
        return false;
    }
}
