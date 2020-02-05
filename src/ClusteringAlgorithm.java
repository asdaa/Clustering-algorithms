
public interface ClusteringAlgorithm {

	/** Run clustering until convergence*/
	void cluster(Dataset dataset);
	/** Run clustering with maximum number of iterations*/
	void cluster(Dataset dataset, int maxIterations);
	
}
