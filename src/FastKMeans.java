import java.util.ArrayList;
import java.util.Arrays;

/**
 * Fast k-means algorithm
 *
 * @author Juho Puumalainen
 */
public class FastKMeans implements ClusteringAlgorithm {

    /**
     * Iterates Fast K-Means until convergence
     *
     * @param dataset dataset to run the algorithm on
     */
    public void cluster(Dataset dataset) {
        cluster(dataset, Integer.MAX_VALUE);
    }

    /**
     * Iterates Fast K-Means until convergence or until set amount of iterations
     * have been done
     *
     * @param dataset    dataset to run the algorithm on
     * @param maxIterations maximum number of k-means iterations
     */
    public void cluster(Dataset dataset, int maxIterations) {
        ArrayList<Integer> active = new ArrayList<>();
        for (int i = 0; i < dataset.centroids.length; i++) {
            active.add(i);
        }
        double[] prevDistances = new double[dataset.data.length];
        for (int i = 0; i < dataset.data.length; i++) {
            prevDistances[i] = Dataset.distSq(dataset.data[i], dataset.centroids[dataset.partitions[i]]);
        }
        for (int iteration = 1; iteration <= maxIterations; iteration++) {
            double[][] prevCentroids = Arrays.copyOf(dataset.centroids, dataset.centroids.length);

            partition(prevDistances, active, dataset);
            dataset.updateCentroids();
            updateActiveCentroids(active, prevCentroids, dataset);
            if (active.size() == 0) {
                break;
            }
        }
    }

    /**
     * Performs fast k-means partitioning for the dataset
     *
     * @param prevDistances centroid distances before last iteration
     * @param active        active centroids indices
     * @param dataset       dataset to partition
     */
    public static void partition(double[] prevDistances, ArrayList<Integer> active, Dataset dataset) {
        for (int i = 0; i < dataset.data.length; i++) {
            double currentDist = Dataset.distSq(dataset.data[i],
                    dataset.centroids[dataset.partitions[i]]);
            if (currentDist <= prevDistances[i]) {
                partialSearch(i, currentDist, prevDistances, active, dataset);
            } else {
                fullSearch(i, currentDist, prevDistances, active, dataset);
            }
        }
    }

    /**
     * Finds correct partition for point within current centroid and active centroids
     *
     * @param id          index of point
     * @param currentDist distance to current centroid
     */
    private static void partialSearch(int id, double currentDist, double[] prevDistances, ArrayList<Integer> active, Dataset dataset) {
        double minDist = currentDist;
        int nearestCentroidIndex = dataset.partitions[id];
        for (int centroid : active) {
            double dist = Dataset.distSq(dataset.data[id], dataset.centroids[centroid]);
            if (dist < minDist) {
                minDist = dist;
                nearestCentroidIndex = centroid;
            }
        }
        dataset.partitions[id] = nearestCentroidIndex;
        prevDistances[id] = minDist;
    }

    /**
     * Finds correct partition for point within all centroids
     *
     * @param id          index of point
     * @param currentDist distance to current centroid
     */
    private static void fullSearch(int id, double currentDist, double[] prevDistances, ArrayList<Integer> active, Dataset dataset) {
        double minDist = currentDist;
        int nearestCentroidIndex = dataset.partitions[id];
        for (int centroid = 0; centroid < dataset.centroids.length; centroid++) {
            double dist = Dataset.distSq(dataset.data[id], dataset.centroids[centroid]);
            if (dist < minDist) {
                minDist = dist;
                nearestCentroidIndex = centroid;
            }
        }
        dataset.partitions[id] = nearestCentroidIndex;
        prevDistances[id] = minDist;
    }

    /**
     * Updates the list of active centroids based on moved centroids
     */
    private static void updateActiveCentroids(ArrayList<Integer> active, double[][] prevCentroids, Dataset dataset) {
        active.clear();
        for (int i = 0; i < dataset.centroids.length; i++) {
            if (!Arrays.equals(dataset.centroids[i], prevCentroids[i])) {
                active.add(i);
            }
        }
    }
}
