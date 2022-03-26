package FOO;
/*
 * Name: Jerome Sparnaay
 * Date: March 25 2022
 * Description: Simulation of bank
 */
public class Main {

    /**
     * Creates, run and prints statistics about simulation
     * @param args comand line arguments
     */
    public static void main(String[] args) {
    BankSim sim = new BankSim();
    sim.run(90000);
    sim.printStat();
    }
}
