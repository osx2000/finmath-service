package de.osx2000.finmath.engine;

import de.osx2000.finmath.api.ProductDescriptorGenerator;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.ScheduleInterface;
import net.finmath.time.ScheduleMetaData;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import org.osgi.service.component.annotations.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component(service = ProductDescriptorGenerator.class)
public class ProductDescriptorGeneratorImpl implements ProductDescriptorGenerator {
    @Override
    public Map<String, Serializable> getProductDescriptor(
            String productType,
            long id,
            long formatVersion
    ) {
        Random random = new Random(id);

        Map<String, Serializable> result = new HashMap<>();
        result.put("productType", productType);

        LocalDate referenceDate = LocalDate.of(2001, 04, 27);
        BusinessdayCalendar calendar = new BusinessdayCalendarExcludingTARGETHolidays();

        LocalDate startDate = calendar.getRolledDate(referenceDate, (int)Math.round(random.nextDouble() * 3600));
        result.put("startDate", startDate);

        LocalDate endDate = calendar.getRolledDate(startDate, (int)Math.round(random.nextDouble() * 7200));
        result.put("maturityDate", endDate);

        Double swapRate = Math.floor((random.nextDouble() * 7 - 2)*100*100) / 100.0 / 100.0 / 100.0;
        result.put("swapRate", swapRate);

        ScheduleGenerator.Frequency frequencyFloat	= random.nextBoolean() ? ScheduleGenerator.Frequency.QUARTERLY : ScheduleGenerator.Frequency.SEMIANNUAL;
        result.put("leg1.frequency", frequencyFloat);

        ScheduleGenerator.Frequency frequencyFix		= ScheduleGenerator.Frequency.ANNUAL;
        result.put("leg2.frequency", frequencyFix);

        result.put("leg1.daycountConvention", ScheduleGenerator.DaycountConvention.ACT_360);
        result.put("leg2.daycountConvention", ScheduleGenerator.DaycountConvention.ACT_360);

        ScheduleMetaData schedulePrototypeFloat = new ScheduleMetaData(frequencyFloat, ScheduleGenerator.DaycountConvention.ACT_360, ScheduleGenerator.ShortPeriodConvention.FIRST, BusinessdayCalendar.DateRollConvention.FOLLOWING, new BusinessdayCalendarExcludingTARGETHolidays(), 2, 2, false);
        ScheduleInterface scheduleFloat = schedulePrototypeFloat.generateSchedule(referenceDate, startDate, endDate);
        result.put("leg1.periods", new ArrayList<>(scheduleFloat.getPeriods()));

        ScheduleMetaData schedulePrototypeFix = new ScheduleMetaData(ScheduleGenerator.Frequency.ANNUAL, ScheduleGenerator.DaycountConvention.ACT_360, ScheduleGenerator.ShortPeriodConvention.FIRST, BusinessdayCalendar.DateRollConvention.FOLLOWING, new BusinessdayCalendarExcludingTARGETHolidays(), 2, 2, false);
        ScheduleInterface scheduleFix = schedulePrototypeFix.generateSchedule(referenceDate, startDate, endDate);
        result.put("leg2.periods", new ArrayList<>(scheduleFix.getPeriods()));

        return result;
    }

}
