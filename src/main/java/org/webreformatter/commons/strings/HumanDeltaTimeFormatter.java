/**
 * 
 */
package org.webreformatter.commons.strings;


/**
 * @author kotelnikov
 */
public class HumanDeltaTimeFormatter {

    public static class DefaultMessages implements IMessages {

        public String daysAgo(int days) {
            if (days == 1) {
                return "One day ago";
            }
            return days + " days ago";
        }

        public String hoursAgo(int hours) {
            if (hours == 1) {
                return "One hour ago";
            }
            return hours + " hours ago";
        }

        public String minutesAgo(int minutes) {
            if (minutes == 1) {
                return "One minute ago";
            }
            return minutes + " minutes ago";
        }

        public String monthsAgo(int months) {
            if (months == 1) {
                return "One month ago";
            }
            return months + " months ago";
        }

        public String secondsAgo(int seconds) {
            if (seconds == 0) {
                return "Just now";
            }
            if (seconds == 1) {
                return "One second ago";
            }
            return "Less than a minute ago";
        }

        public String weeksAgo(int weeks) {
            if (weeks == 1) {
                return "One week ago";
            }
            return weeks + " weeks ago";
        }

        public String yearsAgo(int years) {
            if (years == 1) {
                return "One year ago";
            }
            return years + " years ago";
        }
    }

    public interface IMessages {

        String daysAgo(int days);

        String hoursAgo(int hours);

        String minutesAgo(int minutes);

        String monthsAgo(int months);

        String secondsAgo(int seconds);

        String weeksAgo(int weeks);

        String yearsAgo(int years);

    }

    private static final int DAY;

    private static final int HOUR;

    private static final int MIN;

    private static final int MONTH;

    private static final int SEC;

    private static final int WEEK;

    private static final int YEAR;

    static {
        SEC = 1000;
        MIN = 60 * SEC;
        HOUR = 60 * MIN;
        DAY = 24 * HOUR;
        WEEK = 7 * DAY;
        MONTH = 4 * WEEK;
        YEAR = 12 * MONTH;
    }

    private IMessages fMessages;

    public HumanDeltaTimeFormatter() {
        this(new DefaultMessages());
    }

    /**
     * 
     */
    public HumanDeltaTimeFormatter(IMessages messages) {
        fMessages = messages;
    }

    /**
     * @param delta delta in milliseconds
     * @return
     */
    public String format(int delta) {
        if (delta < 0) {
            return null;
        } else if (delta < MIN) {
            int diff = delta / SEC;
            return fMessages.secondsAgo(diff);
        } else if (delta < HOUR) {
            int diff = delta / MIN;
            return fMessages.minutesAgo(diff);
        } else if (delta < DAY) {
            int diff = delta / HOUR;
            return fMessages.hoursAgo(diff);
        } else if (delta < WEEK) {
            int diff = delta / DAY;
            return fMessages.daysAgo(diff);
        } else if (delta < MONTH) {
            int diff = delta / WEEK;
            return fMessages.weeksAgo(diff);
        } else if (delta < YEAR) {
            int diff = delta / MONTH;
            return fMessages.monthsAgo(diff);
        } else {
            int diff = delta / YEAR;
            return fMessages.yearsAgo(diff);
        }
    }

}
