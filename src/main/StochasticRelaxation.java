import java.util.Arrays;

/**
 * Stochastic relaxation algorithm
 *
 * @author Juho Puumalainen
 */
public class StochasticRelaxation implements ClusteringAlgorithm {

    /**
     * If the distortion improves less than epsilon*100% between consecutive iterations,
     * the algorithm exits.
     */
    private static final double EXIT_EPSILON = 1E-6;

    /**
     * Constant base of the temperature function. Value in range
     * (0, 1). Lower value makes the algorithm converge faster
     * but produces a (potentially) worse result.
     *
     * Value 0.95 suggested by the paper.
     */
    private double temperatureAlpha = 0.975;

    /**
     * Empty constructor using default parameters
     */
    public StochasticRelaxation() {
    }

    /**
     * Constructor that allows specifying the alpha value
     * of the temperature function
     *
     * @param temperatureAlpha governs how quickly the random movement slows down. Value in range ]0,1[
     */
    public StochasticRelaxation(double temperatureAlpha) throws IllegalArgumentException{
        if(temperatureAlpha <= 0 || temperatureAlpha >= 1)
            throw new IllegalArgumentException("StochasticRelaxation(): temperatureAlpha is not within acceptable range: ]0, 1[");
    	this.temperatureAlpha = temperatureAlpha;
    }

    /**
     * Iterates Stochastic relaxation until convergence
     */
    @Override
    public void cluster(Dataset dataset) {
        cluster(dataset, Integer.MAX_VALUE);
    }

    /**
     * Iterates Stochastic relaxation for set amount of
     * maximum iterations
     */
    @Override
    public void cluster(Dataset dataset, int maxIterations) {
        double[] variances = dataset.variances();
        double lastDistortion = Double.POSITIVE_INFINITY;
        for (int iteration = 1; iteration <= maxIterations; iteration++) {
            double distortion = dataset.partition();
            if (distortion <= lastDistortion && (lastDistortion - distortion) / distortion < EXIT_EPSILON) {
                break;
            }
            lastDistortion = distortion;
            updateCentroids(dataset);
            performPerturbations(iteration, variances, dataset);
        }
        new KMeans().cluster(dataset); // make sure the clusters are stable
    }

    /**
     * Updates centroids based on average vectors of partitions.
     *
     * Differs from standard k-means: If a cluster is left without
     * training vectors, its centroid is relocated.
     */
    private void updateCentroids(Dataset dataset) {
        double[][] sums = new double[dataset.centroids.length][dataset.data[0].length];
        int[] numberOfPoints = new int[dataset.centroids.length];
        for (int i = 0; i < dataset.data.length; i++) {
            for (int d = 0; d < dataset.data[i].length; d++) {
                sums[dataset.partitions[i]][d] += dataset.data[i][d];
            }
            numberOfPoints[dataset.partitions[i]]++;
        }
        for (int c = 0; c < dataset.centroids.length; c++) {
            if (numberOfPoints[c] == 0) {
                // cluster has no training vectors; re-assign centroid
                dataset.centroids[c] = Arrays.copyOf(dataset.data[(int) (Math.random() * dataset.data.length)], dataset.data[0].length);
            } else {
                for (int d = 0; d < sums[c].length; d++) {
                    sums[c][d] = sums[c][d] / numberOfPoints[c];
                }
                dataset.centroids[c] = sums[c];
            }
        }
    }

    /**
     * Moves code vectors randomly.
     */
    private void performPerturbations(int iteration, double[] variances, Dataset dataset) {
        double temperature = temperature(iteration);
        double[] adjustedVariance = new double[dataset.data[0].length];
        for (int d = 0; d < adjustedVariance.length; d++) {
            adjustedVariance[d] = variances[d] * temperature;
        }
        for (int c = 0; c < dataset.centroids.length; c++) {
            for (int d = 0; d < dataset.centroids[c].length; d++) {
                double random = ((-0.5 + Math.random()) * Math.sqrt(12 * adjustedVariance[d]));
                dataset.centroids[c][d] += random;
            }
        }
    }

    /**
     * Temperature function used. Returns temperature based on current
     * iteration number.
     * <p>
     * (Temperature function (c) in the paper)
     *
     * @param iteration Current iteration
     */
    private double temperature(int iteration) {
        return Math.pow(temperatureAlpha, iteration);
    }


}
