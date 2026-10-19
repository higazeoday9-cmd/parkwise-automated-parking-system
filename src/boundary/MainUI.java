package boundary;

import control.IRSImportControl;

import java.util.Scanner;

public class MainUI {

    public void run() {
        Scanner sc = new Scanner(System.in);
        IRSImportControl irs = new IRSImportControl();

        while (true) {
            System.out.println("\n=== ParkWise Ex2 ===");
            System.out.println("1) Import IRS JSON (PriceRate)");
            System.out.println("0) Exit");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            try {
                if ("1".equals(choice)) {
                    int inserted = irs.importPriceRates("json/irs_2025.json");
                    System.out.println("Inserted: " + inserted);
                } else if ("0".equals(choice)) {
                    break;
                } else {
                    System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }

        System.out.println("Press Enter to close...");
        sc.nextLine();
    }
}

