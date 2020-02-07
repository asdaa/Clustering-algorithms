public interface ClusteringAlgorithm {

	/**
	 *  Run clustering until convergence
	 * @param  dataset The dataset object to run the clustering on.
	 * */
	void cluster(Dataset dataset);
	/**
	 * Run clustering with maximum number of iterations
	 * @param dataset The dataset object to run the clustering on.
	 * @param maxIterations Exit after this many iterations if the algorithm hasn't converged.
	 */
	void cluster(Dataset dataset, int maxIterations);
	
}
