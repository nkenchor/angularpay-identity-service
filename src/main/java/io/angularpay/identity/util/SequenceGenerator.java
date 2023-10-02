package io.angularpay.identity.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static io.angularpay.identity.common.Constants.SERVICE_CODE;

public class SequenceGenerator {

    public static final AtomicReference<DailySequence> cache = new AtomicReference<>();

    static {
        // set initial value
        cache.set(new DailySequence(Date.from(Instant.now()), 0L));
    }

    private static long getNextSequence() {
        boolean success;
        long nextSequence;
        do {
            DailySequence expectedValue = cache.get();
            nextSequence = DateUtils.isSameDay(expectedValue.getDate(), Date.from(Instant.now())) ? expectedValue.getSequence() : 0L;
            Date newDate = DateUtils.isSameDay(expectedValue.getDate(), Date.from(Instant.now())) ? expectedValue.getDate() : Date.from(Instant.now());
            DailySequence newValue = new DailySequence(newDate, ++nextSequence);
            success = cache.compareAndSet(expectedValue, newValue);
        } while (!success);
        return nextSequence;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class DailySequence {
        private Date date;
        private long sequence;
    }

    public static String generateRequestTag() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.from(Instant.now()));

        String year = new SimpleDateFormat("yy").format(calendar.getTime());
        String month = new SimpleDateFormat("MMM").format(calendar.getTime());
        String day = new SimpleDateFormat("dd").format(calendar.getTime());
        String hour = new SimpleDateFormat("HH").format(calendar.getTime());
        String minute = new SimpleDateFormat("mm").format(calendar.getTime());
        String seconds = new SimpleDateFormat("ss").format(calendar.getTime());
        String milliseconds = new SimpleDateFormat("SSS").format(calendar.getTime());

        return "@" + SERVICE_CODE + year + month.toUpperCase() + day + hour + minute + seconds + milliseconds; // + getNextSequence();
    }
}
