import java.util.Arrays;

/**
 * Random swap algorithm
 *
 * @author Juho Puumalainen
 */
public class RandomSwap implements ClusteringAlgorithm {

    /**
     * Iterates Random swap for 1000 times
     *
     * @param dataset dataset to run the algorithm on
     */
    public void cluster(Dataset dataset) {
        cluster(dataset, 1000);
    }

    /**
     * Iterates Random swap until set amount of iterations have been done
     *
     * @param dataset    dataset to run the algorithm on
     * @param maxIterations maximum number of iterations (number of swamps)
     */
    public void cluster(Dataset dataset, int maxIterations) {
        double prevMSE = dataset.MSE();
        for (int iteration = 1; iteration <= maxIterations; iteration++) {
            double[][] prevCentroids = Arrays.copyOf(dataset.centroids, dataset.centroids.length);

            // swap random centroid with random data vector
            dataset.centroids[(int)(dataset.centroids.length * Math.random())] =
                    dataset.data[(int)(dataset.data.length * Math.random())];

            // two k-means iterations
            dataset.partition();
            dataset.updateCentroids();
            dataset.partition();
            dataset.updateCentroids();

            double newMSE = dataset.partition() / dataset.data.length;
            if(prevMSE < newMSE){
                // got worse, revert
                dataset.centroids = prevCentroids;
            } else {
                //System.out.printf("RS %d: %f\n", iteration, newMSE);
                prevMSE = newMSE;
            }
        }
        dataset.partition(); // this is required if the last swap was reverted
    }
}
