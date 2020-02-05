import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Contains the dataset and centroid/partition information
 *
 * @author Juho Puumalainen
 */
public class Dataset {

    /**
     * Expected number of clusters
     */
    public int numberOfClusters;
    /**
     * data vectors
     */
    public double[][] data;
    /**
     * partitioning of data vectors (each value is an index to corresponding
     * centroid)
     */
    public int[] partitions = null;
    /**
     * centroids; null if not initialized
     */
    public double[][] centroids = null;

    /**
     * Real centroids of the dataset; null if not loaded
     */
    public double[][] realCentroids = null;


    public Dataset(double[][] data, int numberOfClusters) {
        this.data = data;
        this.numberOfClusters = numberOfClusters;
    }

    /**
     * Constructor that loads the training vectors from file
     *
     * @throws NumberFormatException if there is an issue parsing the data
     * @throws IOException           if the file cannot be accessed
     */
    public Dataset(String filename, int numberOfClusters) throws NumberFormatException, IOException {
        this.numberOfClusters = numberOfClusters;
        BufferedReader br = new BufferedReader(new FileReader(filename));

        int dimensions = -1;
        ArrayList<double[]> data = new ArrayList<>();
        String line = "";

        while ((line = br.readLine()) != null) {
            String[] toks = line.split("\\s+");
            ArrayList<Double> numbers = new ArrayList<>();
            for (String s : toks) {
                if (s.trim().length() > 0)
                    numbers.add(Double.parseDouble(s.trim()));
            }
            double[] d = new double[numbers.size()];
            for (int i = 0; i < numbers.size(); i++) {
                d[i] = numbers.get(i);
            }
            if (dimensions == -1)
                dimensions = d.length;

            if (d.length != dimensions) {
                System.err.println("Dataset(): Data dimensions don't match");
                System.exit(1);
            }

            data.add(d);
        }
        br.close();

        this.data = data.toArray(new double[data.size()][data.get(0).length]);
    }

    /**
     * @return new copy of the object with shallow copy of the data vectors and
     * deep copy of partitions and centroids
     */
    public Dataset copy() {
        Dataset copy = new Dataset(data, numberOfClusters);
        if (partitions != null)
            copy.partitions = partitions.clone();

        if (centroids != null) {
            copy.centroids = new double[centroids.length][];
            for (int i = 0; i < centroids.length; i++)
                copy.centroids[i] = centroids[i].clone();
        }

        if (realCentroids != null) {
            copy.realCentroids = new double[realCentroids.length][];
            for (int i = 0; i < realCentroids.length; i++)
                copy.realCentroids[i] = realCentroids[i].clone();
        }

        return copy;
    }

    /**
     * Loads real centroids from file.
     *
     * @throws NumberFormatException if there is an issue parsing the file
     * @throws IOException           if the file cannot be accessed
     */
    public void loadRealCentroids(String filename) throws NumberFormatException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));

        ArrayList<double[]> realCentroids = new ArrayList<>();
        String line = "";

        while ((line = br.readLine()) != null) {
            String[] toks = line.split("\\s+");
            ArrayList<Double> numbers = new ArrayList<>();
            for (String s : toks) {
                if (s.trim().length() > 0)
                    numbers.add(Double.parseDouble(s.trim()));
            }
            double[] d = new double[numbers.size()];
            for (int i = 0; i < numbers.size(); i++) {
                d[i] = numbers.get(i);
            }

            if (d.length != data[0].length) {
                System.err.println("loadRealCentroids(): centroid dimensions don't match with data");
                System.exit(1);
            }

            realCentroids.add(d);
        }
        br.close();

        this.realCentroids = realCentroids
                .toArray(new double[realCentroids.size()][realCentroids.get(0).length]);

    }

    /**
     * Generates new partitioning based on current centroids
	 * @return TSE for the partitioning of the dataset
     */
    public double partition() {
        if (partitions == null)
            partitions = new int[data.length];
		double TSE = 0;
		for (int i = 0; i < data.length; i++) {
			partitions[i] = Dataset.nearestIndex(data[i], centroids);
			TSE += Dataset.distSq(data[i], centroids[partitions[i]]);
		}
		return TSE;
    }

    /**
     * Updates centroids based on partition average vectors
     */
    public void updateCentroids() {
        if (partitions == null) {
            throw new RuntimeException("updateCentroids(): Cannot calculate centroids; "
                    + "partitions==null");
        }
        double[][] sums = new double[centroids.length][data[0].length];
        int[] numberOfPoints = new int[centroids.length];
        for (int i = 0; i < data.length; i++) {
            for (int d = 0; d < data[i].length; d++) {
                sums[partitions[i]][d] += data[i][d];
            }
            numberOfPoints[partitions[i]]++;
        }

        for (int c = 0; c < centroids.length; c++) {
            for (int d = 0; d < sums[c].length; d++) {
                sums[c][d] = sums[c][d] / numberOfPoints[c];
            }
            centroids[c] = sums[c];
        }
    }

    /**
     * Reduces the training data to given size by removing random vectors
     */
    public void reduceSize(int newSize) {
        if (newSize > data.length) {
            throw new IllegalArgumentException("reduceSize(): newSize is larger than dataset size");
        }
        Integer[] keptIndices = pickRandom(data.length, newSize);
        double[][] newData = new double[newSize][data[0].length];
        for (int i = 0; i < keptIndices.length; i++) {
            newData[i] = data[keptIndices[i]];
        }
        data = newData;
    }

    /**
     * Initializes centroids using random data vectors and re-partitions data
     */
    public void initializeRandomCentroids() {
        centroids = new double[numberOfClusters][data[0].length];
        Integer[] centroidPointIndices = pickRandom(data.length, numberOfClusters);
        for (int i = 0; i < centroids.length; i++) {
            centroids[i] = data[centroidPointIndices[i]];
        }

        partition();
    }

    /**
     * Calculates centroid index for this dataset. Used for assessing clustering quality
	 * by comparing current centroids to known "best" centroids (or the centroids the data was
	 * generated from).
     *
     * @return centroid index or -1 if real centroids not loaded
     */
    public int centroidIndex() {
        if (realCentroids == null) {
            throw new IllegalStateException("centroidIndex(): Real centroids not loaded");
        }
        return CentroidIndex(centroids, realCentroids);
    }

    /**
     * Calculates centroid index between two sets of centroids.
     * Each of the found clusters should have a pair in known clusters set and vice versa. The
     * result is based on the number of orphaned centroids.
     */
    private static int CentroidIndex(double[][] centroidsA, double[][] centroidsB) {
        if (centroidsA.length != centroidsB.length) {
            throw new IllegalArgumentException("CentroidIndex(): centroidsA.length != centroidsB.length");
        }
        HashSet<Integer> aOrphans = new HashSet<>();
        for (int i = 0; i < centroidsA.length; i++)
            aOrphans.add(i);
        // remove centroids with mappings B->A from orphan set
        for (int c = 0; c < centroidsB.length; c++) {
            int nearestA = nearestIndex(centroidsB[c], centroidsA);
            aOrphans.remove(nearestA);
        }
        HashSet<Integer> bOrphans = new HashSet<>();
        for (int i = 0; i < centroidsB.length; i++)
            bOrphans.add(i);
        // remove centroids with mappings A->B from orphan set
        for (int c = 0; c < centroidsA.length; c++) {
            int nearestB = nearestIndex(centroidsA[c], centroidsB);
            bOrphans.remove(nearestB);
        }
        return Math.max(aOrphans.size(), bOrphans.size());
    }

	/**
	 * @return total squared error
	 */
	public double TSE() {
        double sum = 0;

        for (int i = 0; i < data.length; i++) {
            double[] point = data[i];
            double[] centroid = centroids[partitions[i]];
            sum += distSq(point, centroid);
        }

        return sum;
    }

    public double MSE() {
        return TSE() / (data.length);
    }

    public double nMSE() {
        return MSE() / data[0].length;
    }

    /**
     * @return mean training vector
     */
    public double[] mean() {
        double[] mean = new double[data[0].length];
        for (double[] d : data) {
            for (int i = 0; i < d.length; i++)
                mean[i] += d[i];
        }
        for (int i = 0; i < mean.length; i++)
            mean[i] = mean[i] / data.length;
        return mean;
    }

    /**
     * @return Variances of the invidual variables
     */
    public double[] variances() {
        double[] mean = mean();
        double[] variance = new double[data[0].length];
        for (double[] d : data) {
            for (int i = 0; i < d.length; i++)
                variance[i] += (mean[i] - d[i]) * (mean[i] - d[i]);
        }
        for (int i = 0; i < variance.length; i++)
            variance[i] = variance[i] / data.length;

        return variance;
    }

    /**
     * @return nearest centroid label (index) for point
     */
    public static int nearestIndex(double[] point, double[][] centroids) {
        double minDist = Double.POSITIVE_INFINITY;
        int minLabel = -1;
        for (int i = 0; i < centroids.length; i++) {
            double dist = distSq(point, centroids[i]);
            if (dist < minDist) {
                minDist = dist;
                minLabel = i;
            }
        }
        return minLabel;
    }

    /**
     * @return Squared euclidean distance between 2 points
     */
    public static double distSq(double[] p1, double[] p2) {
        double sum = 0;
        for (int d = 0; d < p1.length; d++) {
            sum += (p1[d] - p2[d]) * (p1[d] - p2[d]);
        }
        return sum;
    }

    /**
     * @return array of n integers from interval [0, max[
     */
    private static Integer[] pickRandom(int max, int n) {
        if (n > max) {
            throw new IllegalArgumentException(
                    "pickRandom(): n > max; interval doesn't contain n unique integers!");
        }
        Random r = new Random();
        HashSet<Integer> pickedNumbers = new HashSet<>();
        while (pickedNumbers.size() < n)
            pickedNumbers.add(r.nextInt(max));
        return pickedNumbers.toArray(new Integer[pickedNumbers.size()]);
    }
}
