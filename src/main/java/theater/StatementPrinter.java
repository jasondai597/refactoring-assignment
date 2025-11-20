package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        
        final StringBuilder result = new StringBuilder("Statement for " + invoice.getCustomer()
                + System.lineSeparator());

        for (Performance performance : invoice.getPerformances()) {
            final int rslt = getAmount(performance);

            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(performance).getName(),
                    usd(rslt),
                    performance.getAudience()));
        }

        final int volumeCredits = getTotalVolumeCredits();
        final int totalAmount = getTotalAmount();

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private int getAmount(Performance performance) {
        final int result;

        switch (getPlay(performance).getType()) {
            case "tragedy":
                result = getTragedyAmount(performance);
                break;
            case "comedy":
                result = getComedyAmount(performance);
                break;
            default:
                throw new RuntimeException("unknown type: "
                        + getPlay(performance).getType());
        }

        return result;
    }

    private int getTotalAmount() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            result += getAmount(performance);
        }
        return result;
    }

    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            result += getVolumeCredits(performance);
        }
        return result;
    }

    private static String usd(int totalAmount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount / Constants.PERCENT_FACTOR);
    }

    private int getVolumeCredits(Performance performance) {
        int result = 0;

        result += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience()
                    / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    private static int getComedyAmount(Performance performance) {
        int thisAmount;
        thisAmount = Constants.COMEDY_BASE_AMOUNT;
        if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
            thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                    + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                    * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
        }
        thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
        return thisAmount;
    }

    private static int getTragedyAmount(Performance performance) {
        int thisAmount;
        thisAmount = Constants.TRAGEDY_BASE_AMOUNT;
        if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
            thisAmount += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON * (performance.getAudience()
                    - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
        }
        return thisAmount;
    }
}
