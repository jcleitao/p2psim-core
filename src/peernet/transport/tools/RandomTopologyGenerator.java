package peernet.transport.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class RandomTopologyGenerator {

    static int outputlines = 0;

    public static void main(String[] args) throws FileNotFoundException {

        if (args.length < 3 && args.length > 6 || (args.length == 5 && !args[4].equalsIgnoreCase("-t"))) {
            System.err.println("Usage: java " + RandomTopologyGenerator.class.getCanonicalName() + " number_of_nodes medium_value variance [random_seed] [-t threads]");
            System.exit(1);
        }

        RandomGaussian rg = args.length == 4 ? new RandomGaussian(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Integer.parseInt(args[3])) :
                new RandomGaussian(Double.parseDouble(args[1]), Double.parseDouble(args[2]));

        int threads = args.length > 3 && args[args.length - 2].equalsIgnoreCase("-t") ? Integer.parseInt(args[args.length - 1]) : 1;
        int nodes = Integer.parseInt(args[0]);

        int segment = nodes / threads;

        int current = 0;
        Set<Thread> th = new HashSet<Thread>();
        for (int i = 0; i < threads - 1; i++) {
            final int c = current;
            th.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        compute(c, c + segment, nodes, rg.clone());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }));
            current += segment;
        }

        for (Thread t : th)
            t.start();

        compute(current, nodes, nodes, rg);

        for (Thread t : th)
            try {
                t.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }

    private static void compute(int start, int end, int n, RandomGaussian rg) throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream(new File("./networkmap-" + n + "-" + Thread.currentThread().getId() + ".txt")));
        for (int i = start; i < end; i++) {
            for (int j = 0; j < n; j++)
                out.print(Math.round(rg.nextValue()) + " ");
            out.println();
            if (i % 10 == 0)
                System.err.println(Thread.currentThread().getId() + ": output " + (i + 1 - start) + "/" + (end - start));
        }
    }

}
