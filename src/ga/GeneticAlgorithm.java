package ga;


import algorithms.*;
import ga.geneticoperators.Mutation;
import ga.geneticoperators.Recombination;
import ga.selectionmethods.SelectionMethod;
import statistics.ExecutionTime;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class GeneticAlgorithm<I extends Individual, P extends Problem<I>> extends Algorithm<I, P> {

    private final int populationSize;
    private Population<I, P> population;
    private final SelectionMethod<I, P> selection;
    private final Recombination<I, P> recombination;
    private final Mutation<I, P> mutation;

    public GeneticAlgorithm(
            int populationSize,
            int maxGenerations,
            SelectionMethod<I, P> selection,
            Recombination<I, P> recombination,
            Mutation<I, P> mutation,
            Random rand) {
        super(maxGenerations, rand);

        this.populationSize = populationSize;
        this.selection = selection;
        this.recombination = recombination;
        this.mutation = mutation;
    }

    public I run(P problem) {
        t = 0;
        population = new Population<>(populationSize, problem);

        globalBest = population.evaluate();
//        I elite = globalBest;
        fireIterationEnded(new AlgorithmEvent(this));
        long sumDuration = 0;
        long selectionTimeSum = 0, recombinationTimeSum = 0, mutationTimeSum = 0, evaluationTimeSum = 0;

        while (t < maxIterations && !stopped) {
            Instant start = Instant.now();
            Population<I, P> populationAux = selection.run(population);
            selectionTimeSum += Duration.between(start, Instant.now()).toNanos();

            Instant operationStart = Instant.now();
            recombination.run(populationAux);
            recombinationTimeSum += Duration.between(operationStart, Instant.now()).toNanos();

            operationStart = Instant.now();
            mutation.run(populationAux);
            mutationTimeSum += Duration.between(operationStart, Instant.now()).toNanos();
            population = populationAux;

            operationStart = Instant.now();
            I bestInGen = population.evaluate();
            evaluationTimeSum += Duration.between(operationStart, Instant.now()).toNanos();

            computeBestInRun(bestInGen);
            t++;
            Instant finish = Instant.now();
            sumDuration += Duration.between(start, finish).toNanos();;
            fireIterationEnded(new AlgorithmEvent(this));

        }

        ExecutionTime.getInstance().getRoot().add(new DefaultMutableTreeNode("Average Time per iteration: " + (double) sumDuration / maxIterations / 1000000 + "ms"));
        DefaultMutableTreeNode breakdown = new DefaultMutableTreeNode("Time Breakdown");
        breakdown.add(new DefaultMutableTreeNode("Selection Time Average: " + (double) selectionTimeSum / maxIterations / 1000000 + "ms"));
        breakdown.add(new DefaultMutableTreeNode("Recombination Time Average: " + (double) recombinationTimeSum / maxIterations / 1000000 + "ms"));
        breakdown.add(new DefaultMutableTreeNode("Mutation Time Average: " + (double) mutationTimeSum / maxIterations / 1000000 + "ms"));
        breakdown.add(new DefaultMutableTreeNode("Evaluation Time Average: " + (double) evaluationTimeSum / maxIterations / 1000000 + "ms"));
        ExecutionTime.getInstance().getRoot().add(breakdown);

        fireRunEnded(new AlgorithmEvent(this));

        return globalBest;
    }

    private void computeBestInRun(I bestInGen) {
        if (bestInGen.compareTo(globalBest) > 0) {
            globalBest = (I) bestInGen.clone();
        }
    }

    public double getAverageFitness() {
        return population.getAverageFitness();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Population size:" + populationSize + "\n");
        sb.append("Max generations:" + maxIterations + "\n");
        sb.append("Selection:" + selection + "\n");
        sb.append("Recombination:" + recombination + "\n");
        sb.append("Mutation:" + mutation + "\n");
        return sb.toString();
    }

}
